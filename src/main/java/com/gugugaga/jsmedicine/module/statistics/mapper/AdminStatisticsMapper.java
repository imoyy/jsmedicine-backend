package com.gugugaga.jsmedicine.module.statistics.mapper;

import com.gugugaga.jsmedicine.module.statistics.dto.ContentInteractionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamPaperScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamScoreSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.RegionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursRegionResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursResourceResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.TopicStudentStatisticsRecordResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.TopicStudentStatisticsSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminStatisticsMapper {

    @Select("""
            SELECT
                COUNT(1) AS recordCount,
                COUNT(DISTINCT lr.student_id) AS studentCount,
                COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completedCount,
                COALESCE(SUM(lr.study_seconds), 0) AS totalStudySeconds,
                ROUND(COALESCE(SUM(lr.study_seconds), 0) / 3600, 2) AS totalStudyHours,
                ROUND(COALESCE(AVG(lr.progress_percent), 0), 2) AS averageProgressPercent
            FROM learning_records lr
            JOIN students s ON s.id = lr.student_id AND s.deleted = 0
            WHERE lr.last_studied_at >= #{startAt}
              AND lr.last_studied_at < #{endAt}
              AND (#{resourceType} IS NULL OR lr.resource_type = #{resourceType})
              AND (#{resourceId} IS NULL OR lr.resource_id = #{resourceId})
              AND (#{studentId} IS NULL OR lr.student_id = #{studentId})
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            """)
    StudyHoursSummaryResponse selectStudyHoursSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            SELECT
                lr.resource_type AS resourceType,
                COUNT(1) AS recordCount,
                COUNT(DISTINCT lr.student_id) AS studentCount,
                COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completedCount,
                COALESCE(SUM(lr.study_seconds), 0) AS totalStudySeconds,
                ROUND(COALESCE(SUM(lr.study_seconds), 0) / 3600, 2) AS totalStudyHours,
                ROUND(COALESCE(AVG(lr.progress_percent), 0), 2) AS averageProgressPercent
            FROM learning_records lr
            JOIN students s ON s.id = lr.student_id AND s.deleted = 0
            WHERE lr.last_studied_at >= #{startAt}
              AND lr.last_studied_at < #{endAt}
              AND (#{resourceType} IS NULL OR lr.resource_type = #{resourceType})
              AND (#{studentId} IS NULL OR lr.student_id = #{studentId})
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            GROUP BY lr.resource_type
            ORDER BY totalStudySeconds DESC, recordCount DESC
            """)
    List<StudyHoursResourceResponse> selectStudyHoursByResource(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            <script>
            SELECT
                COALESCE(s.province, '') AS province,
                CASE
                    WHEN #{dimension} = 'province' THEN ''
                    ELSE COALESCE(s.city, '')
                END AS city,
                CASE
                    WHEN #{dimension} = 'district' THEN COALESCE(s.district, '')
                    ELSE ''
                END AS district,
                COUNT(DISTINCT lr.student_id) AS studentCount,
                COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completedCount,
                COALESCE(SUM(lr.study_seconds), 0) AS totalStudySeconds,
                ROUND(COALESCE(SUM(lr.study_seconds), 0) / 3600, 2) AS totalStudyHours,
                ROUND(
                    CASE WHEN COUNT(DISTINCT lr.student_id) = 0 THEN 0
                    ELSE (COALESCE(SUM(lr.study_seconds), 0) / 3600) / COUNT(DISTINCT lr.student_id)
                    END, 2
                ) AS averageStudyHours,
                ROUND(COALESCE(AVG(lr.progress_percent), 0), 2) AS averageProgressPercent
            FROM learning_records lr
            JOIN students s ON s.id = lr.student_id AND s.deleted = 0
            WHERE lr.last_studied_at <![CDATA[ >= ]]> #{startAt}
              AND lr.last_studied_at <![CDATA[ < ]]> #{endAt}
              AND (#{resourceType} IS NULL OR lr.resource_type = #{resourceType})
              AND (#{resourceId} IS NULL OR lr.resource_id = #{resourceId})
              AND (#{studentId} IS NULL OR lr.student_id = #{studentId})
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            GROUP BY
                COALESCE(s.province, ''),
                CASE WHEN #{dimension} = 'province' THEN '' ELSE COALESCE(s.city, '') END,
                CASE WHEN #{dimension} = 'district' THEN COALESCE(s.district, '') ELSE '' END
            ORDER BY totalStudySeconds DESC, province ASC, city ASC, district ASC
            </script>
            """)
    List<StudyHoursRegionResponse> selectStudyHoursByRegion(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("dimension") String dimension
    );

    @Select("""
            SELECT
                COUNT(1) AS totalStudents,
                COALESCE(SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), 0) AS enabledStudents,
                COALESCE(SUM(CASE WHEN s.certification_status = 2 THEN 1 ELSE 0 END), 0) AS approvedStudents,
                COALESCE(SUM(CASE WHEN s.certification_status = 1 THEN 1 ELSE 0 END), 0) AS pendingCertifications,
                COALESCE(SUM(CASE WHEN s.certification_status = 3 THEN 1 ELSE 0 END), 0) AS rejectedCertifications,
                COALESCE(SUM(CASE WHEN s.user_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS linkedUsers
            FROM students s
            WHERE s.deleted = 0
              AND s.created_at >= #{startAt}
              AND s.created_at < #{endAt}
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            """)
    StudentSummaryResponse selectStudentSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            SELECT
                COALESCE(s.province, '') AS province,
                COALESCE(s.city, '') AS city,
                COALESCE(s.district, '') AS district,
                COUNT(1) AS studentCount,
                COALESCE(SUM(CASE WHEN s.certification_status = 2 THEN 1 ELSE 0 END), 0) AS approvedStudentCount,
                COALESCE(SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), 0) AS enabledStudentCount,
                COALESCE(region_hours.completed_count, 0) AS completedCount,
                COALESCE(region_hours.total_study_seconds, 0) AS totalStudySeconds,
                ROUND(COALESCE(region_hours.total_study_seconds, 0) / 3600, 2) AS totalStudyHours,
                ROUND(
                    CASE WHEN COUNT(1) = 0 THEN 0
                    ELSE (COALESCE(region_hours.total_study_seconds, 0) / 3600) / COUNT(1)
                    END, 2
                ) AS averageStudyHours
            FROM students s
            LEFT JOIN (
                SELECT
                    COALESCE(st.province, '') AS province,
                    COALESCE(st.city, '') AS city,
                    COALESCE(st.district, '') AS district,
                    COALESCE(SUM(lr.study_seconds), 0) AS total_study_seconds,
                    COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_count
                FROM learning_records lr
                JOIN students st ON st.id = lr.student_id AND st.deleted = 0
                WHERE lr.last_studied_at >= #{startAt}
                  AND lr.last_studied_at < #{endAt}
                  AND (#{province} IS NULL OR st.province = #{province})
                  AND (#{city} IS NULL OR st.city = #{city})
                  AND (#{district} IS NULL OR st.district = #{district})
                GROUP BY COALESCE(st.province, ''), COALESCE(st.city, ''), COALESCE(st.district, '')
            ) region_hours
                ON region_hours.province = COALESCE(s.province, '')
               AND region_hours.city = COALESCE(s.city, '')
               AND region_hours.district = COALESCE(s.district, '')
            WHERE s.deleted = 0
              AND s.created_at >= #{startAt}
              AND s.created_at < #{endAt}
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            GROUP BY COALESCE(s.province, ''), COALESCE(s.city, ''), COALESCE(s.district, ''),
                     region_hours.completed_count, region_hours.total_study_seconds
            ORDER BY studentCount DESC, province ASC, city ASC, district ASC
            """)
    List<RegionStatisticsResponse> selectRegionStatistics(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            <script>
            SELECT
                COUNT(DISTINCT s.id) AS totalStudents,
                COUNT(DISTINCT CASE WHEN topic_learning.record_count > 0 THEN s.id END) AS topicStudents,
                COUNT(DISTINCT CASE
                    WHEN topic_learning.record_count > 0 AND topic_learning.completed_count = 0 THEN s.id
                    END) AS learningStudents,
                COUNT(DISTINCT CASE WHEN topic_learning.completed_count > 0 THEN s.id END) AS completedStudents,
                COUNT(DISTINCT CASE WHEN topic_learning.record_count IS NULL OR topic_learning.record_count = 0 THEN s.id END)
                    AS notStartedStudents
            FROM students s
            LEFT JOIN practice_types pt ON pt.id = s.practice_type_id AND pt.deleted = 0
            LEFT JOIN (
                SELECT
                    lr.student_id,
                    COUNT(1) AS record_count,
                    COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_count,
                    COALESCE(SUM(lr.study_seconds), 0) AS total_study_seconds
                FROM learning_records lr
                JOIN (
                    SELECT 'topic' AS resource_type, #{topicId} AS resource_id
                    UNION ALL
                    SELECT 'course' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                    UNION ALL
                    SELECT 'course_video' AS resource_type, cv.id AS resource_id
                    FROM topic_items ti
                    JOIN course_videos cv ON cv.course_id = ti.item_id AND cv.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                    UNION ALL
                    SELECT 'book' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                    UNION ALL
                    SELECT 'book_chapter' AS resource_type, bc.id AS resource_id
                    FROM topic_items ti
                    JOIN book_chapters bc ON bc.book_id = ti.item_id AND bc.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                    UNION ALL
                    SELECT 'podcast' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                    UNION ALL
                    SELECT 'podcast_audio' AS resource_type, pa.id AS resource_id
                    FROM topic_items ti
                    JOIN podcast_audios pa ON pa.podcast_id = ti.item_id AND pa.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                ) topic_resources
                    ON topic_resources.resource_type = lr.resource_type
                   AND topic_resources.resource_id = lr.resource_id
                WHERE lr.last_studied_at <![CDATA[ >= ]]> #{startAt}
                  AND lr.last_studied_at <![CDATA[ < ]]> #{endAt}
                GROUP BY lr.student_id
            ) topic_learning ON topic_learning.student_id = s.id
            WHERE s.deleted = 0
              AND (#{keyword} IS NULL OR s.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR s.mobile LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_no LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            </script>
            """)
    TopicStudentStatisticsSummaryResponse selectTopicStudentStatisticsSummary(
            @Param("topicId") Long topicId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM (
                SELECT s.id
                FROM students s
                LEFT JOIN (
                    SELECT
                        lr.student_id,
                        COUNT(1) AS record_count,
                        COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_count
                    FROM learning_records lr
                    JOIN (
                        SELECT 'topic' AS resource_type, #{topicId} AS resource_id
                        UNION ALL
                        SELECT 'course' AS resource_type, ti.item_id AS resource_id
                        FROM topic_items ti
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                        UNION ALL
                        SELECT 'course_video' AS resource_type, cv.id AS resource_id
                        FROM topic_items ti
                        JOIN course_videos cv ON cv.course_id = ti.item_id AND cv.deleted = 0
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                        UNION ALL
                        SELECT 'book' AS resource_type, ti.item_id AS resource_id
                        FROM topic_items ti
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                        UNION ALL
                        SELECT 'book_chapter' AS resource_type, bc.id AS resource_id
                        FROM topic_items ti
                        JOIN book_chapters bc ON bc.book_id = ti.item_id AND bc.deleted = 0
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                        UNION ALL
                        SELECT 'podcast' AS resource_type, ti.item_id AS resource_id
                        FROM topic_items ti
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                        UNION ALL
                        SELECT 'podcast_audio' AS resource_type, pa.id AS resource_id
                        FROM topic_items ti
                        JOIN podcast_audios pa ON pa.podcast_id = ti.item_id AND pa.deleted = 0
                        WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                    ) topic_resources
                        ON topic_resources.resource_type = lr.resource_type
                       AND topic_resources.resource_id = lr.resource_id
                    WHERE lr.last_studied_at <![CDATA[ >= ]]> #{startAt}
                      AND lr.last_studied_at <![CDATA[ < ]]> #{endAt}
                    GROUP BY lr.student_id
                ) topic_learning ON topic_learning.student_id = s.id
                WHERE s.deleted = 0
                  AND (#{keyword} IS NULL OR s.real_name LIKE CONCAT('%', #{keyword}, '%')
                       OR s.mobile LIKE CONCAT('%', #{keyword}, '%')
                       OR s.student_no LIKE CONCAT('%', #{keyword}, '%'))
                  AND (#{province} IS NULL OR s.province = #{province})
                  AND (#{city} IS NULL OR s.city = #{city})
                  AND (#{district} IS NULL OR s.district = #{district})
                  AND (
                       #{learningStatus} IS NULL
                       OR (#{learningStatus} = 'completed' AND topic_learning.completed_count > 0)
                       OR (#{learningStatus} = 'learning' AND topic_learning.record_count > 0 AND topic_learning.completed_count = 0)
                       OR (#{learningStatus} = 'not_started'
                           AND (topic_learning.record_count IS NULL OR topic_learning.record_count = 0))
                  )
            ) topic_student_records
            </script>
            """)
    long countTopicStudentStatisticsRecords(
            @Param("topicId") Long topicId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("learningStatus") String learningStatus
    );

    @Select("""
            <script>
            SELECT
                s.id AS studentId,
                s.student_no AS studentNo,
                s.real_name AS realName,
                s.gender AS gender,
                s.mobile AS mobile,
                s.age AS age,
                s.education_level AS educationLevel,
                s.organization AS organization,
                pt.type_name AS practiceTypeName,
                ROUND(COALESCE(topic_learning.total_study_seconds, 0) / 3600, 2) AS studyHours,
                CASE
                    WHEN topic_learning.completed_count > 0 THEN 'completed'
                    WHEN topic_learning.record_count > 0 THEN 'learning'
                    ELSE 'not_started'
                END AS topicLearningStatus,
                CASE
                    WHEN topic_learning.completed_count > 0 THEN '已完成'
                    WHEN topic_learning.record_count > 0 THEN '学习中'
                    ELSE '未开始'
                END AS topicLearningStatusLabel,
                CASE WHEN topic_learning.record_count > 0 THEN TRUE ELSE FALSE END AS isLearningCurrentTopic
            FROM students s
            LEFT JOIN practice_types pt ON pt.id = s.practice_type_id AND pt.deleted = 0
            LEFT JOIN (
                SELECT
                    lr.student_id,
                    COUNT(1) AS record_count,
                    COALESCE(SUM(CASE WHEN lr.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_count,
                    COALESCE(SUM(lr.study_seconds), 0) AS total_study_seconds
                FROM learning_records lr
                JOIN (
                    SELECT 'topic' AS resource_type, #{topicId} AS resource_id
                    UNION ALL
                    SELECT 'course' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                    UNION ALL
                    SELECT 'course_video' AS resource_type, cv.id AS resource_id
                    FROM topic_items ti
                    JOIN course_videos cv ON cv.course_id = ti.item_id AND cv.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'course'
                    UNION ALL
                    SELECT 'book' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                    UNION ALL
                    SELECT 'book_chapter' AS resource_type, bc.id AS resource_id
                    FROM topic_items ti
                    JOIN book_chapters bc ON bc.book_id = ti.item_id AND bc.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'book'
                    UNION ALL
                    SELECT 'podcast' AS resource_type, ti.item_id AS resource_id
                    FROM topic_items ti
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                    UNION ALL
                    SELECT 'podcast_audio' AS resource_type, pa.id AS resource_id
                    FROM topic_items ti
                    JOIN podcast_audios pa ON pa.podcast_id = ti.item_id AND pa.deleted = 0
                    WHERE ti.topic_id = #{topicId} AND ti.item_type = 'podcast'
                ) topic_resources
                    ON topic_resources.resource_type = lr.resource_type
                   AND topic_resources.resource_id = lr.resource_id
                WHERE lr.last_studied_at <![CDATA[ >= ]]> #{startAt}
                  AND lr.last_studied_at <![CDATA[ < ]]> #{endAt}
                GROUP BY lr.student_id
            ) topic_learning ON topic_learning.student_id = s.id
            WHERE s.deleted = 0
              AND (#{keyword} IS NULL OR s.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR s.mobile LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_no LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
              AND (
                   #{learningStatus} IS NULL
                   OR (#{learningStatus} = 'completed' AND topic_learning.completed_count > 0)
                   OR (#{learningStatus} = 'learning' AND topic_learning.record_count > 0 AND topic_learning.completed_count = 0)
                   OR (#{learningStatus} = 'not_started'
                       AND (topic_learning.record_count IS NULL OR topic_learning.record_count = 0))
              )
            ORDER BY studyHours DESC, s.id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<TopicStudentStatisticsRecordResponse> selectTopicStudentStatisticsRecords(
            @Param("topicId") Long topicId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("learningStatus") String learningStatus,
            @Param("offset") long offset,
            @Param("size") long size
    );

    @Select("""
            SELECT
                COUNT(1) AS examCount,
                COUNT(DISTINCT er.student_id) AS studentCount,
                COALESCE(SUM(CASE WHEN er.passed = 1 THEN 1 ELSE 0 END), 0) AS passedCount,
                ROUND(CASE WHEN COUNT(1) = 0 THEN 0 ELSE SUM(CASE WHEN er.passed = 1 THEN 1 ELSE 0 END) * 100 / COUNT(1) END, 2) AS passRatePercent,
                ROUND(COALESCE(AVG(er.score), 0), 2) AS averageScore,
                ROUND(COALESCE(MAX(er.score), 0), 2) AS maxScore,
                ROUND(COALESCE(MIN(er.score), 0), 2) AS minScore
            FROM exam_records er
            JOIN students s ON s.id = er.student_id AND s.deleted = 0
            WHERE er.submitted_at >= #{startAt}
              AND er.submitted_at < #{endAt}
              AND (#{resourceType} IS NULL OR er.source_type = #{resourceType})
              AND (#{resourceId} IS NULL OR er.source_id = #{resourceId})
              AND (#{studentId} IS NULL OR er.student_id = #{studentId})
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            """)
    ExamScoreSummaryResponse selectExamScoreSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            SELECT
                er.paper_id AS paperId,
                ep.paper_name AS paperTitle,
                COUNT(1) AS examCount,
                COUNT(DISTINCT er.student_id) AS studentCount,
                COALESCE(SUM(CASE WHEN er.passed = 1 THEN 1 ELSE 0 END), 0) AS passedCount,
                ROUND(CASE WHEN COUNT(1) = 0 THEN 0 ELSE SUM(CASE WHEN er.passed = 1 THEN 1 ELSE 0 END) * 100 / COUNT(1) END, 2) AS passRatePercent,
                ROUND(COALESCE(AVG(er.score), 0), 2) AS averageScore
            FROM exam_records er
            JOIN exam_papers ep ON ep.id = er.paper_id AND ep.deleted = 0
            JOIN students s ON s.id = er.student_id AND s.deleted = 0
            WHERE er.submitted_at >= #{startAt}
              AND er.submitted_at < #{endAt}
              AND (#{resourceType} IS NULL OR er.source_type = #{resourceType})
              AND (#{resourceId} IS NULL OR er.source_id = #{resourceId})
              AND (#{studentId} IS NULL OR er.student_id = #{studentId})
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
              AND (#{district} IS NULL OR s.district = #{district})
            GROUP BY er.paper_id, ep.paper_name
            ORDER BY examCount DESC, averageScore DESC
            """)
    List<ExamPaperScoreResponse> selectExamScoresByPaper(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district
    );

    @Select("""
            SELECT COUNT(1)
            FROM students s
            WHERE s.deleted = 0
              AND (#{keyword} IS NULL OR s.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR s.mobile LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_no LIKE CONCAT('%', #{keyword}, '%'))
            """)
    long countStudentScores(@Param("keyword") String keyword);

    @Select("""
            SELECT
                s.id AS studentId,
                s.student_no AS studentNo,
                s.real_name AS realName,
                s.gender AS gender,
                s.mobile AS mobile,
                s.age AS age,
                s.education_level AS educationLevel,
                s.organization AS organization,
                pt.type_name AS practiceTypeName,
                COALESCE(ssr.theory_training_status, 'none') AS theoryTrainingStatus,
                COALESCE(ssr.clinical_practice_status, 'none') AS clinicalPracticeStatus,
                COALESCE(ssr.practical_assessment_status, 'none') AS practicalAssessmentStatus,
                COALESCE(ssr.theory_assessment_status, 'none') AS theoryAssessmentStatus,
                COALESCE(ssr.online_training_status, 'none') AS onlineTrainingStatus
            FROM students s
            LEFT JOIN practice_types pt ON pt.id = s.practice_type_id AND pt.deleted = 0
            LEFT JOIN student_score_records ssr ON ssr.student_id = s.id AND ssr.deleted = 0
            WHERE s.deleted = 0
              AND (#{keyword} IS NULL OR s.real_name LIKE CONCAT('%', #{keyword}, '%')
                   OR s.mobile LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_no LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY s.id DESC
            LIMIT #{offset}, #{size}
            """)
    List<StudentScoreResponse> selectStudentScores(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    @Select("""
            SELECT
                s.id AS studentId,
                s.student_no AS studentNo,
                s.real_name AS realName,
                s.gender AS gender,
                s.mobile AS mobile,
                s.age AS age,
                s.education_level AS educationLevel,
                s.organization AS organization,
                pt.type_name AS practiceTypeName,
                COALESCE(ssr.theory_training_status, 'none') AS theoryTrainingStatus,
                COALESCE(ssr.clinical_practice_status, 'none') AS clinicalPracticeStatus,
                COALESCE(ssr.practical_assessment_status, 'none') AS practicalAssessmentStatus,
                COALESCE(ssr.theory_assessment_status, 'none') AS theoryAssessmentStatus,
                COALESCE(ssr.online_training_status, 'none') AS onlineTrainingStatus
            FROM students s
            LEFT JOIN practice_types pt ON pt.id = s.practice_type_id AND pt.deleted = 0
            LEFT JOIN student_score_records ssr ON ssr.student_id = s.id AND ssr.deleted = 0
            WHERE s.deleted = 0
              AND s.id = #{studentId}
            LIMIT 1
            """)
    List<StudentScoreResponse> selectStudentScoresByStudentId(@Param("studentId") Long studentId);

    @Select("""
            SELECT
                resource_type AS resourceType,
                resource_id AS resourceId,
                SUM(browse_count) AS browseCount,
                SUM(favorite_count) AS favoriteCount,
                SUM(share_count) AS shareCount,
                SUM(unique_browse_users) AS uniqueBrowseUsers,
                SUM(unique_favorite_users) AS uniqueFavoriteUsers,
                SUM(unique_share_users) AS uniqueShareUsers
            FROM (
                SELECT
                    ubh.resource_type,
                    ubh.resource_id,
                    COALESCE(SUM(ubh.view_count), 0) AS browse_count,
                    0 AS favorite_count,
                    0 AS share_count,
                    COUNT(DISTINCT ubh.user_id) AS unique_browse_users,
                    0 AS unique_favorite_users,
                    0 AS unique_share_users
                FROM user_browse_histories ubh
                WHERE ubh.viewed_at >= #{startAt}
                  AND ubh.viewed_at < #{endAt}
                  AND (#{resourceType} IS NULL OR ubh.resource_type = #{resourceType})
                  AND (#{resourceId} IS NULL OR ubh.resource_id = #{resourceId})
                GROUP BY ubh.resource_type, ubh.resource_id
                UNION ALL
                SELECT
                    uf.resource_type,
                    uf.resource_id,
                    0 AS browse_count,
                    COUNT(1) AS favorite_count,
                    0 AS share_count,
                    0 AS unique_browse_users,
                    COUNT(DISTINCT uf.user_id) AS unique_favorite_users,
                    0 AS unique_share_users
                FROM user_favorites uf
                WHERE uf.created_at >= #{startAt}
                  AND uf.created_at < #{endAt}
                  AND (#{resourceType} IS NULL OR uf.resource_type = #{resourceType})
                  AND (#{resourceId} IS NULL OR uf.resource_id = #{resourceId})
                GROUP BY uf.resource_type, uf.resource_id
                UNION ALL
                SELECT
                    usr.resource_type,
                    usr.resource_id,
                    0 AS browse_count,
                    0 AS favorite_count,
                    COUNT(1) AS share_count,
                    0 AS unique_browse_users,
                    0 AS unique_favorite_users,
                    COUNT(DISTINCT usr.user_id) AS unique_share_users
                FROM user_share_records usr
                WHERE usr.created_at >= #{startAt}
                  AND usr.created_at < #{endAt}
                  AND (#{resourceType} IS NULL OR usr.resource_type = #{resourceType})
                  AND (#{resourceId} IS NULL OR usr.resource_id = #{resourceId})
                GROUP BY usr.resource_type, usr.resource_id
            ) interaction_data
            GROUP BY resource_type, resource_id
            ORDER BY browseCount DESC, favoriteCount DESC, shareCount DESC
            """)
    List<ContentInteractionStatisticsResponse> selectContentInteractionStatistics(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId
    );
}
