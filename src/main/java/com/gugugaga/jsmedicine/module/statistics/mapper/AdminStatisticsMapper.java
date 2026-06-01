package com.gugugaga.jsmedicine.module.statistics.mapper;

import com.gugugaga.jsmedicine.module.statistics.dto.ContentInteractionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamPaperScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamScoreSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.RegionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursResourceResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursSummaryResponse;
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
            """)
    StudyHoursSummaryResponse selectStudyHoursSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city
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
            GROUP BY lr.resource_type
            ORDER BY totalStudySeconds DESC, recordCount DESC
            """)
    List<StudyHoursResourceResponse> selectStudyHoursByResource(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city
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
            """)
    StudentSummaryResponse selectStudentSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("province") String province,
            @Param("city") String city
    );

    @Select("""
            SELECT
                COALESCE(s.province, '') AS province,
                COALESCE(s.city, '') AS city,
                COUNT(1) AS studentCount,
                COALESCE(SUM(CASE WHEN s.certification_status = 2 THEN 1 ELSE 0 END), 0) AS approvedStudentCount,
                COALESCE(SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), 0) AS enabledStudentCount
            FROM students s
            WHERE s.deleted = 0
              AND s.created_at >= #{startAt}
              AND s.created_at < #{endAt}
              AND (#{province} IS NULL OR s.province = #{province})
              AND (#{city} IS NULL OR s.city = #{city})
            GROUP BY COALESCE(s.province, ''), COALESCE(s.city, '')
            ORDER BY studentCount DESC, province ASC, city ASC
            """)
    List<RegionStatisticsResponse> selectRegionStatistics(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("province") String province,
            @Param("city") String city
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
            """)
    ExamScoreSummaryResponse selectExamScoreSummary(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city
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
            GROUP BY er.paper_id, ep.title
            ORDER BY examCount DESC, averageScore DESC
            """)
    List<ExamPaperScoreResponse> selectExamScoresByPaper(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("province") String province,
            @Param("city") String city
    );

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
