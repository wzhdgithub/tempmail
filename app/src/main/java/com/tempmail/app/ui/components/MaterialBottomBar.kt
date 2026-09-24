package com.tempmail.app.ui.components

// Material3 默认底栏 —— 零改动区：只允许整体搬迁, 禁止修改任何样式/行为。
// 从根布局原样搬出, 仅将捕获变量参数化(current/label/onSelect)。

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tempmail.app.model.Tab

@Composable
internal fun MaterialBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = current == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, label(tab)) },
                label = { Text(label(tab)) }
            )
        }
    }
}
