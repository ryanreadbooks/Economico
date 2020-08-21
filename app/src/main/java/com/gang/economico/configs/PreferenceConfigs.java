package com.gang.economico.configs;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Description: 配置SharedPreference的配置类
 * Time: 4/18/2020
*/
public class PreferenceConfigs {

    private static final String TAG = "PreferenceConfigs";
    private static SharedPreferences mPreferences;

    public PreferenceConfigs(Context context) {
        mPreferences = context.getSharedPreferences("user_configs", Context.MODE_PRIVATE);
    }

    public boolean needsPreInsertion() {
        boolean needsPreInsertion;
        if (mPreferences.contains("needs_pre_insertion")) {
            needsPreInsertion = mPreferences.getBoolean("needs_pre_insertion", true);
            Log.d(TAG, "needsPreInsertion: contains needs_pre_insertion: " + needsPreInsertion);
        }
        else {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("needs_pre_insertion", false);
            editor.apply();
            needsPreInsertion = true;
        }
        return needsPreInsertion;
    }

    public static String getTotalBudgets() {
        return mPreferences.getString("total_budgets", "0.00");
    }

    public void setTotalBudgets(String totalBudgets) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("total_budgets", totalBudgets);
        editor.apply();
    }
}
