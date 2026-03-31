package com.sivalabs.bookmarks.repositories;

import com.sivalabs.bookmarks.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.sivalabs.bookmarks.jooq.tables.Users.USERS;

@Repository
public class UserRepository {
    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public String findUserNameById(Long id) {
        UsersRecord usersRecord = dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional().orElseThrow();
        return usersRecord.getName();
    }
}
