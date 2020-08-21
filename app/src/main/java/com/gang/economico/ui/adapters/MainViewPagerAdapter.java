package com.gang.economico.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;


/**
 * 主Activity中的ViewPager的Adapter
 * 该类已经不使用 迁移到ViewPager2中已经使用FragmentStateAdapter代替 其性能更优
 * deprecated in v1.1
 */
@Deprecated
public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private final static String[] mTabTitle = {"总览", "支出", "收入", "我的"};

    public MainViewPagerAdapter(@NonNull FragmentManager fm, int behavior, List<Fragment> fragmentList) {
        super(fm, behavior);
        this.mFragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitle[position];
    }
}
