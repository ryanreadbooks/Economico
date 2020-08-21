package com.gang.economico.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;


/**
 * deprecated in v1.1
 */
@Deprecated
public class EditViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mEditFragmentList;

    public EditViewPagerAdapter(@NonNull FragmentManager fm, int behavior, List<Fragment> editFragmentList) {
        super(fm, behavior);
        this.mEditFragmentList = editFragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mEditFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mEditFragmentList.size();
    }
}
