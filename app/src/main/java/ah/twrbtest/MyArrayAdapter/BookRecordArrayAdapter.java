package ah.twrbtest.MyArrayAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ah.twrbtest.DBObject.BookRecord;
import ah.twrbtest.DBObject.BookableStation;
import ah.twrbtest.Helper.AsyncBookHelper;
import ah.twrbtest.Helper.AsyncCancelHelper;
import ah.twrbtest.OnBookedEvent;
import ah.twrbtest.OnCancelledEvent;
import ah.twrbtest.R;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class BookRecordArrayAdapter extends MyArrayAdapter<BookRecord> {
    MyArrayAdapter.ViewHolder viewHolder;

    public BookRecordArrayAdapter(Context context, int resource, List<BookRecord> bookRecords) {
        super(context, resource, bookRecords);
        EventBus.getDefault().register(this);
    }

    public void onEvent(OnCancelledEvent e) {
        notifyDataSetChanged();
        System.out.println("Book record array adapter receive OnCancelledEvent");
    }

    public void onEvent(OnBookedEvent e) {
        notifyDataSetChanged();
        System.out.println("Book record array adapter receive OnBookedEvent");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = convertView == null ? new ViewHolder() : null;
        return createView(position, convertView, parent, viewHolder);
    }

    @Override
    protected void findView(View view, MyArrayAdapter.ViewHolder viewHolder) {
        ((ViewHolder) viewHolder).textView_date = (TextView) view.findViewById(R.id.textView_date);
        ((ViewHolder) viewHolder).textView_no = (TextView) view.findViewById(R.id.textView_no);
        ((ViewHolder) viewHolder).textView_from = (TextView) view.findViewById(R.id.textView_from);
        ((ViewHolder) viewHolder).textView_to = (TextView) view.findViewById(R.id.textView_to);
        ((ViewHolder) viewHolder).textView_qtu = (TextView) view.findViewById(R.id.textView_qtu);
        ((ViewHolder) viewHolder).textView_persionId = (TextView) view.findViewById(R.id.textView_personid);
        ((ViewHolder) viewHolder).textView_code = (TextView) view.findViewById(R.id.textView_code);
        ((ViewHolder) viewHolder).textView_isCancelled = (TextView) view.findViewById(R.id.textView_isCancelled);
        ((ViewHolder) viewHolder).linearLayout_code = (LinearLayout) view.findViewById(R.id.linearLayout_code);
        ((ViewHolder) viewHolder).button_cancel = (Button) view.findViewById(R.id.button_cancel);
        ((ViewHolder) viewHolder).button_delete = (Button) view.findViewById(R.id.button_delete);
        ((ViewHolder) viewHolder).button_book = (Button) view.findViewById(R.id.button_book);
    }

    @Override
    protected void setView(MyArrayAdapter.ViewHolder viewHolder, final BookRecord bookRecord) {
        update(viewHolder, bookRecord);
        ((ViewHolder) viewHolder).textView_date.setText(bookRecord.getGetinDate());
        ((ViewHolder) viewHolder).textView_no.setText(bookRecord.getTrainNo());
        ((ViewHolder) viewHolder).textView_from.setText(BookableStation.getNameByNo(bookRecord.getFromStation()));
        ((ViewHolder) viewHolder).textView_to.setText(BookableStation.getNameByNo(bookRecord.getToStation()));
        ((ViewHolder) viewHolder).textView_qtu.setText(bookRecord.getOrderQtuStr());
        ((ViewHolder) viewHolder).textView_persionId.setText(bookRecord.getPersonId());
        ((ViewHolder) viewHolder).button_delete.setOnClickListener(new DeleteL(bookRecord, viewHolder));
        ((ViewHolder) viewHolder).button_book.setOnClickListener(new BookL(bookRecord, viewHolder));
        ((ViewHolder) viewHolder).button_cancel.setOnClickListener(new CancelL(bookRecord, viewHolder));
    }

    private void update(MyArrayAdapter.ViewHolder viewHolder, final BookRecord bookRecord) {
        ((ViewHolder) viewHolder).textView_code.setText(bookRecord.getCode());
        ((ViewHolder) viewHolder).linearLayout_code.setVisibility(bookRecord.getCode().isEmpty() ? View.GONE : View.VISIBLE);
        ((ViewHolder) viewHolder).button_book.setVisibility(!bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? View.GONE : View.VISIBLE);
        ((ViewHolder) viewHolder).button_cancel.setVisibility(bookRecord.getCode().isEmpty() || bookRecord.isCancelled() ? View.GONE : View.VISIBLE);
        ((ViewHolder) viewHolder).textView_isCancelled.setVisibility(bookRecord.isCancelled() ? View.VISIBLE : View.GONE);

    }

    static class ViewHolder extends MyArrayAdapter.ViewHolder {
        TextView textView_date;
        TextView textView_no;
        TextView textView_from;
        TextView textView_to;
        TextView textView_qtu;
        TextView textView_persionId;
        TextView textView_code;
        TextView textView_isCancelled;
        LinearLayout linearLayout_code;
        Button button_cancel;
        Button button_delete;
        Button button_book;
    }

    abstract class ButtonListener implements View.OnClickListener {
        protected BookRecord bookRecord;
        protected MyArrayAdapter.ViewHolder viewHolder;

        public ButtonListener(BookRecord bookRecord, MyArrayAdapter.ViewHolder viewHolder) {
            this.bookRecord = bookRecord;
            this.viewHolder = viewHolder;
        }
    }

    class DeleteL extends ButtonListener {
        public DeleteL(BookRecord bookRecord, MyArrayAdapter.ViewHolder viewHolder) {
            super(bookRecord, viewHolder);
        }

        @Override
        public void onClick(View v) {
            getItems().remove(bookRecord);
            notifyDataSetChanged();
            BookRecord br = Realm.getDefaultInstance().where(BookRecord.class).equalTo("id", bookRecord.getId()).findFirst();
            if (br == null) return;
            Realm.getDefaultInstance().beginTransaction();
            br.removeFromRealm();
            Realm.getDefaultInstance().commitTransaction();
        }
    }

    class BookL extends ButtonListener {
        public BookL(BookRecord bookRecord, MyArrayAdapter.ViewHolder viewHolder) {
            super(bookRecord, viewHolder);
        }

        @Override
        public void onClick(View v) {
            if (bookRecord.getCode().equals(""))
                new AsyncBookHelper(this.bookRecord).execute(bookRecord.getId());
        }
    }

    class CancelL extends ButtonListener {
        public CancelL(BookRecord bookRecord, MyArrayAdapter.ViewHolder viewHolder) {
            super(bookRecord, viewHolder);
        }

        @Override
        public void onClick(View v) {
            if (!bookRecord.getCode().equals(""))
                new AsyncCancelHelper(getContext(), this.bookRecord).execute(bookRecord.getId());
        }
    }
}
