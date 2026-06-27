package com.okane.paperaero.util

import android.content.Context
import android.widget.Toast

object RealtimeToast {
    private var currentToast: Toast? = null

    // 🌟 이 함수를 부르면 이전 토스트를 즉시 지우고 새 토스트를 띄움!
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        currentToast?.cancel() // 👈 살아있는 이전 토스트가 있다면 칼같이 캔슬!

        currentToast = Toast.makeText(context, message, duration).apply {
            show()
        }
    }
}