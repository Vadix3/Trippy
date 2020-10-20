package com.example.trippy.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.trippy.Interfaces.OnSearchTypeSelectedListener;
import com.example.trippy.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SearchTypeFragment extends Fragment {

    public static final String TAG = "pttt";
    protected View view;

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;

    private MaterialButtonToggleGroup toggleButton;

    private OnSearchTypeSelectedListener onSearchTypeSelectedListener;

    public SearchTypeFragment() {
    }

    public void setActivityCallBack(OnSearchTypeSelectedListener onSearchTypeSelectedListener) {
        this.onSearchTypeSelectedListener = onSearchTypeSelectedListener;
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
            view = inflater.inflate(R.layout.fragment_searchtype, container, false);
        }

        initViews();
        return view;
    }

    private void initViews() {
        Log.d(TAG, "initViews: initing views");
        toggleButton = view.findViewById(R.id.searchtype_BTN_toggleGroup);
        btn1 = view.findViewById(R.id.searchtype_BTN_btn1);
        btn2 = view.findViewById(R.id.searchtype_BTN_btn2);
        btn3 = view.findViewById(R.id.searchtype_BTN_btn3);
        btn4 = view.findViewById(R.id.searchtype_BTN_btn4);
        btn5 = view.findViewById(R.id.searchtype_BTN_btn5);
        toggleButton.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                switch (checkedId) {
                    case R.id.searchtype_BTN_btn1:
                        makeSearchTypeCallback("restaurant");
                        break;
                    case R.id.searchtype_BTN_btn2:
                        makeSearchTypeCallback("supermarket");
                        break;
                    case R.id.searchtype_BTN_btn3:
                        makeSearchTypeCallback("bar");
                        break;
                    case R.id.searchtype_BTN_btn4:
                        makeSearchTypeCallback("transit_station");
                        break;
                    case R.id.searchtype_BTN_btn5:
                        makeSearchTypeCallback("atm");
                        break;
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d("pttt", "onSaveInstanceState");

        super.onSaveInstanceState(outState);
    }

    private void makeSearchTypeCallback(String selectedSearchType) {
        if (onSearchTypeSelectedListener != null) {
            onSearchTypeSelectedListener.setSearchType(selectedSearchType);
        }
    }
}
