package com.nld.cloudpos.payment.view;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

import common.StringUtil;

public class NumberKeyBoard extends LinearLayout {

    private Context mContext;
    private MEditText mInputView;
    private LayoutInflater mInflater;
    private LinearLayout numberKeyBoardLL, qwerKeyBoardLL, keyBoardBtnCaps;
    private TextView keyBoardBtnA, keyBoardBtnB ,keyBoardBtnC, keyBoardBtnD
            , keyBoardBtnE, keyBoardBtnF, keyBoardBtnG, keyBoardBtnH, keyBoardBtnI
            , keyBoardBtnJ, keyBoardBtnK, keyBoardBtnL, keyBoardBtnM, keyBoardBtnN
            , keyBoardBtnO, keyBoardBtnP, keyBoardBtnQ, keyBoardBtnR, keyBoardBtnS
            , keyBoardBtnT, keyBoardBtnU, keyBoardBtnV, keyBoardBtnW, keyBoardBtnX
            , keyBoardBtnY, keyBoardBtnZ;
    private boolean switchKeyboard;
    private int maxInputLength = 6;
    private String keepUpperOrLow;
    public NumberKeyBoard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public NumberKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NumberKeyBoard(Context context, MEditText MEditText) {
        super(context);
        initView(context);
        mInputView=MEditText;
    }
    public NumberKeyBoard(Context context) {
        super(context);
        initView(context);
    }

