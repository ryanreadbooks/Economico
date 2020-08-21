package com.gang.economico.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.gang.economico.R;
import com.gang.economico.databases.CalendarConverter;
import com.gang.economico.entities.BillRecord;
import com.gang.economico.entities.CategoryModel;
import com.gang.economico.ui.activities.MainActivity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;


/**
 * Description: EditActivity内的数据管理
 * Time: 4/21/2020
*/
public class EditViewModel extends ViewModel {

    private static final String TAG = "edit viewModel";
    public static final int HAS_EDIT_OPERATION = 1;
    // 记录所选的记录的类别的LiveData
    private MutableLiveData<CategoryModel> selectedCategoryLiveData;
    private CategoryModel selectedCategory;
    private MutableLiveData<CategoryModel> selectedPaymentLiveData;
    private CategoryModel selectedPayment;
    // 记录显示日期的LiveData
    private Calendar selectedDate;
    private MutableLiveData<Calendar> selectedDateLiveData;
    // 显示的缓存的LiveData
    private MutableLiveData<String> displayBufferLiveData;
    // 显示缓存
    private String displayBuffer;
    // 输入缓存
    private StringBuilder inputBuffer;
    // 数字格式化
    private DecimalFormat mDecimalFormat;
    // 是否在EditActivity中插入了数据或者修改了数据
    private MutableLiveData<Integer> editState;

    public EditViewModel() {
        // inputBuffer的初始状态就是没数据
        inputBuffer = new StringBuilder();
        // 而displayBuffer在没有数据的时候仍然会显示一个0
        mDecimalFormat = new DecimalFormat();
        mDecimalFormat.setMinimumFractionDigits(2);
        // display buffer 格式化显示 始终都保留两位小数
        mDecimalFormat.applyPattern("0.00");
        displayBuffer = mDecimalFormat.format(0);
        displayBufferLiveData = new MutableLiveData<>(displayBuffer);
        // 默认选中的分类
        selectedCategory = new CategoryModel(R.drawable.ic_spending_regular + "", "普通", true);
        selectedCategoryLiveData = new MutableLiveData<>(selectedCategory);
        Log.d(TAG, "EditViewModel: constructor");
        // 默认选中的支付方式
        selectedPayment = new CategoryModel(R.drawable.ic_alipay + "", "支付宝", false);
        selectedPaymentLiveData = new MutableLiveData<>(selectedPayment);
        // 默认的日期时当天的时间
        selectedDate = new GregorianCalendar();
        selectedDateLiveData = new MutableLiveData<>(selectedDate);
        // 默认没有任何操作
        editState = new MutableLiveData<>(MainActivity.DO_NADA);
    }

    // 返回用于显示的LiveData
    public LiveData<String> getDisplayBufferLiveData() {
        return displayBufferLiveData;
    }

    public void setDisplayBufferLiveData(String newDisplayBuffer) {
        DecimalFormat df = new DecimalFormat();
        // 如果是整数格式(如15.00这种格式，则把后面的0和小数点去掉)
        df.applyPattern("0.##");
        inputBuffer = new StringBuilder(df.format(Float.parseFloat(newDisplayBuffer)));
        displayBuffer = mDecimalFormat.format(new BigDecimal(inputBuffer.toString()));
        displayBufferLiveData.setValue(displayBuffer);
    }

    public LiveData<CategoryModel> getSelectedCategoryLiveData() {
        return selectedCategoryLiveData;
    }

    public LiveData<CategoryModel> getSelectedPaymentLiveData() {
        return selectedPaymentLiveData;
    }

    public LiveData<Calendar> getSelectedDateLiveData() {
        return selectedDateLiveData;
    }

    public void setSelectedCategoryLiveData(String imgId, String categoryName) {
        selectedCategory.setImgRes(imgId);
        selectedCategory.setCategoryName(categoryName);
        selectedCategoryLiveData.setValue(selectedCategory);
    }

    public void setSelectedCategoryLiveData(int imgId, String categoryName) {
        Log.d(TAG, "setSelectedCategoryLiveData: id " + imgId + " name " + categoryName);
        selectedCategory.setImgRes(String.valueOf(imgId));
        selectedCategory.setCategoryName(categoryName);
        selectedCategoryLiveData.setValue(selectedCategory);
    }

