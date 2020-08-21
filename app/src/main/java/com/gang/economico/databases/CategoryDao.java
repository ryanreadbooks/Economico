package com.gang.economico.databases;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gang.economico.entities.CategoryModel;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("select * from category_table")
    List<CategoryModel> retrieveAllCategories();

    @Query("select * from category_table where isSpending == 1")
    List<CategoryModel> retrieveSpendingCategory();

    @Query("select * from category_table where isSpending == 0")
    List<CategoryModel> retrieveIncomeCategory();

    @Insert
    void addNewCategory(CategoryModel... categoryModels);

    @Query("select img_res from category_table where category_name =:category_name")
    String retrieveImgResByName(String category_name);
}
