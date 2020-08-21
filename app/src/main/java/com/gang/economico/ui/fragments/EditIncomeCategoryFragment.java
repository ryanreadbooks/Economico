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

public class EditIncomeCategoryFragment extends Fragment {

    private static final String TAG = "EditIncome";
    private CategoryDisplayView mIncomeCategoryView;
    private List<CategoryModel> mIncomeCategoryModels;
    private EditViewModel mEditViewModel;

    public EditIncomeCategoryFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mIncomeCategoryModels = new ArrayList<>();
        mEditViewModel = new ViewModelProvider(requireActivity()).get(EditViewModel.class);
        // 加载配置情况
        PreferenceConfigs pConfigs = new PreferenceConfigs(requireContext());
        // 加载收入分类
        mIncomeCategoryModels = new CategoryConfigs(requireContext(), pConfigs.needsPreInsertion()).loadIncomeCategoryConfig();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_edit_income_category, container, false);
        mIncomeCategoryView = view.findViewById(R.id.edit_income_category);
        mIncomeCategoryView.setOnItemClickListener(new CategoryDisplayView.OnItemClickListener() {
            @Override
            public void onItemClick(String categoryImgRes, String categoryName) {
                Log.d(TAG, "onItemClick: " + categoryName);
                mEditViewModel.setSelectedCategoryLiveData(categoryImgRes, categoryName);
            }
        });
        mIncomeCategoryView.setCategoryResource(mIncomeCategoryModels);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIncomeCategoryModels.clear();
        mIncomeCategoryModels = null;
        mIncomeCategoryView.clearData();
        mEditViewModel = null;
        Log.d(TAG, "onDestroy: EditIncomeCategoryFragment");
    }
}
