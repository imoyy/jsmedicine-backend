# Database Schema

This schema is the first production-oriented baseline derived from `AGENTS.md` and the management manual.

Java entity classes under `src/main/java/com/gugugaga/jsmedicine/**/entity` are the application model source. The Flyway migration is the deployment artifact that keeps runtime databases aligned with those entities.

## Module Mapping

| Module | Tables |
| --- | --- |
| Login, admin, roles, permissions | `sys_admins`, `sys_roles`, `sys_permissions`, `sys_admin_roles`, `sys_role_permissions` |
| Audit workflow | `audit_records` |
| User and student management | `app_users`, `students` |
| Uploads and media references | `file_assets` |
| Home management | `home_categories`, `home_contents` |
| Course management | `courses`, `course_videos`, `exam_papers`, `exam_paper_questions` |
| Book management | `book_categories`, `books`, `book_chapters`, `exam_papers`, `exam_paper_questions` |
| Articles | `articles` |
| Podcasts | `podcasts`, `podcast_audios` |
| Topics | `topics`, `topic_items` |
| Experts | `expert_categories`, `experts`, `expert_category_relations`, `expert_experiences` |
| Question bank | `question_categories`, `questions`, `question_options` |
| Statistics | `learning_records`, `exam_records`, `exam_record_answers`, `students` |
| Live management | `live_sessions` |
| QA | `qa_questions`, `qa_answers` |
| Feedback | `feedbacks` |

## Conventions

- `deleted` is the MyBatis-Plus logic delete field.
- Managed entities inherit `id`, audit fields, timestamps, and logical deletion from common base classes.
- `review_status` uses `0 draft, 1 pending, 2 approved, 3 rejected`.
- `publish_status` uses `0 unpublished, 1 published`.
- `status` uses `1 enabled, 0 disabled` unless the column comment states otherwise.
- Cross-resource configuration such as home content and topic content uses `(type, id)` pairs to avoid heavy join-table growth while keeping queries explicit.
- File metadata is centralized in `file_assets`; business tables keep URL fields for simple read paths and future migration compatibility.
- `tags`, `resource_tags`, and `entity_extensions` provide controlled extension points for new filtering, operation labels, and fields that appear after the first release.
