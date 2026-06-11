INSERT INTO experts (
    user_id,
    real_name,
    gender,
    mobile,
    avatar_url,
    organization,
    organization_id,
    practice_type_id,
    status,
    consult_enabled,
    consultation_notice,
    sort_order,
    created_by,
    updated_by,
    deleted
)
SELECT
    identities.user_id,
    COALESCE(NULLIF(student.real_name, ''), NULLIF(app_user.nickname, ''), NULLIF(app_user.username, ''), CONCAT('Expert-', identities.user_id)),
    app_user.gender,
    app_user.mobile,
    app_user.avatar_url,
    student.organization,
    student.organization_id,
    student.practice_type_id,
    1,
    0,
    NULL,
    0,
    0,
    0,
    0
FROM app_user_identities identities
JOIN app_users app_user
  ON app_user.id = identities.user_id
 AND app_user.deleted = 0
LEFT JOIN students student
  ON student.user_id = identities.user_id
 AND student.deleted = 0
LEFT JOIN experts expert
  ON expert.user_id = identities.user_id
 AND expert.deleted = 0
WHERE identities.identity_type = 'EXPERT'
  AND identities.identity_status = 1
  AND identities.deleted = 0
  AND expert.id IS NULL;
