package ah.twrbtest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import ah.twrbtest.Fragments.BookRecordFragment;
import ah.twrbtest.Fragments.BookTicketFragment;
import ah.twrbtest.Fragments.SearchFragment;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends Activity {

    @Bind(R.id.button_ticket)
    Button ticket_button;
    @Bind(R.id.button_search)
    Button search_button;
    private BookTicketFragment bookTicketFragment;
    private SearchFragment searchFragment;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.bookTicketFragment = BookTicketFragment.newInstance();
        this.searchFragment = SearchFragment.newInstance();
        switchFragment(bookTicketFragment);
    }

    @OnClick(R.id.button_search)
    public void onSearchButtonClick() {
        switchFragment(this.searchFragment);
    }

    @OnClick(R.id.button_ticket)
    public void onTicketButtonClick() {
        switchFragment(BookRecordFragment.newInstance());
    }

    @OnClick(R.id.button_bookticket)
    public void onBookTicketButtonClick() {
        switchFragment(this.bookTicketFragment);
    }

    public void switchFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
