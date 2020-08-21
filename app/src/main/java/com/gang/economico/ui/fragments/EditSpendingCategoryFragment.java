package com.gang.economico.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.gang.economico.R;
import com.gang.economico.configs.CategoryConfigs;
import com.gang.economico.entities.CategoryModel;
import com.gang.economico.configs.PreferenceConfigs;
import com.gang.economico.ui.customs.CategoryDisplayView;
import com.gang.economico.viewmodels.EditViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditSpendingCategoryFragment extends Fragment {

    private static final String TAG = "EditSpending";
    private CategoryDisplayView mSpendingCategoryView;
    private List<CategoryModel> mSpendingCategoryModels;
    private EditViewModel mEditViewModel;

    public EditSpendingCategoryFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mSpendingCategoryModels = new ArrayList<>();
        mEditViewModel = new ViewModelProvider(requireActivity()).get(EditViewModel.class);
        // 加载配置情况
        PreferenceConfigs pConfigs = new PreferenceConfigs(requireContext());
        // 加载支出分类
        mSpendingCategoryModels = new CategoryConfigs(requireContext(), pConfigs.needsPreInsertion()).loadSpendingCategoryConfig();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_edit_spending_category, container, false );
        mSpendingCategoryView = view.findViewById(R.id.edit_spending_category);
        mSpendingCategoryView.setOnItemClickListener(new CategoryDisplayView.OnItemClickListener() {
            @Override
            public void onItemClick(String categoryImgRes, String categoryName) {
                Log.d(TAG, "onItemClick: " + categoryName);
                mEditViewModel.setSelectedCategoryLiveData(categoryImgRes, categoryName);
            }
        });
        mSpendingCategoryView.setCategoryResource(mSpendingCategoryModels);

        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        mSpendingCategoryModels.clear();
        mSpendingCategoryModels = null;
        mSpendingCategoryView.clearData();
        mEditViewModel = null;
    }
}
