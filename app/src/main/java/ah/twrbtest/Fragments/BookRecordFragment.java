package ah.twrbtest.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ah.twrbtest.BookRecordAdapter;
import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.Events.OnBookRecordAddedEvent;
import ah.twrbtest.Events.OnBookedEvent;
import ah.twrbtest.R;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;

public class BookRecordFragment extends Fragment {
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    private BookRecordAdapter bookRecordAdapter;
    private List<BookRecord> bookRecords;

    public static BookRecordFragment newInstance() {
        return new BookRecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.getDefaultInstance().refresh();
        RealmResults<BookRecord> rrs = Realm.getDefaultInstance().where(BookRecord.class).findAll();
        rrs.sort("id", RealmResults.SORT_ORDER_DESCENDING);
        this.bookRecords = new ArrayList<>();
        this.bookRecords.addAll(rrs);
        this.bookRecordAdapter = new BookRecordAdapter(getActivity(), this.bookRecords);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookrecord, container, false);
        ButterKnife.bind(this, view);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.recyclerView.setAdapter(this.bookRecordAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onEventMainThread(OnBookedEvent e) {
        long id = e.getBookRecordId();
        for (int i = 0; i < this.bookRecords.size(); i++)
            if (id == this.bookRecords.get(i).getId()) {
                this.bookRecordAdapter.notifyItemChanged(i);
                break;
            }
    }

    public void onEventMainThread(OnBookRecordAddedEvent e) {
        BookRecord br = BookRecord.get(e.getBookRecordId());
        bookRecords.add(0, br);
        this.bookRecordAdapter.notifyItemInserted(0);
    }
}