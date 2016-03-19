package com.dowob.twrb.features.tickets;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.events.OnBookRecordAddedEvent;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.book.RandInputDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookRecordFragment extends Fragment implements BookRecordModel.Observer {
    @Bind(R.id.textView_emptyMsg)
    TextView emptyMsg_textView;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    private BookRecordAdapter bookRecordAdapter;

    private ProgressDialog progressDialog;
    private long bookingRecordId;
    private int displayingItemIndex;
    private BookRecordModel bookRecordModel = BookRecordModel.getInstance();
    private List<BookRecord> bookRecords;
    private boolean isWaitingRandomInput = false;

    public static BookRecordFragment newInstance() {
        return new BookRecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookRecordModel.registerObserver(this);
        bookRecords = new ArrayList<>(bookRecordModel.getBookRecords());
        bookRecordAdapter = new BookRecordAdapter(getActivity(), bookRecords);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookrecord, container, false);
        ButterKnife.bind(this, view);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.recyclerView.setAdapter(this.bookRecordAdapter);
        // Add space at last item(buttom).
        this.recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position == state.getItemCount() - 1)
                    outRect.bottom = Math.round(20 * getResources().getDisplayMetrics().density);
            }
        });
        updateEmptyMsg();
        this.bookRecordAdapter.setParentView(this.recyclerView);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        bookRecordModel.unregisterObserver(this);
    }

    public void onEvent(BookRecordAdapter.OnBookEvent e) {
        bookingRecordId = e.getId();
        Observable.just(e.getId())
                .map(id -> bookRecordModel.book(getContext(), id))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> progressDialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.is_booking)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(captcha_stream -> {
                    progressDialog.dismiss();
                    if (captcha_stream == null) {
                        SnackbarHelper.show(recyclerView, "網路有些問題，再試一次看看", Snackbar.LENGTH_LONG);
                        return;
                    }
                    isWaitingRandomInput = true;
                    Bitmap captcha_bitmap = BitmapFactory.decodeByteArray(captcha_stream.toByteArray(), 0, captcha_stream.size());
                    RandInputDialog randInputDialog = new RandInputDialog(getContext(), captcha_bitmap);
                    randInputDialog.show();
                });
    }

    public void onEvent(RandInputDialog.OnSubmitEvent e) {
        if (!isWaitingRandomInput)
            return;
        isWaitingRandomInput = false;
        Observable.just(e.getRandInput())
                .map(randInput -> bookRecordModel.sendRandomInput(bookingRecordId, randInput))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> progressDialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.is_booking)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressDialog.dismiss();
                    String s = BookManager.getResultMsg(getContext(), result.getKey());
                    SnackbarHelper.show(recyclerView, s, Snackbar.LENGTH_LONG);
                });
    }

    public void onEventMainThread(OnBookRecordAddedEvent e) {
        BookRecord bookRecord = bookRecordModel.getBookRecord(e.getBookRecordId());
        bookRecords.add(0, bookRecord);
        this.bookRecordAdapter.notifyItemInserted(0);
        this.recyclerView.scrollToPosition(0);
        updateEmptyMsg();
    }

    public void onEventMainThread(ShowSnackbarEvent e) {
        e.getSnackbar().show();
    }

    public void updateEmptyMsg() {
        this.emptyMsg_textView.setVisibility(bookRecordModel.getBookRecords().isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void onEvent(BookRecordAdapter.OnDisplayItemDetailEvent e) {
        displayingItemIndex = e.getIndex();
    }

    @Override
    public void notifyBookRecordCreate() {
    }

    @Override
    public void notifyBookRecordUpdate(long bookRecordId) {
        getActivity().runOnUiThread(() -> {
            int index = findBookRecordIndexById(bookRecordId);
            if (index == -1) return;
            BookRecord bookRecord = bookRecordModel.getBookRecord(bookRecordId);
            if (bookRecord == null) return;
            bookRecords.set(index, bookRecord);
            bookRecordAdapter.notifyItemChanged(index);
        });
    }

    @Override
    public void notifyBookRecordRemove(long bookRecordId) {
        getActivity().runOnUiThread(() -> {
            int index = findBookRecordIndexById(bookRecordId);
            if (index == -1) return;
            bookRecords.remove(index);
            bookRecordAdapter.notifyItemRemoved(index);
            updateEmptyMsg();
        });
    }

    private int findBookRecordIndexById(final long bookingRecordId) {
        final List<Integer> index = new ArrayList<>();
        getActivity().runOnUiThread(() -> {
            for (int i = 0; i < bookRecords.size(); i++)
                if (bookRecords.get(i).getId() == bookingRecordId) {
                    index.add(i);
                    break;
                }
        });
        if (!index.isEmpty())
            return index.get(0);
        return -1;
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
