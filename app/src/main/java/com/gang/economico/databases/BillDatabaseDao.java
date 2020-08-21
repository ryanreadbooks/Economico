package com.gang.economico.databases;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.gang.economico.entities.BillRecord;
import com.gang.economico.model.AccountModel;
import com.gang.economico.model.DailyBillRecord;
import com.gang.economico.model.MonthlyStatistic;
import com.gang.economico.model.YearlyCategoryStatistic;

import java.util.List;


/**
 * Description:
 * 指定的账本中的查询功能
 * 与年相关
 * 0、按年查询所有数据
 * 1、按年查询所有支出
 * 2、按年查询所有收入
 * 3、按年查询某一支出分类的所有数据
 * 4、按年查询某一收入分类的所有数据
 * 5、按年查询某一支付方式下的所有支出数据
 * 6、按年查询某一支付方式下的所有收入数据
 *
 * 与月相关
 * 0、按月(包含某年)查询所有数据
 * 1、按月(包含某年)查询所有支出
 * 2、按月(包含某年)查询所有收入
 * 3、按月(包含某年)查询某一支出分类的所有数据
 * 4、按月(包含某年)查询某一收入分类的所有数据
 * 5、按月(包含某年)查询某一支付方式下的所有支出数据
 * 6、按月(包含某年)查询某一支付方式下的所有收入数据
 *
 * 与分类单独相关
 * 1、查询某一支出分类的所有数据
 * 2、查询某一收入分类的所有数据
 *
 * 与支付方式单独相关
 * 1、按照某一支付方式查询所有支出数据
 * 2、按照某一支付方式查询所有收入数据
 *
 * 其它
 * 1、插入一条完整的数据
 * 2、更新一条数据
 * 3、删除一条数据
 * 4、删除所有数据
 *
 * 注意：使用Runtime时期的SQL语句查询，需要使用 @RawQuery
 * Time: 4/17/2020
*/
@Dao
public interface BillDatabaseDao {

    // 插入数据 如果后面有... 则表示可以批量操作，返回值也会是一个数据，包含多个返回值
    @Insert
    Long insertBillRecordToDB(BillRecord billRecord);

    // 更新数据 返回数据更新条数
    @Update
    int updateBillRecordInDB(BillRecord... billRecords);

    // 删除数据
    @Delete
    void deleteBillRecordFromDB(BillRecord... billRecords);

    // 删除所有数据
    @Query("delete from bill_table")
    void deleteAllBillRecordsFromDB();

    // 按年查询所有数据，包括支出和收入
    // 按照每个月的数据分好组
    @RawQuery
    List<MonthlyStatistic> retrieveYearlyBillsFromDB(SupportSQLiteQuery query);

    // 查询每年的所有分类数据 指定支出或者收入
    // 按照每个分类分好组并排序
    @RawQuery
    List<YearlyCategoryStatistic> retrieveYearlyCategorizedFromDB(SupportSQLiteQuery query);

    // 按月查询
    @RawQuery
    List<BillRecord> retrieveMonthlyBillsFromDB(SupportSQLiteQuery query);

    // 每个月的每日金额的统计可以直接在SQLite中完成查询和处理
    @RawQuery
    List<DailyBillRecord> retrieveDailyTotalBillsFromDB(SupportSQLiteQuery query);

    // 查询指定年 月 和 分类的全部数据
    @RawQuery
    List<BillRecord> retrieveCategorizedBillsFromDB(SupportSQLiteQuery query);

    // 查询所有分类的数据 并且求和
    @RawQuery
    List<AccountModel> retrieveAccountsFromDB(SupportSQLiteQuery query);

    // ...还有其它
}
