package com.dowob.twrb.database.migration;


import com.twrb.core.MyLogger;

import java.util.Date;

import io.realm.FieldAttribute;
import io.realm.RealmSchema;

class Migration0 {
    public void migrate(RealmSchema schema) {
        schema.create("TrainInfo")
                .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                .addField("departureDateTime", Date.class, FieldAttribute.REQUIRED)
                .addField("arrivalDateTime", Date.class, FieldAttribute.REQUIRED)
                .addField("departureStation", String.class, FieldAttribute.REQUIRED)
                .addField("destination", String.class, FieldAttribute.REQUIRED)
                .addField("trainType", String.class, FieldAttribute.REQUIRED)
                .addField("way", String.class, FieldAttribute.REQUIRED)
                .addField("remarks", String.class, FieldAttribute.REQUIRED)
                .addField("fares", int.class)
                .addField("everyday", boolean.class)
                .addField("handicapped", boolean.class)
                .addField("bike", boolean.class)
                .addField("breastfeeding", boolean.class)
                .addField("acrossNight", boolean.class);
        schema.get("BookRecord")
                .addRealmObjectField("trainInfo", schema.get("TrainInfo"))
                .addField("orderQtu", int.class)
                .transform(obj -> {
                    obj.setInt("orderQtu", Integer.parseInt(obj.getString("orderQtuStr")));
                    obj.setNull("trainInfo");
                })
                .removeField("orderQtuStr");
        MyLogger.i("Done Migration0.");
    }
}
