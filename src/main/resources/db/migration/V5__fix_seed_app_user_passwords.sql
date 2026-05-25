UPDATE app_users
SET password_hash = '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.',
    password_updated_at = COALESCE(password_updated_at, NOW())
WHERE username IN ('td_user_01', 'td_user_02')
  AND deleted = 0;
