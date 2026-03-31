USE `jooq_db`;

-- ==============================================================
-- Helper functions required by SP_GetDocumentsForFileProcessing_genAi_multi_company
-- (kept identical in name/signature to existing individual files)
-- ==============================================================

-- 1) fn_getUserId
DELIMITER $$
DROP FUNCTION IF EXISTS fn_getUserId$$
CREATE FUNCTION fn_getUserId(p_email VARCHAR(255))
    RETURNS VARCHAR(36)
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_user_id VARCHAR(36);

    SELECT BIN_TO_UUID(u.id)
    INTO v_user_id
    FROM users u
    WHERE LOWER(u.email) = LOWER(TRIM(p_email))
      AND (u.is_active = 1 OR u.is_active IS NULL)
    LIMIT 1;

    RETURN v_user_id;
END$$
DELIMITER ;

-- 2) IS_SuperUser
DELIMITER $$
DROP FUNCTION IF EXISTS IS_SuperUser$$
CREATE FUNCTION IS_SuperUser(p_user_id BINARY(16))
    RETURNS INT
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_is_admin INT DEFAULT 0;

    SELECT CASE WHEN COALESCE(u.is_super_user, 0) = 1 THEN 1 ELSE 0 END
    INTO v_is_admin
    FROM users u
    WHERE u.id = p_user_id
    LIMIT 1;

    RETURN COALESCE(v_is_admin, 0);
END$$
DELIMITER ;

-- 3) fn_isPGManagerForCompany
DELIMITER $$
DROP FUNCTION IF EXISTS fn_isPGManagerForCompany$$
CREATE FUNCTION fn_isPGManagerForCompany(
    p_user_id_str VARCHAR(36),
    p_company_id_str VARCHAR(36)
)
    RETURNS INT
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_is_manager INT DEFAULT 0;
    DECLARE v_user_id_bin BINARY(16);
    DECLARE v_company_id_bin BINARY(16);

    SET v_user_id_bin = UUID_TO_BIN(p_user_id_str);
    SET v_company_id_bin = UUID_TO_BIN(p_company_id_str);

    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM user_company uc
        WHERE uc.user_id = v_user_id_bin
          AND uc.company_id = v_company_id_bin
          AND uc.is_active = 1
          AND uc.role IN ('PG_SENIOR_MANAGER', 'PG_MANAGER')
    ) THEN 1 ELSE 0 END
    INTO v_is_manager;

    RETURN v_is_manager;
END$$
DELIMITER ;

