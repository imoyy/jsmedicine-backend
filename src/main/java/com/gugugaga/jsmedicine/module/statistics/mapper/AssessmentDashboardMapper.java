package com.gugugaga.jsmedicine.module.statistics.mapper;

import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentLatestEventResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentParticipantRow;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentQuestionStructureRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentDashboardMapper {

    @Select("""
            SELECT
                snapshot.student_id AS studentId,
                snapshot.student_name_snapshot AS studentName,
                snapshot.mobile_snapshot AS mobile,
                snapshot.masked_id_card_no_snapshot AS maskedIdCardNo,
                snapshot.province_code_snapshot AS provinceCode,
                snapshot.province_name_snapshot AS provinceName,
                snapshot.city_code_snapshot AS cityCode,
                snapshot.city_name_snapshot AS cityName,
                snapshot.district_code_snapshot AS districtCode,
                snapshot.district_name_snapshot AS districtName,
                snapshot.organization_id_snapshot AS organizationId,
                snapshot.organization_name_snapshot AS organizationName,
                record.id AS recordId,
                record.status AS recordStatus,
                record.submit_type AS submitType,
                record.score AS score,
                record.passed AS passed,
                record.started_at AS startedAt,
                record.submitted_at AS submittedAt
            FROM exam_assessment_students snapshot
            LEFT JOIN exam_records record
                ON record.assessment_id = snapshot.assessment_id
               AND record.student_id = snapshot.student_id
            WHERE snapshot.assessment_id = #{assessmentId}
            ORDER BY snapshot.id ASC
            """)
    List<AssessmentParticipantRow> selectParticipantRows(@Param("assessmentId") Long assessmentId);

    @Select("""
            SELECT
                question.question_type AS questionType,
                COUNT(1) AS questionCount,
                MIN(relation.score) AS scorePerQuestion,
                COALESCE(SUM(relation.score), 0) AS totalScore
            FROM exam_assessments assessment
            JOIN exam_paper_questions relation ON relation.paper_id = assessment.paper_id
            JOIN questions question ON question.id = relation.question_id AND question.deleted = 0
            WHERE assessment.id = #{assessmentId}
              AND assessment.deleted = 0
            GROUP BY question.question_type
            ORDER BY question.question_type ASC
            """)
    List<AssessmentQuestionStructureRow> selectQuestionStructureRows(@Param("assessmentId") Long assessmentId);

    @Select("""
            SELECT
                event.id AS eventId,
                event.event_type AS eventType,
                event.student_id AS studentId,
                event_row.student_name_snapshot AS studentName,
                event.organization_name_snapshot AS organizationName,
                event.province_code_snapshot AS provinceCode,
                event.city_code_snapshot AS cityCode,
                event.district_code_snapshot AS districtCode,
                event.event_time AS eventTime,
                event.description AS description
            FROM exam_assessment_events event
            LEFT JOIN exam_assessment_students event_row
                ON event_row.assessment_id = event.assessment_id
               AND event_row.student_id = event.student_id
            WHERE event.assessment_id = #{assessmentId}
              AND event.event_type IN ('enter', 'submit', 'timeout', 'forced_submit')
            ORDER BY event.event_time DESC, event.id DESC
            LIMIT #{limit}
            """)
    List<AssessmentLatestEventResponse> selectLatestEvents(@Param("assessmentId") Long assessmentId, @Param("limit") long limit);

    @Select("""
            SELECT
                event.id AS eventId,
                event.event_type AS eventType,
                event.student_id AS studentId,
                event_row.student_name_snapshot AS studentName,
                event.organization_name_snapshot AS organizationName,
                event.province_code_snapshot AS provinceCode,
                event.city_code_snapshot AS cityCode,
                event.district_code_snapshot AS districtCode,
                event.event_time AS eventTime,
                event.description AS description
            FROM exam_assessment_events event
            LEFT JOIN exam_assessment_students event_row
                ON event_row.assessment_id = event.assessment_id
               AND event_row.student_id = event.student_id
            WHERE event.assessment_id = #{assessmentId}
              AND event.event_type IN ('enter', 'submit', 'timeout', 'forced_submit')
            ORDER BY event.event_time ASC, event.id ASC
            """)
    List<AssessmentLatestEventResponse> selectOrderedEvents(@Param("assessmentId") Long assessmentId);
}
