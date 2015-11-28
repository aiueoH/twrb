package ah.twrbtest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.MyArrayAdapter.BookRecordArrayAdapter;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class BookRecordFragment extends Fragment {

    private BookRecordArrayAdapter bookRecordArrayAdapter;

    private ListView listView;

    public static BookRecordFragment newInstance() {
        return new BookRecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RealmResults<BookRecord> rrs = Realm.getDefaultInstance().where(BookRecord.class).findAll();
        rrs.sort("id", RealmResults.SORT_ORDER_DESCENDING);
        List<BookRecord> brs = new ArrayList<>();
        for (BookRecord br : rrs)
            brs.add(br);
        this.bookRecordArrayAdapter = new BookRecordArrayAdapter(getActivity(), R.layout.item_bookrecord, brs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookrecord, container, false);
        this.listView = ButterKnife.findById(view, R.id.listView);
        this.listView.setAdapter(this.bookRecordArrayAdapter);
        return view;
    }
}
