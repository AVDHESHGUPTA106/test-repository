package com.sivalabs.bookmarks.repositories;

import com.sivalabs.bookmarks.services.FileProcessingDocumentDTO;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository implementation that replicates the logic of
 * SP_GetDocumentsForFileProcessing_genAi_multi_company using jOOQ fluent DSL.
 *
 * <p>
 * NOTE: This repository uses plain SQL table/field references (DSL.table/DSL.field)
 * instead of generated jOOQ classes, because these tables may not be part of the
 * currently generated schema in this demo project.
 */
@Repository
public class FileProcessingRepository {

    private static final Table<?> USERS = DSL.table(DSL.name("users"));
    private static final Field<String> USERS_EMAIL = DSL.field(DSL.name("users", "email"), String.class);
    private static final Field<byte[]> USERS_ID = DSL.field(DSL.name("users", "id"), byte[].class);
    private static final Field<Integer> USERS_IS_ACTIVE = DSL.field(DSL.name("users", "is_active"), Integer.class);
    private static final Field<Integer> USERS_IS_SUPER_USER = DSL.field(DSL.name("users", "is_super_user"), Integer.class);

    private static final Table<?> USER_COMPANY = DSL.table(DSL.name("user_company"));
    private static final Field<byte[]> UC_USER_ID = DSL.field(DSL.name("user_company", "user_id"), byte[].class);
    private static final Field<byte[]> UC_COMPANY_ID = DSL.field(DSL.name("user_company", "company_id"), byte[].class);
    private static final Field<Integer> UC_IS_ACTIVE = DSL.field(DSL.name("user_company", "is_active"), Integer.class);
    private static final Field<String> UC_ROLE = DSL.field(DSL.name("user_company", "role"), String.class);

    private static final Table<?> COMPANY = DSL.table(DSL.name("company"));
    private static final Field<byte[]> COMPANY_ID = DSL.field(DSL.name("company", "id"), byte[].class);
    private static final Field<String> COMPANY_DISPLAY_NAME = DSL.field(DSL.name("company", "display_name"), String.class);
    private static final Field<Integer> COMPANY_IS_ACTIVE = DSL.field(DSL.name("company", "is_active"), Integer.class);

    private static final Table<?> DOCUMENT = DSL.table(DSL.name("document"));
    private static final Field<byte[]> DOC_ID = DSL.field(DSL.name("document", "id"), byte[].class);
    private static final Field<byte[]> DOC_COMPANY_ID = DSL.field(DSL.name("document", "company_id"), byte[].class);
    private static final Field<Integer> DOC_IS_ACTIVE = DSL.field(DSL.name("document", "is_active"), Integer.class);
    private static final Field<byte[]> DOC_OVERWRITTEN_BY = DSL.field(DSL.name("document", "overwritten_by"), byte[].class);
    private static final Field<String> DOC_DOCUMENT_TYPE = DSL.field(DSL.name("document", "document_type"), String.class);
    private static final Field<java.time.LocalDateTime> DOC_CREATED_DATE = DSL.field(DSL.name("document", "created_date"), java.time.LocalDateTime.class);

    private static final Table<?> USER_DOCUMENT_PERMISSION = DSL.table(DSL.name("user_document_permission"));
    private static final Field<byte[]> UDP_DOCUMENT_ID = DSL.field(DSL.name("user_document_permission", "document_id"), byte[].class);
    private static final Field<byte[]> UDP_TYPE_ID = DSL.field(DSL.name("user_document_permission", "type_id"), byte[].class);
    private static final Field<Integer> UDP_IS_ACTIVE = DSL.field(DSL.name("user_document_permission", "is_active"), Integer.class);

    private final DSLContext dsl;

    public FileProcessingRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public byte[] findUserIdBinByEmail(String email) {
        return dsl.select(USERS_ID)
            .from(USERS)
            .where(DSL.lower(USERS_EMAIL).eq(email == null ? null : email.trim().toLowerCase()))
            .and(USERS_IS_ACTIVE.eq(1))
            .limit(1)
            .fetchOne(USERS_ID);
    }

