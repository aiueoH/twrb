package ah.twrbtest.DBObject;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BookableStation extends RealmObject {
    @PrimaryKey
    private String no;
    private String name;

    public static String getNameByNo(String no) {
        BookableStation bs = Realm.getDefaultInstance().where(BookableStation.class).equalTo("no", no).findFirst();
        return bs == null ? "" : bs.getName();
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
