package com.qhm.topic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qhm on 2017/4/17
 * <p>
 * 主题编辑EditText
 */

public class UCTopicEditText extends AppCompatEditText {

    private static final String DEFAULT_IDENTIFIER = "#";

    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#999999");
    private static final int DEFAULT_IDENTIFIER_COLOR = Color.parseColor("#23D0F2");

    private List<TopicPosition> mData;
    /**
     * 默认文字颜色
     */
    private int mDefaultTextColor = DEFAULT_TEXT_COLOR;
    /**
     * 高亮部分文字颜色
     */
    private int mIdentifierTextColor = DEFAULT_IDENTIFIER_COLOR;
    /**
     * 标识符 文字
     */
    private String mIdentifier = DEFAULT_IDENTIFIER;
    /**
     * 正则表达式
     */
    private String mRegular;
    /**
     * 正则
     */
    private Pattern pattern;
    /**
     * 变化前的字符串
     */
    private String lastString = "";

    public UCTopicEditText(Context context) {
        this(context, null);
    }

    public UCTopicEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue(attrs);
        initView();
    }

    private void initValue(AttributeSet attrs) {
        mData = new ArrayList<>();
        initAttributes(attrs);
        initRegular();
    }

    private void initView() {
        // 要实现文字的点击效果，这里需要做特殊处理
//        setMovementMethod(LinkMovementMethod.getInstance());
        initTextWatcher();
        initKeyListener();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        checkoutPosition(selStart);
    }

    /**
     * 设置光标 不能处于高亮文字中间
     *
     * @param selStart
     */
    private void checkoutPosition(int selStart) {
        if (mData == null) {
            return;
        }
        mData.clear();
        String content = getText().toString();
        // 设置正则
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            // 重置正则位置
            matcher.reset();
        }

        while (matcher.find()) {
            String topic = matcher.group(0);

            // 处理话题##符号
            if (topic != null) {
                int start = matcher.start(0);
                int end = start + topic.length();
                mData.add(new TopicPosition(start, end));
            }
        }

        for (int i = 0; i < mData.size(); i++) {
            TopicPosition tp = mData.get(i);
            if (selStart < tp.endPos && selStart > tp.startPos) {
                setSelection(tp.endPos);
                return;
            }
        }

    }

    /**
     * 初始化文字变化监听
     */
    private void initTextWatcher() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int positon = getSelectionStart();
                if (!s.toString().equals(lastString)) {
                    lastString = s.toString();
                    setText(getTopicContent(s.toString()));
                }
                setSelection(positon);
            }
        });
    }

    /**
     * 解析自定义属性
     *
     * @param attrs
     */
    private void initAttributes(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.UCTopicEditText);

            // 设置链接高亮颜色
            mIdentifierTextColor = typedArray.getColor(R.styleable.UCTopicEditText_defaultIdentifierColor,
                    DEFAULT_IDENTIFIER_COLOR);
            mDefaultTextColor = typedArray.getColor(R.styleable.UCTopicEditText_defaultTextColor,
                    DEFAULT_TEXT_COLOR);
            mIdentifier = typedArray.getString(R.styleable.UCTopicEditText_defaultIdentifier);
            if (TextUtils.isEmpty(mIdentifier)) {
                mIdentifier = DEFAULT_IDENTIFIER;
            }
            setTextColor(mDefaultTextColor);
            typedArray.recycle();
        }
    }

    /**
     * 监听键盘删除事件
     */
    private void initKeyListener() {
        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Editable editable = getText();
                String content = editable.toString().substring(0, getSelectionStart());
                if (TextUtils.isEmpty(content)) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN
                        && (content.lastIndexOf(mIdentifier) == content.length() - 1)) {
                    int selectionStart = getSelectionStart();
                    int selectionEnd = getSelectionEnd();
                    //如果起始光标和结束光标的位置不一致，说明是选中效果。直接返回false
                    if (selectionEnd != selectionStart) {
                        return false;
                    }
                    SpannableString spannableString = new SpannableString(content);
//                    int lastPosition = 0;
                    Matcher matcher = pattern.matcher(spannableString);
                    int start = 0;
                    int end = 0;
                    while (matcher.find()) {
                        String topic = matcher.group(0);
                        if (!TextUtils.isEmpty(topic)) {
                            start = content.lastIndexOf(topic);
                            end = start + topic.length();
                        }
                    }
                    if (start != end && end != 0 && end == content.length()) {
                        editable.delete(start, end);
                        return true;
                    }

                }
                return false;
            }
        });
    }

    /**
     * 初始化 正则表达式
     */
    private void initRegular() {
        setRegular(mIdentifier + "[\u4e00-\u9fa5\\w]+" + mIdentifier);
        pattern = Pattern.compile(mRegular);
    }

    /**
     * 处理文字，高亮 点击等
     */
    private CharSequence getTopicContent(CharSequence charSequence) {
        SpannableString spannableString = new SpannableString(charSequence);
        // 设置正则
        Matcher matcher = pattern.matcher(spannableString);

        if (matcher.find()) {
            // 重置正则位置
            matcher.reset();
        }

        while (matcher.find()) {
            String topic = matcher.group(0);

            // 处理话题##符号
            if (topic != null) {
                int start = matcher.start(0);
                int end = start + topic.length();
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mIdentifierTextColor);
                spannableString.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }

    /**
     * 设置 正则表达式
     *
     * @param regular
     */
    public void setRegular(String regular) {
        this.mRegular = regular;
    }

    /**
     * 设置默认字体颜色
     *
     * @param color
     */
    public void setDefaultTextColor(int color) {
        this.mDefaultTextColor = color;
    }

    /**
     * 标题文字颜色
     *
     * @param color
     */
    public void setIdentifierTextColor(int color) {
        this.mIdentifierTextColor = color;
    }

    /**
     * 设置标志文字
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
        initRegular();
    }

    /**
     * append 文字
     *
     * @param charSequence
     */
    public void appendText(CharSequence charSequence) {
        int position = getSelectionStart();
        if (position == -1) {
            position = 0;
        }
        Editable editable = getText();
        editable.insert(position, charSequence);
        setText(getTopicContent(getText().toString()));
        setSelection(position + charSequence.length());
    }

    /**
     * 用以记录高亮文字相应的位置
     */
    class TopicPosition {
        public int startPos;
        public int endPos;

        public TopicPosition(int startPos, int endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }
}