    protected void initView(Context context){
        mContext=context;
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_number_keyboard, this);
        numberKeyBoardLL = (LinearLayout) findViewById(R.id.number_keyboard_ll);
        qwerKeyBoardLL = (LinearLayout) findViewById(R.id.qwer_keyboard_ll);
        initBtns();
    }

    /**
     * 设置输入的MEditText
     * @param MEditText
     */
    public void setInputView(MEditText MEditText){
        mInputView=MEditText;
        mInputView.closeSoftKeyboard();
    }

    /**
     * 设置是否可以切换键盘
     * @param switchFlag
     */
    public void setSwitchKeyboard(boolean switchFlag){
        switchKeyboard = switchFlag;
    }

    /**
     * 设置最大可输入多少长度
     * @param len
     */
    public void setMaxInputLength(int len){
        maxInputLength = len;
    }

    /**
     * 保持大写或小写
     * @param upperOrLow
     */
    public void setKeepUpperOrLow(String upperOrLow){
        keepUpperOrLow = upperOrLow;
        //如果设置了保持大写或小写则大写键不生效
        if (!StringUtil.isEmpty(keepUpperOrLow)) {
            if ("upper".equals(keepUpperOrLow)){
                setUpper();
                keyBoardBtnCaps.setOnClickListener(null);
                mInputView.setFilters(new InputFilter[]{new UpperFilter(), new MaxLengthFilter()});
            } else if ("lower".equals(keepUpperOrLow)){
                setLower();
                keyBoardBtnCaps.setOnClickListener(null);
                mInputView.setFilters(new InputFilter[]{new LowerFilter(), new MaxLengthFilter()});
            }
        }
    }

    /**
     * 设置确定按钮点击事件
     * @param listener
     */
    public void setConfirmClick(OnClickListener listener){
        findViewById(R.id.keyboard_bt_ok).setOnClickListener(listener);
        findViewById(R.id.keyboard_qwe_bt_ok).setOnClickListener(listener);
    }

    private void initBtns(){
        findViewById(R.id.keyboard_bt_0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_0);
            }
        });
        findViewById(R.id.keyboard_bt_1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_1);
            }
        });
        findViewById(R.id.keyboard_bt_2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_2);
            }
        });
        findViewById(R.id.keyboard_bt_3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_3);
            }
        });
        findViewById(R.id.keyboard_bt_4).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_4);
            }
        });
        findViewById(R.id.keyboard_bt_5).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_5);
            }
        });
        findViewById(R.id.keyboard_bt_6).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_6);
            }
        });
        findViewById(R.id.keyboard_bt_7).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_7);
            }
        });
        findViewById(R.id.keyboard_bt_8).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_8);
            }
        });
        findViewById(R.id.keyboard_bt_9).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_9);
            }
        });
        findViewById(R.id.keyboard_bt_dot).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_NUMPAD_DOT);
            }
        });
        findViewById(R.id.keyboard_bt_del).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_DEL );
            }
        });
        //调用系统键盘
        findViewById(R.id.keyboard_bt_sys_kb).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchKeyboard) {
                    qwerKeyBoardLL.setVisibility(VISIBLE);
                    numberKeyBoardLL.setVisibility(GONE);
                }
            }
        });



        //字母键盘 开始
        keyBoardBtnA = (TextView) findViewById(R.id.keyboard_bt_a);
        keyBoardBtnA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_A);
            }
        });
        keyBoardBtnB = (TextView) findViewById(R.id.keyboard_bt_b);
        keyBoardBtnB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_B);
            }
        });
        keyBoardBtnC = (TextView) findViewById(R.id.keyboard_bt_c);
        keyBoardBtnC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_C);
            }
        });
        keyBoardBtnD = (TextView) findViewById(R.id.keyboard_bt_d);
        keyBoardBtnD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_D);
            }
        });
        keyBoardBtnE = (TextView) findViewById(R.id.keyboard_bt_e);
        keyBoardBtnE.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_E);
            }
        });
        keyBoardBtnF = (TextView) findViewById(R.id.keyboard_bt_f);
        keyBoardBtnF.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_F);
            }
        });
        keyBoardBtnG = (TextView) findViewById(R.id.keyboard_bt_g);
        keyBoardBtnG.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_G);
            }
        });
        keyBoardBtnH = (TextView) findViewById(R.id.keyboard_bt_h);
        keyBoardBtnH.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_H);
            }
        });
        keyBoardBtnI = (TextView) findViewById(R.id.keyboard_bt_i);
        keyBoardBtnI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_I);
            }
        });
        keyBoardBtnJ = (TextView) findViewById(R.id.keyboard_bt_j);
        keyBoardBtnJ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_J);
            }
        });
        keyBoardBtnK = (TextView) findViewById(R.id.keyboard_bt_k);
        keyBoardBtnK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_K);
            }
        });
        keyBoardBtnL = (TextView) findViewById(R.id.keyboard_bt_l);
        keyBoardBtnL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_L);
            }
        });
        keyBoardBtnM = (TextView) findViewById(R.id.keyboard_bt_m);
        keyBoardBtnM.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_M);
            }
        });
        keyBoardBtnN = (TextView) findViewById(R.id.keyboard_bt_n);
        keyBoardBtnN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_N);
            }
        });
        keyBoardBtnO = (TextView) findViewById(R.id.keyboard_bt_o);
        keyBoardBtnO.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_O);
            }
        });
        keyBoardBtnP = (TextView) findViewById(R.id.keyboard_bt_p);
        keyBoardBtnP.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_P);
            }
        });
        keyBoardBtnQ = (TextView) findViewById(R.id.keyboard_bt_q);
        keyBoardBtnQ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_Q);
            }
        });
        keyBoardBtnR = (TextView) findViewById(R.id.keyboard_bt_r);
        keyBoardBtnR.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_R);
            }
        });
        keyBoardBtnS = (TextView) findViewById(R.id.keyboard_bt_s);
        keyBoardBtnS.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_S);
            }
        });
        keyBoardBtnT = (TextView) findViewById(R.id.keyboard_bt_t);
        keyBoardBtnT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_T);
            }
        });
        keyBoardBtnU = (TextView) findViewById(R.id.keyboard_bt_u);
        keyBoardBtnU.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_U);
            }
        });
        keyBoardBtnV = (TextView) findViewById(R.id.keyboard_bt_v);
        keyBoardBtnV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_V);
            }
        });
        keyBoardBtnW = (TextView) findViewById(R.id.keyboard_bt_w);
        keyBoardBtnW.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_W);
            }
        });
        keyBoardBtnX = (TextView) findViewById(R.id.keyboard_bt_x);
        keyBoardBtnX.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_X);
            }
        });
        keyBoardBtnY = (TextView) findViewById(R.id.keyboard_bt_y);
        keyBoardBtnY.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_Y);
            }
        });
        keyBoardBtnZ = (TextView) findViewById(R.id.keyboard_bt_z);
        keyBoardBtnZ.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_Z);
            }
        });
        //大写键
        keyBoardBtnCaps = (LinearLayout) findViewById(R.id.keyboard_qwe_bt_caps);
        keyBoardBtnCaps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("a".equals(keyBoardBtnA.getText().toString())){
                    setUpper();
                }else{
                    setLower();
                }
                performInput(KeyEvent.KEYCODE_CAPS_LOCK );
            }
        });

        //删除键
        findViewById(R.id.keyboard_qwe_bt_del).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performInput(KeyEvent.KEYCODE_DEL );
            }
        });
        //切换键盘键
        findViewById(R.id.keyboard_bt_switch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                qwerKeyBoardLL.setVisibility(GONE);
                numberKeyBoardLL.setVisibility(VISIBLE);
            }
        });
    }



    /**
     * 键盘按键模拟
     * @param
     */
    void performInput(final int code) {
        if(null==mInputView){
            return;
        }
        //点击大写键盘
        if (code == KeyEvent.KEYCODE_CAPS_LOCK){
            if ("A".equals(keyBoardBtnA.getText().toString())){
                mInputView.setFilters(new InputFilter[]{new UpperFilter(), new MaxLengthFilter()});
            } else {
                mInputView.setFilters(new InputFilter[]{new MaxLengthFilter()});
            }

            return;
        }
        mInputView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
        mInputView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, code));
        mInputView.requestFocus();
    }

    private class UpperFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence inputChar, int i, int i1, Spanned spanned, int i2, int i3) {
//            if (spanned.toString().length() >= 6) {
//                return "";
//            }
            return inputChar.toString().toUpperCase();
        }
    }

    private class LowerFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence inputChar, int i, int i1, Spanned spanned, int i2, int i3) {
//            if (spanned.toString().length() >= 6) {
//                return "";
//            }
            return inputChar.toString().toLowerCase();
        }
    }

    private class MaxLengthFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence inputChar, int i, int i1, Spanned spanned, int i2, int i3) {
            if (maxInputLength != 0 && spanned.toString().length() >= maxInputLength) {
                return "";
            }
            return inputChar;
        }
    }

    private void setUpper(){
        keyBoardBtnA.setText("A");
        keyBoardBtnB.setText("B");
        keyBoardBtnC.setText("C");
        keyBoardBtnD.setText("D");
        keyBoardBtnE.setText("E");
        keyBoardBtnF.setText("F");
        keyBoardBtnG.setText("G");
        keyBoardBtnH.setText("H");
        keyBoardBtnI.setText("I");
        keyBoardBtnJ.setText("J");
        keyBoardBtnK.setText("K");
        keyBoardBtnL.setText("L");
        keyBoardBtnM.setText("M");
        keyBoardBtnN.setText("N");
        keyBoardBtnO.setText("O");
        keyBoardBtnP.setText("P");
        keyBoardBtnQ.setText("Q");
        keyBoardBtnR.setText("R");
        keyBoardBtnS.setText("S");
        keyBoardBtnT.setText("T");
        keyBoardBtnU.setText("U");
        keyBoardBtnV.setText("V");
        keyBoardBtnW.setText("W");
        keyBoardBtnX.setText("X");
        keyBoardBtnY.setText("Y");
        keyBoardBtnZ.setText("Z");
    }

    private void setLower(){
        keyBoardBtnA.setText("a");
        keyBoardBtnB.setText("b");
        keyBoardBtnC.setText("c");
        keyBoardBtnD.setText("d");
        keyBoardBtnE.setText("e");
        keyBoardBtnF.setText("f");
        keyBoardBtnG.setText("g");
        keyBoardBtnH.setText("h");
        keyBoardBtnI.setText("i");
        keyBoardBtnJ.setText("j");
        keyBoardBtnK.setText("k");
        keyBoardBtnL.setText("l");
        keyBoardBtnM.setText("m");
        keyBoardBtnN.setText("n");
        keyBoardBtnO.setText("o");
        keyBoardBtnP.setText("p");
        keyBoardBtnQ.setText("q");
        keyBoardBtnR.setText("r");
        keyBoardBtnS.setText("s");
        keyBoardBtnT.setText("t");
        keyBoardBtnU.setText("u");
        keyBoardBtnV.setText("v");
        keyBoardBtnW.setText("w");
        keyBoardBtnX.setText("x");
        keyBoardBtnY.setText("y");
        keyBoardBtnZ.setText("z");
    }
}
