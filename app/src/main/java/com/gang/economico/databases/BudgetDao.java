package com.gang.economico.databases;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.gang.economico.entities.Budget;

/**
 * Description: 操作BudgetDatabase的DAO
*/
@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertBudgetToDB(Budget budget);

    @Update
    int updateBudgetInDB(Budget budget);

    @Delete
    void deleteBudgetInDB(Budget budget);

    @RawQuery
    Budget retrieveBudgetFromDB(SimpleSQLiteQuery query);
}
