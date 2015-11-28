package ah.twrbtest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends Activity {

    @Bind(R.id.button_ticket)
    Button ticket_button;
    @Bind(R.id.button_timetable)
    Button titmetable_button;
    BookTicketFragment bookTicketFragment;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        this.realm = Realm.getDefaultInstance();
//        this.realm.beginTransaction();
//        User u = realm.createObject(User.class);
//        u.setId(System.currentTimeMillis());
//        u.setTitle("haha title");
//        u.setContent("內容");
//        this.realm.commitTransaction();
//
//
//        RealmResults<User> rr = this.realm.where(User.class).findAll();
//        for (User user : rr) {
//            System.out.println("------------------");
//            System.out.println(user.getId());
//            System.out.println(user.getTitle());
//            System.out.println(user.getContent());
//        }

        bookTicketFragment = BookTicketFragment.newInstance();
        switchFragment(bookTicketFragment);
    }

    @OnClick(R.id.button_timetable)
    public void onTimetableButtonClick() {
        switchFragment(TimetableFragment.newInstance());
    }

    @OnClick(R.id.button_ticket)
    public void onTicketButtonClick() {
        switchFragment(BookRecordFragment.newInstance());
    }

    @OnClick(R.id.button_bookticket)
    public void onBookTicketButtonClick() {
        switchFragment(bookTicketFragment);
    }

    public void switchFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
