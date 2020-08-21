package com.gang.economico.configs;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gang.economico.R;
import com.gang.economico.databases.CategoryDao;
import com.gang.economico.databases.CategoryDatabase;
import com.gang.economico.entities.CategoryModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: 用户设置的读取和设置处理类
 * Time: 4/18/2020
*/
public class CategoryConfigs {

    private static final String TAG = "CategoryConfigs";
    private static CategoryDao mCategoryDao;
    private List<CategoryModel> mCategoryList;
    private List<CategoryModel> mIncomeList;

    public CategoryConfigs(Context context, boolean needsPreInsertion) {
        CategoryDatabase categoryDatabase = CategoryDatabase.getCategoryDatabase(context);
        mCategoryDao = categoryDatabase.getCategoryDao();
        if (needsPreInsertion) {
            try {
                insertInFirstUsage(context);
            } catch (IOException e) {
                Log.d(TAG, "if exceptions occurs then write io caused it");
                e.printStackTrace();
            }
        }
        loadCategoryInDatabase();
    }

    // 从数据库中加载分类
    private void loadCategoryInDatabase() {
        mCategoryList = mCategoryDao.retrieveSpendingCategory();
        mIncomeList = mCategoryDao.retrieveIncomeCategory();
    }

    public List<CategoryModel> loadSpendingCategoryConfig() {
        return mCategoryList;
    }

    public List<CategoryModel> loadIncomeCategoryConfig() {
        return mIncomeList;
    }

    public String getImgResByCategoryName(String categoryName) {
        return mCategoryDao.retrieveImgResByName(categoryName);
    }

    private void insertInFirstUsage(Context context) throws IOException {

        ArrayList<CategoryModel> categoryList = createResource();

        // 同时往SharedPreference中添加一样的数据
        SharedPreferences pf = context.getSharedPreferences("category_img_res", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pf.edit();
        // 第一次使用时需要创建数据库, 且数据库中没有数据, 并且添加默认的数据进去
        for (CategoryModel categoryModel : categoryList) {
            mCategoryDao.addNewCategory(categoryModel);
            editor.putInt(categoryModel.getCategoryName(), categoryModel.getImgResInt());

        }
        // 把支付方式的默认图标也放进去
        ArrayList<CategoryModel> paymentList = new ArrayList<>();
        paymentList.add(new CategoryModel(R.drawable.ic_alipay + "", "支付宝", false));
        paymentList.add(new CategoryModel(R.drawable.ic_wechat + "", "微信", false));
        paymentList.add(new CategoryModel(R.drawable.ic_cash + "", "现金", false));
        paymentList.add(new CategoryModel(R.drawable.ic_chinese_bank + "", "中国银行", false));
        paymentList.add(new CategoryModel(R.drawable.ic_cbc + "", "建设银行", false));

        Log.d(TAG, "insertInFirstUsage: ");
        // 写入二进制文件
        FileOutputStream fileOutputStream = context.openFileOutput("payment_list", Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(paymentList);
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
        objectOutputStream.close();

        for (CategoryModel c : paymentList) {
            editor.putInt(c.getCategoryName(), c.getImgResInt());
        }
        editor.apply();
    }

    private ArrayList<CategoryModel> createResource() {
        ArrayList<CategoryModel> categoryList = new ArrayList<>();
        categoryList.add(new CategoryModel(R.drawable.ic_spending_regular + "", "普通", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_dining + "", "餐饮", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_transportation + "", "交通", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_exercise + "", "健身", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_house + "", "居家", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_clothe + "", "衣服", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_digital + "", "数码", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_food + "", "食材", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_gift + "", "送礼", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_beverage + "", "饮料", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_dating + "", "约会", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_makeups + "", "化妆", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_movie + "", "电影", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_travel + "", "旅游", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_household + "", "家具", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_medical + "", "医疗", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_recreation + "", "游乐", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_study + "", "学习", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_shopping + "", "购物", true));
        categoryList.add(new CategoryModel(R.drawable.ic_spending_phonebills + "", "话费", true));

        categoryList.add(new CategoryModel(R.drawable.ic_income_salary + "", "工资", false));
        categoryList.add(new CategoryModel(R.drawable.ic_income_bonus + "", "奖金", false));
        categoryList.add(new CategoryModel(R.drawable.ic_income_investment_income + "", "投资", false));
        categoryList.add(new CategoryModel(R.drawable.ic_income_redpacket + "", "红包", false));
        categoryList.add(new CategoryModel(R.drawable.ic_income_scholarship + "", "奖学金", false));
        categoryList.add(new CategoryModel(R.drawable.ic_income_refund + "", "报销", false));

        return categoryList;
    }

    /**
     * 调试时用来更新资源的id
     * 由我来手动操作
     */
    @Deprecated
    public void updateResourceInSharedPref(Context context) {
        ArrayList<CategoryModel> categoryList = createResource();
        SharedPreferences pf = context.getSharedPreferences("category_img_res", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pf.edit();
        // 第一次使用时需要创建数据库, 且数据库中没有数据, 并且添加默认的数据进去
        for (CategoryModel categoryModel : categoryList) {
            editor.putInt(categoryModel.getCategoryName(), categoryModel.getImgResInt());
        }
        editor.apply();
        // 把支付方式的默认图标也放进去
        ArrayList<CategoryModel> paymentList = new ArrayList<>();
        paymentList.add(new CategoryModel(R.drawable.ic_alipay + "", "支付宝", false));
        paymentList.add(new CategoryModel(R.drawable.ic_wechat + "", "微信", false));
        paymentList.add(new CategoryModel(R.drawable.ic_cash + "", "现金", false));
        paymentList.add(new CategoryModel(R.drawable.ic_chinese_bank + "", "中国银行", false));
        paymentList.add(new CategoryModel(R.drawable.ic_cbc + "", "建设银行", false));

        for (CategoryModel c : paymentList) {
            editor.putInt(c.getCategoryName(), c.getImgResInt());
        }
        editor.apply();
    }
}