    public boolean isSuperUser(byte[] userIdBin) {
        Integer flag = dsl.select(USERS_IS_SUPER_USER)
            .from(USERS)
            .where(USERS_ID.eq(userIdBin))
            .limit(1)
            .fetchOne(USERS_IS_SUPER_USER);
        return flag != null && flag == 1;
    }

    public boolean isPgManagerForCompany(byte[] userIdBin, byte[] companyIdBin) {
        // Matches: role IN ('PG_SENIOR_MANAGER', 'PG_MANAGER') AND is_active=1
        Integer exists = dsl.selectOne()
            .from(USER_COMPANY)
            .where(UC_USER_ID.eq(userIdBin))
            .and(UC_COMPANY_ID.eq(companyIdBin))
            .and(UC_IS_ACTIVE.eq(1))
            .and(UC_ROLE.in("PG_SENIOR_MANAGER", "PG_MANAGER"))
            .fetchOptional()
            .map(r -> 1)
            .orElse(0);
        return exists == 1;
    }

    public boolean isUserInCompany(byte[] userIdBin, byte[] companyIdBin) {
        Integer count = dsl.selectCount()
            .from(USER_COMPANY)
            .where(UC_USER_ID.eq(userIdBin))
            .and(UC_COMPANY_ID.eq(companyIdBin))
            .and(UC_IS_ACTIVE.eq(1))
            .fetchOne(0, int.class);
        return count != null && count > 0;
    }

    public List<FileProcessingDocumentDTO> fetchDocumentsForCompany(
        byte[] userIdBin,
        boolean isAdminOrManager,
        boolean isUserInCompany,
        byte[] companyIdBin,
        LocalDate startDate,
        LocalDate endDate
    ) {
        // Convert date range (DATE in SP) to [start 00:00, end +1day 00:00)
        var start = startDate.atStartOfDay();
        var endExclusive = endDate.plusDays(1).atStartOfDay();

        var baseDocCondition = DOC_COMPANY_ID.eq(companyIdBin)
            .and(DOC_IS_ACTIVE.eq(1))
            .and(DOC_OVERWRITTEN_BY.isNull())
            .and(DOC_DOCUMENT_TYPE.eq("shared_file"))
            .and(DOC_CREATED_DATE.ge(start))
            .and(DOC_CREATED_DATE.lt(endExclusive));

        var allowedDocCondition = DSL.condition(false);
        if (isAdminOrManager) {
            allowedDocCondition = DSL.trueCondition();
        } else if (isUserInCompany) {
            allowedDocCondition = DSL.exists(
                dsl.selectOne()
                    .from(USER_DOCUMENT_PERMISSION)
                    .where(UDP_DOCUMENT_ID.eq(DOC_ID))
                    .and(UDP_TYPE_ID.eq(userIdBin))
                    .and(UDP_IS_ACTIVE.eq(1))
            );
        }

        Field<String> docIdStr = DSL.field("BIN_TO_UUID({0})", String.class, DOC_ID).as("document_id");
        Field<String> companyIdStr = DSL.field("BIN_TO_UUID({0})", String.class, COMPANY_ID).as("company_id");

        return dsl.select(
                docIdStr,
                companyIdStr,
                COMPANY_DISPLAY_NAME
            )
            .from(COMPANY)
            .leftJoin(DOCUMENT).on(DOC_COMPANY_ID.eq(COMPANY_ID).and(baseDocCondition).and(allowedDocCondition))
            .where(COMPANY_ID.eq(companyIdBin))
            .and(COMPANY_IS_ACTIVE.eq(1))
            .fetch(record -> new FileProcessingDocumentDTO(
                record.get(docIdStr),
                record.get(companyIdStr),
                record.get(COMPANY_DISPLAY_NAME)
            ));
    }
}


