package com.gang.economico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.gang.economico.databases.CalendarConverter;

import java.math.BigDecimal;
import java.util.Calendar;

/*
 * Description: 一条记账记录类
 */
// 如果在这个注解里面不设置表名的话，那么默认的表名就是实体类的名称，因此需要设置表名
@Entity(tableName = "bill_table")
public class BillRecord implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int uid;
    @ColumnInfo(name = "major_category")
    private String mMajorCategory;
    @ColumnInfo(name = "amount")
    private String mAmount;
    @ColumnInfo(name = "comment")
    private String mComment;
    @ColumnInfo(name = "record_calendar")
    private String mRecordTime;
    @ColumnInfo(name = "record_year")
    private int mRecordYear;
    @ColumnInfo(name = "record_month")
    private int mRecordMonth;
    @ColumnInfo(name = "record_day")
    private int mRecordDay;
    @ColumnInfo(name = "payment_method")
    private String mPaymentMethod;
    @ColumnInfo(name = "isSpending")
    private boolean mIsSpending;
    @ColumnInfo(name = "fromBook")
    private String mBook;

    public BillRecord(String majorCategory,
                      String amount, String comment,
                      String recordTime, String paymentMethod,
                      boolean isSpending, String book) {
        mMajorCategory = majorCategory;
        mAmount = amount;
        mComment = comment;
        mRecordTime = recordTime;
        Calendar c = CalendarConverter.stringToCalendar(recordTime);
        mRecordYear = c.get(Calendar.YEAR);
        mRecordMonth = c.get(Calendar.MONTH) + 1;
        mRecordDay = c.get(Calendar.DAY_OF_MONTH);
        mPaymentMethod = paymentMethod;
        mIsSpending = isSpending;
        mBook = book;
    }

    protected BillRecord(Parcel in) {
        uid = in.readInt();
        mMajorCategory = in.readString();
        mAmount = in.readString();
        mComment = in.readString();
        mRecordTime = in.readString();
        mRecordYear = in.readInt();
        mRecordMonth = in.readInt();
        mRecordDay = in.readInt();
        mPaymentMethod = in.readString();
        mIsSpending = in.readByte() != 0;
        mBook = in.readString();
    }

    public static final Creator<BillRecord> CREATOR = new Creator<BillRecord>() {
        @Override
        public BillRecord createFromParcel(Parcel in) {
            return new BillRecord(in);
        }

        @Override
        public BillRecord[] newArray(int size) {
            return new BillRecord[size];
        }
    };

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getMajorCategory() {
        return mMajorCategory;
    }

    public void setMajorCategory(String majorCategory) {
        mMajorCategory = majorCategory;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public String getRecordTime() {
        return mRecordTime;
    }

    public void setRecordTime(String recordTime) {
        mRecordTime = recordTime;
        Calendar c = CalendarConverter.stringToCalendar(recordTime);
        mRecordYear = c.get(Calendar.YEAR);
        mRecordMonth = c.get(Calendar.MONTH) + 1;
        mRecordDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public int getRecordYear() {
        return mRecordYear;
    }

    public void setRecordYear(int recordYear) {
        mRecordYear = recordYear;
    }

    public int getRecordMonth() {
        return mRecordMonth;
    }

    public void setRecordMonth(int recordMonth) {
        mRecordMonth = recordMonth;
    }

    public int getRecordDay() {
        return mRecordDay;
    }

    public void setRecordDay(int recordDay) {
        mRecordDay = recordDay;
    }

    public String getPaymentMethod() {
        return mPaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        mPaymentMethod = paymentMethod;
    }

    public boolean isSpending() {
        return mIsSpending;
    }

    public void setSpending(boolean spending) {
        mIsSpending = spending;
    }

    public String getBook() {
        return mBook;
    }

    public void setBook(String book) {
        mBook = book;
    }

    @NonNull
    @Override
    public String toString() {
        return "BillRecord{" +
                "uid=" + uid +
                ", mMajorCategory='" + mMajorCategory + '\'' +
                ", mAmount='" + mAmount + '\'' +
                ", mComment='" + mComment + '\'' +
                ", mRecordTime=" +mRecordTime +
                ", mRecordYear=" + mRecordYear +
                ", mRecordMonth=" + mRecordMonth +
                ", mRecordDay=" + mRecordDay +
                ", mPaymentMethod='" + mPaymentMethod + '\'' +
                ", mIsSpending=" + mIsSpending +
                ", mBook='" + mBook + '\'' +
                '}';
    }

    public BigDecimal getAmountDecimal() {
        return new BigDecimal(mAmount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(mMajorCategory);
        dest.writeString(mAmount);
        dest.writeString(mComment);
        dest.writeString(mRecordTime);
        dest.writeInt(mRecordYear);
        dest.writeInt(mRecordMonth);
        dest.writeInt(mRecordDay);
        dest.writeString(mPaymentMethod);
        dest.writeByte((byte) (mIsSpending ? 1 : 0));
        dest.writeString(mBook);
    }
}
