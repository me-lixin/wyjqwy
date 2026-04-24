package com.wyjqwy.app.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryPreset(
    val name: String,
    /** 写入服务端 `icon` 字段，英文 snake_case，对用户不可见 */
    val iconKey: String,
    val icon: ImageVector
)

/** 系统默认支出分类（不可作为用户自定义分类进行编辑/删除） */
val expenseCategoryPresets: List<CategoryPreset> = listOf(
    CategoryPreset("餐饮美食", "food_dining", Icons.Outlined.Restaurant),
    CategoryPreset("交通出行", "transport", Icons.Outlined.DirectionsBus),
    CategoryPreset("购物消费", "shopping", Icons.Outlined.ShoppingBag),
    CategoryPreset("居家生活", "home_life", Icons.Outlined.Home),
    CategoryPreset("水电煤气", "utilities", Icons.Outlined.OfflineBolt),
    CategoryPreset("话费网费", "phone_bill", Icons.Outlined.PhoneIphone),
    CategoryPreset("休闲娱乐", "entertainment", Icons.Outlined.SportsEsports),
    CategoryPreset("零食饮品", "snacks_drinks", Icons.Outlined.Icecream),
    CategoryPreset("护肤美妆", "beauty", Icons.Outlined.FaceRetouchingNatural),
    CategoryPreset("服饰鞋包", "fashion", Icons.Outlined.Checkroom),
    CategoryPreset("医疗健康", "medical", Icons.Outlined.MedicalServices),
    CategoryPreset("学习教育", "education", Icons.Outlined.School),
    CategoryPreset("人情往来", "social_relation", Icons.Outlined.Handshake),
    CategoryPreset("宠物开销", "pets", Icons.Outlined.Pets),
    CategoryPreset("运动健身", "sports", Icons.Outlined.FitnessCenter),
    CategoryPreset("汽车开销", "auto", Icons.Outlined.DirectionsCar),
    CategoryPreset("数码电器", "digital", Icons.Outlined.Computer),
    CategoryPreset("房租房贷", "housing_loan", Icons.Outlined.Apartment),
    CategoryPreset("金融保险", "finance_insurance", Icons.Outlined.Shield),
    CategoryPreset("其他支出", "other_expense", Icons.Outlined.MoreHoriz)
)

/** 系统默认收入分类（不可作为用户自定义分类进行编辑/删除） */
val incomeCategoryPresets: List<CategoryPreset> = listOf(
    CategoryPreset("职业薪水", "salary", Icons.Outlined.WorkOutline),
    CategoryPreset("兼职外快", "part_time", Icons.Outlined.Timer),
    CategoryPreset("投资理财", "investment", Icons.Outlined.TrendingUp),
    CategoryPreset("奖金福利", "bonus", Icons.Outlined.CardGiftcard),
    CategoryPreset("收债回款", "debt_collection", Icons.Outlined.AssignmentReturn),
    CategoryPreset("人情礼金", "gift_money", Icons.Outlined.VolunteerActivism),
    CategoryPreset("二手闲置", "secondhand", Icons.Outlined.Recycling),
    CategoryPreset("报销入账", "reimbursement", Icons.Outlined.ReceiptLong),
    CategoryPreset("营业收入", "business_income", Icons.Outlined.Storefront),
    CategoryPreset("其他收入", "other_income", Icons.Outlined.AddCircleOutline)
)

/** 明细等：无 icon 字段时按分类名或关键字兜底 */
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