    public void setSelectedPaymentLiveData(String imgId, String paymentName) {
        selectedPayment.setImgRes(imgId);
        selectedPayment.setCategoryName(paymentName);
        selectedPaymentLiveData.setValue(selectedPayment);
    }

    public void setSelectedDateLiveData(Calendar calendar) {
        selectedDate = calendar;
        selectedDateLiveData.setValue(selectedDate);
    }

    public Integer getEditState() {
        return editState.getValue();
    }

    // 用于设置inputBuffer,往后添加
    public void setInputBuffer(char input) {
        int dotAt = -1;             // 小数点所在位置，-1表示没有小数点
        for (int i = 0; i < inputBuffer.length(); i++) {
            if (inputBuffer.charAt(i) == '.') {
                dotAt = i;
                Log.d(TAG, "setInputBuffer: dot at :" + dotAt);
                break;
            }
        }
        if (input == '.' && dotAt != -1) {   // 只能有一个小数点
            return;
        }
        if (inputBuffer.length() == (dotAt + 3) && dotAt != -1) {    // 只保留小数点后两位
            return;
        }
        // input buffer中添加
        inputBuffer.append(input);
        if (inputBuffer.length() != 0) {        // 不允许出现0开头的非小数
            if (inputBuffer.charAt(0) == '.') {
                Log.d(TAG, "at insertion");
                inputBuffer.insert(0, '0');
            }
        }
        // 转换成为Display buffer显示出来
        displayBuffer = mDecimalFormat.format(new BigDecimal(inputBuffer.toString()));
        displayBufferLiveData.setValue(displayBuffer);
        Log.d(TAG, "input buffer :" + inputBuffer.toString());
    }

    // 按清除键清空
    public void clearInputBuffer() {
        inputBuffer.setLength(0);
        displayBuffer = mDecimalFormat.format(0);
        displayBufferLiveData.setValue(displayBuffer);
    }

    public void backSpace() {
        if (inputBuffer.length() != 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            Log.d(TAG, inputBuffer.toString());
            if (inputBuffer.length() == 0) {
                displayBuffer = mDecimalFormat.format(0);
            }
            else {
                displayBuffer = mDecimalFormat.format(new BigDecimal(inputBuffer.toString()));
            }
            displayBufferLiveData.setValue(displayBuffer);
        }
    }

    // 将当前显示区域的数值与下一次按加号或者ok之前输入的数值进行相加
    public void triggerAddition() {
    }

    // 按下OK键确认
    // 这里应该返回在数据库中是否插入成功
    public boolean triggerConfirm (String comment, boolean isSpending,String book, int resCode, BillRecord updatingBill) {
        // 新建账单对象
        BillRecord tempBillRecord = new BillRecord(
                selectedCategory.getCategoryName(),
                displayBuffer,
                comment,
                CalendarConverter.calendarToString(selectedDate),
                selectedPayment.getCategoryName(),
                isSpending,
                book);
        // MainActivity中请求插入新数据
        if (resCode == MainActivity.INSERT_BILL) {
            // 清空输入缓存 为下一次输入
            clearInputBuffer();
            Log.d(TAG, "triggerConfirm: " + tempBillRecord.toString());
            Long resultUid = null;
            try {
                resultUid = BillViewModel.insertBills(tempBillRecord);
                Log.d(TAG, "insertBillRecord: data of uid " + resultUid + " is inserted");
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            // 添加成功
            if (resultUid != null) {
                // 操作成功
                editState.setValue(HAS_EDIT_OPERATION);
                return true;
            }
            else {
                Log.d(TAG, "triggerConfirm: insertion failure");
            }
        }
        // MainActivity中请求更新一条数据
        else if (resCode == MainActivity.UPDATE_BILL){
            if (updatingBill != null) {
                updatingBill.setMajorCategory(selectedCategory.getCategoryName());
                updatingBill.setAmount(displayBuffer);
                updatingBill.setComment(comment);
                updatingBill.setRecordTime(CalendarConverter.calendarToString(selectedDate));
                updatingBill.setPaymentMethod(selectedPayment.getCategoryName());
                updatingBill.setSpending(isSpending);
                updatingBill.setBook(book);

                int numUpdated = 0;
                try {
                    numUpdated = BillViewModel.updateBills(updatingBill);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (numUpdated != 0) {
                    editState.setValue(HAS_EDIT_OPERATION);
                    return true;
                }
                else {
                    Log.d(TAG, "triggerConfirm: update failure");
                }
            }
        }
        return false;
    }
}
