package cn.xiaomayo.notebook;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Aty_navigation extends FragmentActivity {

    private Fragment fragment1,fragment2,fragment3;
    private Fragment[] fragments;
    private int lastfragment=0;

    private BottomNavigationView.OnNavigationItemSelectedListener monNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navi_home:
                    if (lastfragment != 0) {
                        switchFragment(lastfragment, 0);
                        lastfragment = 0;
                    }
                    return true;
                case R.id.navi_archive:
                    if (lastfragment != 1) {
                        switchFragment(lastfragment, 1);
                        lastfragment = 1;
                    }
                    return true;
                case R.id.navi_my:
                    if(lastfragment !=2){
                        switchFragment(lastfragment,2);
                        lastfragment=2;
                    }
                    return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(monNavigationItemSelectedListener);
        initFragment();
    }


    private void switchFragment(int lastfidx,int idx){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastfidx]);
        if(!fragments[idx].isAdded()){
            transaction.add(R.id.container,fragments[idx]);
        }
        transaction.show(fragments[idx]).commitAllowingStateLoss();
    }

    private void initFragment(){

//        fragment1 = new aty_fragment1();
//        fragment2 = new aty_fragment2();
        fragments = new Fragment[]{fragment1,fragment2};
        lastfragment = 0;
        getSupportFragmentManager().
                beginTransaction()
                .add(R.id.container,fragment1)
                .show(fragment1)
                .commit();
    }



}