package com.sjl.badger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText etNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etNum = findViewById(R.id.etNum);
    }

    public void addBadger(View view) {
        try {
            BadgerUtil.addBadger(this, Integer.valueOf(etNum.getText().toString()));
        }catch (Exception e){
            Toast.makeText(this,"请输入正整数",Toast.LENGTH_SHORT).show();
        }
    }
}
