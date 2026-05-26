package com.gugugaga.jsmedicine.module.interaction.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppFeedbackRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.feedback.entity.Feedback;
import com.gugugaga.jsmedicine.module.interaction.feedback.mapper.FeedbackMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaAnswer;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaQuestion;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaAnswerMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaQuestionMapper;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppInteractionService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final CurrentAppUserResolver currentAppUserResolver;
    private final StudentMapper studentMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final FeedbackMapper feedbackMapper;

    public AppInteractionService(
            CurrentAppUserResolver currentAppUserResolver,
            StudentMapper studentMapper,
            QaQuestionMapper qaQuestionMapper,
            QaAnswerMapper qaAnswerMapper,
            FeedbackMapper feedbackMapper
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.studentMapper = studentMapper;
        this.qaQuestionMapper = qaQuestionMapper;
        this.qaAnswerMapper = qaAnswerMapper;
        this.feedbackMapper = feedbackMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppQaQuestionResponse createQuestion(AppQaQuestionRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        QaQuestion question = new QaQuestion();
        question.setUserId(session.userId());
        question.setStudentId(findStudent(session.userId()).map(Student::getId).orElse(null));
        question.setExpertCategoryId(request.expertCategoryId());
        question.setExpertId(request.expertId());
        question.setTitle(request.title());
        question.setContent(request.content());
        question.setStatus(QaStatus.PENDING);
        question.setDeleted(0);
        qaQuestionMapper.insert(question);
        return toQaQuestionResponse(question, true);
    }

    public PageResponse<AppQaQuestionResponse> myQuestions(long page, long size) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        Page<QaQuestion> questionPage = qaQuestionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<QaQuestion>()
                        .eq(QaQuestion::getDeleted, 0)
                        .eq(QaQuestion::getUserId, userId)
                        .orderByDesc(QaQuestion::getCreatedAt));
        return pageResponse(questionPage, questionPage.getRecords().stream().map(question -> toQaQuestionResponse(question, false)).toList());
    }

    public AppQaQuestionResponse questionDetail(Long id) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        QaQuestion question = qaQuestionMapper.selectOne(new LambdaQueryWrapper<QaQuestion>()
                .eq(QaQuestion::getDeleted, 0)
                .eq(QaQuestion::getId, id)
                .eq(QaQuestion::getUserId, userId)
                .last("LIMIT 1"));
        if (question == null) {
            throw new com.gugugaga.jsmedicine.common.exception.BusinessException(
                    com.gugugaga.jsmedicine.common.exception.ErrorCode.NOT_FOUND, "QA question does not exist");
        }
        return toQaQuestionResponse(question, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedbackResponse submitFeedback(AppFeedbackRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        Feedback feedback = new Feedback();
        feedback.setUserId(session.userId());
        feedback.setStudentId(findStudent(session.userId()).map(Student::getId).orElse(null));
        feedback.setFeedbackType(request.feedbackType());
        feedback.setContent(request.content());
        feedback.setContact(request.contact());
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setDeleted(0);
        feedbackMapper.insert(feedback);
        return toFeedbackResponse(feedback);
    }

    private Optional<Student> findStudent(Long userId) {
        return Optional.ofNullable(studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .eq(Student::getStatus, EnabledStatus.ENABLED)
                .last("LIMIT 1")));
    }

    private AppQaQuestionResponse toQaQuestionResponse(QaQuestion question, boolean includeAnswers) {
        return new AppQaQuestionResponse(question.getId(), question.getExpertCategoryId(), question.getExpertId(),
                question.getTitle(), question.getContent(), question.getStatus(), includeAnswers ? loadAnswers(question.getId()) : List.of());
    }

    private List<QaAnswerResponse> loadAnswers(Long questionId) {
        return qaAnswerMapper.selectList(new LambdaQueryWrapper<QaAnswer>()
                        .eq(QaAnswer::getDeleted, 0)
                        .eq(QaAnswer::getQuestionId, questionId)
                        .orderByAsc(QaAnswer::getAnsweredAt))
                .stream()
                .map(answer -> new QaAnswerResponse(answer.getId(), answer.getQuestionId(), answer.getAdminId(),
                        answer.getExpertId(), answer.getContent(), answer.getAnsweredAt()))
                .toList();
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getUserId(), feedback.getStudentId(),
                feedback.getFeedbackType(), feedback.getContent(), feedback.getContact(), feedback.getStatus(),
                feedback.getProcessedBy(), feedback.getProcessedAt(), feedback.getProcessNote());
    }

    private <E, R> PageResponse<R> pageResponse(Page<E> page, List<R> records) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }
}
