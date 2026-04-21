package com.wyjqwy.app.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryPreset(
    val name: String,
    /** 写入服务端 icon 字段，便于多端对齐 */
    val iconKey: String,
    val icon: ImageVector
)

/** 支出分类（1），顺序与常见记账 App 接近 */
val expenseCategoryPresets: List<CategoryPreset> = listOf(
    CategoryPreset("餐饮", "restaurant", Icons.Outlined.Restaurant),
    CategoryPreset("烟酒", "local_bar", Icons.Outlined.LocalBar),
    CategoryPreset("购物", "shopping_cart", Icons.Outlined.ShoppingCart),
    CategoryPreset("交通", "directions_car", Icons.Outlined.DirectionsCar),
    CategoryPreset("住房", "home", Icons.Outlined.Home),
    CategoryPreset("娱乐", "movie", Icons.Outlined.Movie),
    CategoryPreset("居家", "weekend", Icons.Outlined.Weekend),
    CategoryPreset("家庭", "family_restroom", Icons.Outlined.FamilyRestroom),
    CategoryPreset("礼金", "card_giftcard", Icons.Outlined.CardGiftcard),
    CategoryPreset("通讯", "phone", Icons.Outlined.Phone),
    CategoryPreset("水电", "flash_on", Icons.Outlined.FlashOn),
    CategoryPreset("医疗", "local_hospital", Icons.Outlined.LocalHospital),
    CategoryPreset("教育", "school", Icons.Outlined.School),
    CategoryPreset("宠物", "pets", Icons.Outlined.Pets),
    CategoryPreset("运动", "fitness_center", Icons.Outlined.FitnessCenter),
    CategoryPreset("数码", "smartphone", Icons.Outlined.Smartphone),
    CategoryPreset("旅行", "flight", Icons.Outlined.Flight),
    CategoryPreset("母婴", "child_care", Icons.Outlined.ChildCare),
    CategoryPreset("美容", "face", Icons.Outlined.Face),
    CategoryPreset("维修", "build", Icons.Outlined.Build),
    CategoryPreset("社交", "people", Icons.Outlined.People),
    CategoryPreset("学习", "menu_book", Icons.Outlined.MenuBook),
    CategoryPreset("汽车", "directions_car_2", Icons.Outlined.DirectionsCar),
    CategoryPreset("打车", "local_taxi", Icons.Outlined.LocalTaxi),
    CategoryPreset("地铁", "train", Icons.Outlined.Train),
    CategoryPreset("骑行", "two_wheeler", Icons.Outlined.TwoWheeler),
    CategoryPreset("办公", "business", Icons.Outlined.Business),
    CategoryPreset("其他", "category", Icons.Outlined.Category)
)

/** 收入分类（2） */
val incomeCategoryPresets: List<CategoryPreset> = listOf(
    CategoryPreset("工资", "account_balance_wallet", Icons.Outlined.AccountBalanceWallet),
    CategoryPreset("奖金", "emoji_events", Icons.Outlined.EmojiEvents),
    CategoryPreset("兼职", "work_outline", Icons.Outlined.WorkOutline),
    CategoryPreset("理财", "savings", Icons.Outlined.Savings),
    CategoryPreset("红包", "redeem", Icons.Outlined.Redeem),
    CategoryPreset("退款", "undo", Icons.Outlined.Undo),
    CategoryPreset("转账", "swap_horiz", Icons.Outlined.SwapHoriz),
    CategoryPreset("其他", "category", Icons.Outlined.Category)
)

/** 明细列表等：按分类名解析图标（含预设 + 简单关键字） */
fun categoryIconForName(name: String): ImageVector {
    expenseCategoryPresets.find { it.name == name }?.let { return it.icon }
    incomeCategoryPresets.find { it.name == name }?.let { return it.icon }
    return when {
        name.contains("餐") || name.contains("食") || name.contains("面") -> Icons.Outlined.Restaurant
        name.contains("教育") || name.contains("学") -> Icons.Outlined.School
        name.contains("交通") || name.contains("车") || name.contains("行") -> Icons.Outlined.DirectionsCar
        name.contains("购物") || name.contains("买") -> Icons.Outlined.ShoppingCart
        name.contains("医") || name.contains("药") -> Icons.Outlined.LocalHospital
        name.contains("住") || name.contains("房") -> Icons.Outlined.Home
        else -> Icons.Outlined.Category
    }
}
