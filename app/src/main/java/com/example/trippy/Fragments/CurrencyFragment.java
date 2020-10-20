package com.example.trippy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.trippy.R;

import java.text.DecimalFormat;

public class CurrencyFragment extends Fragment {

    public static final String TAG = "pttt";
    protected View view;

    //Views
    private TextView myCurrencyLabel;
    private TextView myAmountLabel;
    private TextView targetCurrencyLabel;
    private TextView targetAmountLabel;
    private EditText editAmount;
    private RelativeLayout amountLayout;

    //Values
    private float myAmount;
    private float targetAmount;
    private String myCurrencyCode;
    private String targetCurrencyCode;

    public CurrencyFragment() {
    }

    public CurrencyFragment(float myAmount, float targetAmount, String myCurrencyCode, String targetCurrencyCode) {
        Log.d(TAG, "CurrencyFragment: myAmount: " + myAmount + " myCUrrency: " + myCurrencyCode
                + " targetAmount: " + targetAmount + " targetCurrency: " + targetCurrencyCode);
        this.myAmount = myAmount;
        this.targetAmount = targetAmount;
        this.myCurrencyCode = myCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_currency, container, false);
        }

        initViews();
        return view;
    }

    private void initViews() {

        Log.d(TAG, "initViews: initing currencyCard");

        // Weather fragment
        LinearLayout inputAmountLayer = view.findViewById(R.id.currencyFragment_LAY_inputAmountLabel);
        myAmountLabel = view.findViewById(R.id.currencyFragment_LBL_MyAmount);
        myCurrencyLabel = view.findViewById(R.id.currencyFragment_LBL_myCurrency);
        targetAmountLabel = view.findViewById(R.id.currencyFragment_LBL_targetAmount);
        targetCurrencyLabel = view.findViewById(R.id.currencyFragment_LBL_targetCurrency);
        editAmount = view.findViewById(R.id.currencyFragment_EDT_editAmount);
        amountLayout = view.findViewById(R.id.currencyFragment_LAY_amountLayout);

        editAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "onFocusChange: Focus changed!: view: " + view.toString() + " boolean: " + b);
                if (!b) {
                    editAmount.setVisibility(View.GONE);
                    editAmount.setText("");
                    myAmountLabel.setVisibility(View.VISIBLE);
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    targetAmountLabel.setText("" + df.format(targetAmount / myAmount));
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                } else {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        inputAmountLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: edit amount");
                myAmountLabel.setVisibility(View.GONE);
                editAmount.setVisibility(View.VISIBLE);
                editAmount.requestFocus();
            }
        });
        editAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    editAmount.clearFocus();
                }
                return false;
            }
        });

        editAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged: TextChanged");
                if (editAmount.getText().toString().equals("")) {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    float convertedCurrency = targetAmount / myAmount;
                    targetAmountLabel.setText("" + df.format(convertedCurrency));
                    targetAmountLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                } else {
                    float inputValue = Float.parseFloat(editAmount.getText().toString());
                    float adjAmount = targetAmount / myAmount * inputValue;
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    targetAmountLabel.setText("" + df.format(adjAmount));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        float convertedCurrency = targetAmount / myAmount;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        myAmountLabel.setText("1");
        myCurrencyLabel.setText(myCurrencyCode);

        targetAmountLabel.setText("" + df.format(convertedCurrency));
        targetCurrencyLabel.setText(targetCurrencyCode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d("pttt", "onSaveInstanceState");

        super.onSaveInstanceState(outState);
    }

}
