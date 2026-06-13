package com.aiadvertisement.core.storage

import android.content.Context
import com.tencent.mmkv.MMKV

object MMKVHelper {

    fun init(context: Context) {
        MMKV.initialize(context)
    }

    fun getMMKV(name: String): MMKV {
        val mmkv = MMKV.mmkvWithID(name, MMKV.SINGLE_PROCESS_MODE, null)
        return mmkv
    }
}
