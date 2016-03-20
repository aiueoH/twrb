package com.dowob.twrb.features.tickets;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dowob.twrb.R;
import com.dowob.twrb.database.BookRecord;
import com.dowob.twrb.features.shared.NetworkChecker;
import com.dowob.twrb.features.shared.SnackbarHelper;
import com.dowob.twrb.features.tickets.book.Booker;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
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

    private View parentView;
    private BookRecordAdapter bookRecordAdapter;
    private ProgressDialog progressDialog;
    private long bookingRecordId;
    private BookRecordModel bookRecordModel = BookRecordModel.getInstance();
    private List<BookRecord> bookRecords;

    public static BookRecordFragment newInstance() {
        return new BookRecordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookRecordModel.registerObserver(this);
        bookRecords = new ArrayList<>(bookRecordModel.getBookRecords());
        bookRecordAdapter = new BookRecordAdapter.Builder()
                .setBookRecords(bookRecords)
                .setOnBookButtonClickListener(this::onBookRecordBookButtonClick)
                .setOnItemClickListener(this::onBookRecordItemClick)
                .createBookRecordAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookrecord, container, false);
        ButterKnife.bind(this, view);
        parentView = recyclerView;
        setUpRecyclerView();
        updateEmptyMsg();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bookRecordModel.unregisterObserver(this);
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(bookRecordAdapter);
        // Add space at last item(buttom).
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position == 0)
                    outRect.top = Math.round(30 * getResources().getDisplayMetrics().density);
                if (position == state.getItemCount() - 1)
                    outRect.bottom = Math.round(30 * getResources().getDisplayMetrics().density);
            }
        });
    }

    private void onBookRecordItemClick(View view, int which) {
        Intent intent = new Intent(getContext(), BookRecordActivity.class);
        EventBus.getDefault().postSticky(new BookRecordActivity.Data(bookRecords.get(which)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View mainSpace = view.findViewById(R.id.linearLayout_mainSpace);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    Pair.create(mainSpace, mainSpace.getTransitionName())
            );
            this.startActivity(intent, options.toBundle());
        } else {
            this.startActivity(intent);
        }
    }

    private void onBookRecordBookButtonClick(View view, int which) {
        book(bookRecords.get(which).getId());
    }

    private void book(long bookRecordId) {
        if (!NetworkChecker.isConnected(getContext())) {
            SnackbarHelper.show(parentView, getString(R.string.network_not_connected), Snackbar.LENGTH_LONG);
            return;
        }
        bookingRecordId = bookRecordId;
        Observable.just(bookRecordId)
                .map(id -> bookRecordModel.book(getContext(), id))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetCaptcha);
    }

    private void onGetCaptcha(ByteArrayOutputStream captcha) {
        dismissProgressDialog();
        if (captcha == null) {
            SnackbarHelper.show(recyclerView, getString(R.string.book_unknown), Snackbar.LENGTH_LONG);
            return;
        }
        Bitmap captcha_bitmap = BitmapFactory.decodeByteArray(captcha.toByteArray(), 0, captcha.size());
        showRequireRandomInputDialog(captcha_bitmap);
    }

    private void showRequireRandomInputDialog(Bitmap captcha) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.require_randominput, null);
        ImageView captcha_imageView = (ImageView) view.findViewById(R.id.imageView_captcha);
        captcha_imageView.setImageBitmap(captcha);
        new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("送出", (dialog, which) -> {
                    EditText editText = (EditText) view.findViewById(R.id.editText_randInput);
                    sendRandomInput(editText.getText().toString());
                })
                .show();
    }

    public void sendRandomInput(String randomInput) {
        Observable.just(randomInput)
                .map(r -> bookRecordModel.sendRandomInput(bookingRecordId, r))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::showProgressDialog)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onBooked);
    }

    private void onBooked(AbstractMap.SimpleEntry<Booker.Result, List<String>> result) {
        dismissProgressDialog();
        String s = BookManager.getResultMsg(getContext(), result.getKey());
        SnackbarHelper.show(parentView, s, Snackbar.LENGTH_LONG);
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(getContext(), "", getContext().getString(R.string.is_booking));
    }

    private void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void updateEmptyMsg() {
        this.emptyMsg_textView.setVisibility(bookRecordModel.getBookRecords().isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void notifyBookRecordCreate(long bookRecordId) {
        getActivity().runOnUiThread(() -> {
            BookRecord bookRecord = bookRecordModel.getBookRecord(bookRecordId);
            if (bookRecord == null) return;
            bookRecords.add(0, bookRecord);
            bookRecordAdapter.notifyItemInserted(0);
            bookRecordAdapter.notifyItemRangeChanged(0, bookRecords.size()); // Use to update holder position.
            recyclerView.scrollToPosition(0);
            updateEmptyMsg();
        });
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
            bookRecordAdapter.notifyItemRangeChanged(0, bookRecords.size()); // Use to update holder position.
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
}
