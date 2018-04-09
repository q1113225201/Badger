package com.sjl.badger;

import android.app.Activity;
import android.content.Intent;
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
            App.count = Math.abs(Integer.valueOf(etNum.getText().toString()));
        }catch (Exception e){
            Toast.makeText(this,"请输入正整数",Toast.LENGTH_SHORT).show();
        }
    }

    public void jump(View view){
        startActivity(new Intent(this,AnotherActivity.class));
    }
}
