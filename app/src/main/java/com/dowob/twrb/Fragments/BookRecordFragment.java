package com.dowob.twrb.Fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dowob.twrb.BookRecordAdapter;
import com.dowob.twrb.DBObject.BookRecord;
import com.dowob.twrb.Events.OnBookRecordAddedEvent;
import com.dowob.twrb.Events.OnBookRecordRemovedEvent;
import com.dowob.twrb.Events.OnBookedEvent;
import com.dowob.twrb.Helper.BookManager;
import com.dowob.twrb.R;
import com.dowob.twrb.RandInputDialog;
import com.dowob.twrb.SnackbarHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookRecordFragment extends Fragment {
    @Bind(R.id.textView_emptyMsg)
    TextView emptyMsg_textView;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    private BookRecordAdapter bookRecordAdapter;
    private List<BookRecord> bookRecords;

    private ProgressDialog mProgressDialog;
    private BookManager bookManager;
    private long bookingRecordId;

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
        updateEmptyMsg();
        this.bookRecordAdapter.setParentView(this.recyclerView);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        bookManager = null;
    }

    public void onEvent(BookRecordAdapter.OnBookEvent e) {
        bookingRecordId = e.getId();
        bookManager = new BookManager();
        Observable.just(e.getId())
                .map(id -> bookManager.step1(getContext(), id))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> mProgressDialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.is_booking)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(captcha_stream -> {
                    mProgressDialog.dismiss();
                    Bitmap captcha_bitmap = BitmapFactory.decodeByteArray(captcha_stream.toByteArray(), 0, captcha_stream.size());
                    RandInputDialog randInputDialog = new RandInputDialog(getContext(), captcha_bitmap);
                    randInputDialog.show();
                });
    }

    public void onEvent(RandInputDialog.OnSubmitEvent e) {
        if (bookManager == null)
            return;
        Observable.just(e.getRandInput())
                .map(randInput -> bookManager.step2(bookingRecordId, randInput))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> mProgressDialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.is_booking)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mProgressDialog.dismiss();
                    String s = BookManager.getResultMsg(getContext(), result.getKey());
                    SnackbarHelper.show(recyclerView, s, Snackbar.LENGTH_LONG);
                });
    }

    public void onEventMainThread(OnBookedEvent e) {
        long id = e.getBookRecordId();
        for (int i = 0; i < this.bookRecords.size(); i++)
            if (id == this.bookRecords.get(i).getId()) {
                this.bookRecordAdapter.notifyItemChanged(i);
                this.recyclerView.scrollToPosition(i);
                break;
            }
    }

    public void onEventMainThread(OnBookRecordAddedEvent e) {
        BookRecord br = BookRecord.get(e.getBookRecordId());
        bookRecords.add(0, br);
        this.bookRecordAdapter.notifyItemInserted(0);
        this.recyclerView.scrollToPosition(0);
        updateEmptyMsg();
    }

    public void onEventMainThread(OnBookRecordRemovedEvent e) {
        updateEmptyMsg();
    }

    public void onEventMainThread(ShowSnackbarEvent e) {
        e.getSnackbar().show();
    }

    public void updateEmptyMsg() {
        this.emptyMsg_textView.setVisibility(this.bookRecords.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public static class ShowSnackbarEvent {
        private Snackbar snackbar;

        public ShowSnackbarEvent(@NonNull Snackbar snackbar) {
            this.snackbar = snackbar;
        }

        @NonNull
        public Snackbar getSnackbar() {
            return snackbar;
        }
    }
}
