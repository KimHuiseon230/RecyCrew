package com.piooda.recycrew.core.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ViewGreenButtonBinding

class GreenButton : FrameLayout, View.OnClickListener {
    private var listener: OnClickListener? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initView()
        getAttrs(attrs, defStyle)
    }

    private lateinit var binding: ViewGreenButtonBinding

    private fun initView() {
        binding = ViewGreenButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GreenButton)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GreenButton, defStyle, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        binding.button.text = typedArray.getString(R.styleable.GreenButton_text)
        binding.button.setOnClickListener(this)
        typedArray.recycle()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        // 버튼이 활성화된 경우에만 클릭 이벤트 전달
        if (isEnabled) {
            listener?.onClick(v)
        } else {
            Log.d("GreenButton", "Button is disabled, click ignored.")
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        // 명시적으로 UI 상태 업데이트
        binding.button.isEnabled = enabled // 내부 버튼의 활성화 상태 동기화
        alpha = if (enabled) 1.0f else 0.5f // 버튼 투명도 업데이트
    }
}