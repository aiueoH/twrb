package com.dowob.twrb.database.migration;


import com.twrb.core.MyLogger;

import java.util.Date;

import io.realm.RealmSchema;

class Migration0 {
    public void migrate(RealmSchema schema) {
        schema.get("BookRecord")
                .addField("departureDateTime", Date.class)
                .addField("arrivalDateTime", Date.class)
                .addField("departureStation", String.class)
                .addField("destination", String.class)
                .addField("trainType", String.class)
                .addField("way", String.class)
                .addField("remarks", String.class)
                .addField("fares", int.class)
                .addField("everyday", boolean.class)
                .addField("handicapped", boolean.class)
                .addField("bike", boolean.class)
                .addField("breastfeeding", boolean.class)
                .addField("acrossNight", boolean.class)
                .addField("orderQtu", int.class)
                .transform(obj -> obj.setInt("orderQtu", Integer.parseInt(obj.getString("orderQtuStr"))))
                .removeField("orderQtuStr");
        MyLogger.i("Done Migration0.");
    }
}
