package com.nld.cloudpos.bankline.fragment.appsetting;

import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

/**
 * 打印设置
 */

public class PrinterSetDetailFragment extends BaseFragment implements View.OnClickListener {

    @ViewInject(R.id.btn_save_change)
    Button btn_save_change;
    @ViewInject(R.id.rg_scan_print)
    RadioGroup rg_scan_print ; //扫码
    @ViewInject(R.id.rg_font_size)
    RadioGroup rg_font_size; //字体大小
    @ViewInject(R.id.rb_scan_print_one)
    RadioButton rb_scan_print_one;
    @ViewInject(R.id.rb_scan_print_two)
    RadioButton rb_scan_print_two;
    @ViewInject(R.id.rb_small)
    RadioButton rb_small;
    @ViewInject(R.id.rb_normal)
    RadioButton rb_normal;
    @ViewInject(R.id.rb_big)
    RadioButton rb_big;

    private String scan_printer;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_printer;
    }

    @Override
    public void doInitSubViews(View view) {
        ViewUtils.inject(this,view);
        btn_save_change.setOnClickListener(this);
        rg_scan_print.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_scan_print_one:
                        scan_printer = "1";
                        break;
                    case R.id.rb_scan_print_two:
                        scan_printer = "2";
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void doInitData() {
        scan_printer = ScanParamsUtil.getInstance().getParam(TransParamsValue.SCAN_PRINTER_STATUE);
        switch (scan_printer){
            case "1":
                rg_scan_print.check(R.id.rb_scan_print_one);
                break;
            case  "2":
                rg_scan_print.check(R.id.rb_scan_print_two);
                break;
            default:
                scan_printer = "1";
                rg_scan_print.check(R.id.rb_scan_print_one);
                ScanParamsUtil.getInstance().save(TransParamsValue.SCAN_PRINTER_STATUE,scan_printer);
                break;

        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_save_change) {
            saveChange();
        }
    }


    /**
     * 保存设置
     */
    private void saveChange() {
        ScanParamsUtil.getInstance().save(TransParamsValue.SCAN_PRINTER_STATUE,scan_printer);
        ToastUtils.showToast("保存成功");
    }
}