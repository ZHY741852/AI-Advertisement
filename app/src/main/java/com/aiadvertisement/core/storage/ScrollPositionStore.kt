package com.aiadvertisement.core.storage

import com.tencent.mmkv.MMKV

class ScrollPositionStore {

    private val kv: MMKV = MMKVHelper.getMMKV("scroll_position")

    private fun indexKey(channel: String) = "index_$channel"
    private fun offsetKey(channel: String) = "offset_$channel"

    fun savePosition(channel: String, index: Int, offset: Int) {
        kv.encode(indexKey(channel), index)
        kv.encode(offsetKey(channel), offset)
    }

    fun getPosition(channel: String): Pair<Int, Int> {
        val index = kv.decodeInt(indexKey(channel), 0)
        val offset = kv.decodeInt(offsetKey(channel), 0)
        return index to offset
    }
}
