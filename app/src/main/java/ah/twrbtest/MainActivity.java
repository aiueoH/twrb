package ah.twrbtest;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import ah.twrbtest.Fragments.BookRecordFragment;
import ah.twrbtest.Fragments.SearchFragment;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.container)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ViewPagerAdapter vpa = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(vpa);
        tabLayout.setupWithViewPager(viewPager);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final String[] titles = new String[]{
            getString(R.string.tab_name_search),
            getString(R.string.tab_name_ticket),
        };
        private final Fragment[] fragments = new Fragment[]{
                SearchFragment.newInstance(),
                BookRecordFragment.newInstance(),
        };

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
