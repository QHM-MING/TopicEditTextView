package com.qhm.topic;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by qhm on 2017/4/18
 * <p>
 * 主题页面
 */

public class UCTopicEditTextActivity extends Activity implements View.OnClickListener {

    private UCTopicEditText ucTopicEditText;
    private Button btnAdd;

    private int position = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uctopic_edittext);
        initView();
    }

    private void initView() {
        ucTopicEditText = (UCTopicEditText) findViewById(R.id.ucTopicEditText);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ucTopicEditText.appendText("#话题" + position + "#");
        position++;
    }
}
