package com.gang.economico.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gang.economico.entities.CategoryModel;

/**
 * Description: 保存分类数据的数据库
 * Time: 4/18/2020
*/
@Database(entities = {CategoryModel.class}, version = 1, exportSchema = false)
public abstract class CategoryDatabase extends RoomDatabase {

    private static CategoryDatabase CATEGORY_DATABASE_INSTANCE;

    public abstract CategoryDao getCategoryDao();

    public static synchronized CategoryDatabase getCategoryDatabase(Context context) {
        if (CATEGORY_DATABASE_INSTANCE == null) {
            CATEGORY_DATABASE_INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CategoryDatabase.class, "category_table")
                    .allowMainThreadQueries()
                    .build();
        }
        return CATEGORY_DATABASE_INSTANCE;
    }
}
