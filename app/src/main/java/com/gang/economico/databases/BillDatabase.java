package com.gang.economico.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gang.economico.entities.BillRecord;

/**
 * 账单主数据库
 */
@Database(entities = {BillRecord.class}, version = 1, exportSchema = false)
public abstract class BillDatabase extends RoomDatabase {

    // 数据库唯一实例
    private static BillDatabase INSTANCE;
    // 返回DAO对象用于操作数据库
    public abstract BillDatabaseDao getBillDatabaseDao();

    // 获取数据库实例，单例模式
    public static synchronized BillDatabase getBillDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), BillDatabase.class, "bill_table")
                    .build();
        }
        return INSTANCE;
    }
}
