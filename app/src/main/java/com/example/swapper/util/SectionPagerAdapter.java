package com.example.swapper.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SectionPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "SectionPagerAdapter";
    private final List<Fragment> mfragmentList = new ArrayList<>();

    public SectionPagerAdapter(FragmentManager fm){
        super(fm);
    }
    public void addFragment(Fragment fragment)
    {
        mfragmentList.add(fragment);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mfragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mfragmentList.size();
    }
}
