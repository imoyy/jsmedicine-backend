# Database Schema

This schema is the production-oriented baseline derived from `AGENTS.md`, the management manual, and the app user manual.

Java entity classes under `src/main/java/com/gugugaga/jsmedicine/**/entity` are the application model source. The Flyway migration is the deployment artifact that keeps runtime databases aligned with those entities.

## Module Mapping

| Module | Tables |
| --- | --- |
| Login, admin, roles, permissions | `sys_admins`, `sys_roles`, `sys_permissions`, `sys_admin_roles`, `sys_role_permissions` |
| Audit workflow | `audit_records` |
| User and student management | `app_users`, `app_user_identities`, `students`, `organizations`, `practice_types`, `student_certification_files` |
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
| QA and consultation | `qa_questions`, `qa_answers` |
| User interaction | `user_favorites`, `user_browse_histories`, `user_share_records`, `feedbacks` |
| Knowledge base | `knowledge_categories`, `knowledge_entries` |

## Conventions

- `deleted` is the MyBatis-Plus logic delete field.
- Managed entities inherit `id`, audit fields, timestamps, and logical deletion from common base classes.
- `review_status` uses `0 draft, 1 pending, 2 approved, 3 rejected`.
- `publish_status` uses `0 unpublished, 1 published`.
- `status` uses `1 enabled, 0 disabled` unless the column comment states otherwise.
- `app_users` now keeps user-side auth provider metadata and WeChat identity fields for future mini app login.
- `app_users.profile_signature` stores the user-facing signature text shown in profile and management scenarios.
- `app_users.password_hash` is reserved for user-side username/password login and stays independent from the admin account system.
- `app_user_identities` is the normalized business identity relation for front-end users; it does not replace admin-side RBAC roles.
- `students` now carries the certification workflow status instead of only storing approved learner results.
- `students.user_id` is expected to be unique after account-domain normalization so one app user maps to at most one learner archive.
- `students` is being normalized with region codes, organization references, and practice-type references; display text fields are temporarily retained for transition compatibility.
- `experts.user_id` is nullable for legacy expert master data, but when populated it represents the bound app user account.
- `organizations` and `practice_types` are the new reference tables for account-domain normalization and should replace new free-text writes over time.
- `student_certification_files` is the structured storage for learner certification materials; `students.certification_materials` is retained only as a compatibility field.
- Cross-resource configuration such as home content and topic content uses `(type, id)` pairs to avoid heavy join-table growth while keeping queries explicit.
- User-side favorites, browse history, and share records also use `(resource_type, resource_id)` pairs so the same interaction tables can serve articles, topics, courses, books, podcasts, live sessions, and knowledge entries.
- File metadata is centralized in `file_assets`; business tables keep URL fields for simple read paths and future migration compatibility.
- `tags`, `resource_tags`, and `entity_extensions` provide controlled extension points for new filtering, operation labels, and fields that appear after the first release.
