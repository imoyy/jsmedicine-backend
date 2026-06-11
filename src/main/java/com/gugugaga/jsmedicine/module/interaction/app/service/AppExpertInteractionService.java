package com.gugugaga.jsmedicine.module.interaction.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.app.entity.AppExpertSession;
import com.gugugaga.jsmedicine.module.expert.app.service.CurrentAppExpertResolver;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategoryRelation;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryRelationMapper;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppExpertQaAnswerRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppExpertQaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaAnswer;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaQuestion;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaAnswerMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AppExpertInteractionService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final CurrentAppExpertResolver currentAppExpertResolver;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final ExpertCategoryRelationMapper expertCategoryRelationMapper;

    public AppExpertInteractionService(
            CurrentAppExpertResolver currentAppExpertResolver,
            QaQuestionMapper qaQuestionMapper,
            QaAnswerMapper qaAnswerMapper,
            ExpertCategoryRelationMapper expertCategoryRelationMapper
    ) {
        this.currentAppExpertResolver = currentAppExpertResolver;
        this.qaQuestionMapper = qaQuestionMapper;
        this.qaAnswerMapper = qaAnswerMapper;
        this.expertCategoryRelationMapper = expertCategoryRelationMapper;
    }

    public PageResponse<AppExpertQaQuestionResponse> pageQuestions(long page, long size, String keyword, QaStatus status) {
        AppExpertSession expertSession = currentAppExpertResolver.requireCurrentExpert();
        List<Long> categoryIds = loadExpertCategoryIds(expertSession.expertId());
        Page<QaQuestion> questionPage = qaQuestionMapper.selectPage(
                new Page<>(normalizePage(page), normalizeSize(size)),
                visibleQuestionWrapper(expertSession, categoryIds, keyword, status)
                        .orderByDesc(QaQuestion::getCreatedAt)
        );
        return new PageResponse<>(
                questionPage.getRecords().stream().map(this::toExpertQaQuestionResponse).toList(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
    }

    public AppExpertQaQuestionResponse questionDetail(Long id) {
        AppExpertSession expertSession = currentAppExpertResolver.requireCurrentExpert();
        QaQuestion question = requireQuestion(id);
        ensureQuestionVisible(question, expertSession, loadExpertCategoryIds(expertSession.expertId()));
        return toExpertQaQuestionResponse(question);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppExpertQaQuestionResponse answerQuestion(Long questionId, AppExpertQaAnswerRequest request) {
        AppExpertSession expertSession = currentAppExpertResolver.requireCurrentExpert();
        List<Long> categoryIds = loadExpertCategoryIds(expertSession.expertId());
        QaQuestion question = requireQuestion(questionId);
        ensureQuestionVisible(question, expertSession, categoryIds);
        if (question.getStatus() == QaStatus.CLOSED) {
            throw new BusinessException(ErrorCode.CONFLICT, "QA question is already closed");
        }
        if (question.getExpertId() != null && !Objects.equals(question.getExpertId(), expertSession.expertId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "QA question is assigned to another expert");
        }

        QaAnswer answer = new QaAnswer();
        answer.setQuestionId(questionId);
        answer.setAdminId(null);
        answer.setExpertId(expertSession.expertId());
        answer.setContent(request.content());
        answer.setAnsweredAt(LocalDateTime.now());
        answer.setDeleted(0);
        qaAnswerMapper.insert(answer);

        if (question.getExpertId() == null) {
            question.setExpertId(expertSession.expertId());
        }
        question.setStatus(QaStatus.ANSWERED);
        qaQuestionMapper.updateById(question);
        return toExpertQaQuestionResponse(question);
    }

    private LambdaQueryWrapper<QaQuestion> visibleQuestionWrapper(
            AppExpertSession expertSession,
            List<Long> categoryIds,
            String keyword,
            QaStatus status
    ) {
        LambdaQueryWrapper<QaQuestion> wrapper = new LambdaQueryWrapper<QaQuestion>()
                .eq(QaQuestion::getDeleted, 0)
                .eq(status != null, QaQuestion::getStatus, status)
                .and(hasText(keyword), query -> query
                        .like(QaQuestion::getTitle, keyword)
                        .or()
                        .like(QaQuestion::getContent, keyword));
        if (categoryIds.isEmpty()) {
            return wrapper.eq(QaQuestion::getExpertId, expertSession.expertId());
        }
        return wrapper.and(query -> query
                .eq(QaQuestion::getExpertId, expertSession.expertId())
                .or()
                .isNull(QaQuestion::getExpertId)
                .in(QaQuestion::getExpertCategoryId, categoryIds));
    }

    private void ensureQuestionVisible(QaQuestion question, AppExpertSession expertSession, List<Long> categoryIds) {
        if (Objects.equals(question.getExpertId(), expertSession.expertId())) {
            return;
        }
        if (question.getExpertId() != null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "QA question is assigned to another expert");
        }
        if (question.getExpertCategoryId() == null || !categoryIds.contains(question.getExpertCategoryId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "QA question is outside current expert scope");
        }
    }

    private QaQuestion requireQuestion(Long id) {
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || !Objects.equals(question.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "QA question does not exist");
        }
        return question;
    }

    private List<Long> loadExpertCategoryIds(Long expertId) {
        return expertCategoryRelationMapper.selectList(new LambdaQueryWrapper<ExpertCategoryRelation>()
                        .eq(ExpertCategoryRelation::getExpertId, expertId))
                .stream()
                .map(ExpertCategoryRelation::getCategoryId)
                .toList();
    }

    private AppExpertQaQuestionResponse toExpertQaQuestionResponse(QaQuestion question) {
        return new AppExpertQaQuestionResponse(
                question.getId(),
                question.getUserId(),
                question.getStudentId(),
                question.getExpertCategoryId(),
                question.getExpertId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                qaStatusCode(question.getStatus()),
                qaStatusLabel(question.getStatus()),
                question.getCreatedAt(),
                loadAnswers(question.getId())
        );
    }

    private List<QaAnswerResponse> loadAnswers(Long questionId) {
        return qaAnswerMapper.selectList(new LambdaQueryWrapper<QaAnswer>()
                        .eq(QaAnswer::getDeleted, 0)
                        .eq(QaAnswer::getQuestionId, questionId)
                        .orderByAsc(QaAnswer::getAnsweredAt))
                .stream()
                .map(answer -> new QaAnswerResponse(
                        answer.getId(),
                        answer.getQuestionId(),
                        answer.getAdminId(),
                        answer.getExpertId(),
                        answer.getContent(),
                        answer.getAnsweredAt()
                ))
                .toList();
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private String qaStatusCode(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "pending";
            case ANSWERED -> "answered";
            case CLOSED -> "closed";
        };
    }

    private String qaStatusLabel(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "待回复";
            case ANSWERED -> "已回复";
            case CLOSED -> "已关闭";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
