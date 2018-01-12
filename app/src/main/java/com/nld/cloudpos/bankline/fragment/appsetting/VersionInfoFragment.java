package com.nld.cloudpos.bankline.fragment.appsetting;

import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nld.cloudpos.bankline.BuildConfig;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.starpos.wxtrade.http.ApiTools;

/**
 * Created by sago on 2017/2/25.
 */

public class VersionInfoFragment extends BaseFragment {

    private TextView mTxtVersionInfo, mTxtVersionBuild, mTxtVersionEvm;
    private Button btn_return;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_layou_app_set_versioninfo;
    }

    @Override
    public void doInitSubViews(View view) {
        mTxtVersionInfo = queryViewById(R.id.txt_version_info);
        try {
            String versionName = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;

            mTxtVersionInfo.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTxtVersionBuild = queryViewById(R.id.txt_version_build);
//        String content = null;
//        Resources resources = getActivity().getResources();
//        InputStream is = null;
//
//        is = resources.openRawResource(R.raw.date);
//        byte buffer[] = new byte[0];
//        try {
//            buffer = new byte[is.available()];
//            is.read(buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        content = new String(buffer);


        mTxtVersionBuild.setText(BuildConfig.BUILD_TIME);

        mTxtVersionEvm = queryViewById(R.id.txt_version_evm);
        mTxtVersionEvm.setText(ApiTools.MODEL);
        btn_return = queryViewById(R.id.btn_return);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }
}
