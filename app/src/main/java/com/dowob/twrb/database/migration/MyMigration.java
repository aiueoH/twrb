package com.dowob.twrb.database.migration;

import com.twrb.core.MyLogger;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

public class MyMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        MyLogger.i(String.format("Migrate from %d to %d.", oldVersion, newVersion));
        if (oldVersion == 0) {
            new Migration0().migrate(realm.getSchema());
            oldVersion++;
        }
    }
}
