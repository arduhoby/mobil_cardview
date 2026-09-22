package com.kartview.app.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.kartview.app.data.DemoData
import com.kartview.app.data.model.DemoItem

class MainViewModel : ViewModel() {

    val items = mutableStateListOf<DemoItem>().apply { addAll(DemoData.items) }

    fun dismiss(id: Long) {
        items.removeAll { it.id == id }
    }
}