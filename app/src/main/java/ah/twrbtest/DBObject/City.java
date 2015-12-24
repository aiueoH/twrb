package ah.twrbtest.DBObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class City extends RealmObject {
    @PrimaryKey
    private String no;
    private String nameCh;
    private String nameEn;
    private RealmList<TimetableStation> timetableStations;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getNameCh() {
        return nameCh;
    }

    public void setNameCh(String nameCh) {
        this.nameCh = nameCh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public RealmList<TimetableStation> getTimetableStations() {
        return timetableStations;
    }

    public void setTimetableStations(RealmList<TimetableStation> timetableStations) {
        this.timetableStations = timetableStations;
    }
}
