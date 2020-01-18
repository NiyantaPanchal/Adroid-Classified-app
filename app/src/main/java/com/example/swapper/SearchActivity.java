package com.example.swapper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.swapper.fragments.AccountFragment;
import com.example.swapper.fragments.PostFragment;
import com.example.swapper.fragments.SearchFragment;
import com.example.swapper.fragments.WatchListFragment;
import com.example.swapper.util.SectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int REQUEST_CODE = 1;

    private TabLayout mTabLayout;
    public ViewPager mViewPager;
    public SectionPagerAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mTabLayout = (TabLayout)findViewById(R.id.tabs);
        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);

        verifyPermissions();

    }
    private void setupViewPager()
    {
        mPageAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mPageAdapter.addFragment(new SearchFragment());
        mPageAdapter.addFragment(new PostFragment());
        mPageAdapter.addFragment(new WatchListFragment());
        mPageAdapter.addFragment(new AccountFragment());

        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setText(getString(R.string.fragment_search));
        mTabLayout.getTabAt(1).setText(getString(R.string.fragment_post));
        mTabLayout.getTabAt(2).setText(getString(R.string.fragment_watch_list));
        mTabLayout.getTabAt(3).setText(getString(R.string.fragment_account));

    }

    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            setupViewPager();
        }else{
            ActivityCompat.requestPermissions(SearchActivity.this,
                    permissions,
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }
}
