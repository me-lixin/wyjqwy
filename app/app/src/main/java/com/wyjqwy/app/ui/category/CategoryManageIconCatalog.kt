package com.wyjqwy.app.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nightlife
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SportsBar
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

/** 添加分类页：分组展示的图标槽位（iconKey 与后端 icon 字段对齐） */
data class CategoryManageIconSlot(
    val iconKey: String,
    val icon: ImageVector
)

data class CategoryManageIconSection(
    val title: String,
    val slots: List<CategoryManageIconSlot>
)

/** 支出图标分组 */
val expenseManageIconSections: List<CategoryManageIconSection> = listOf(
    CategoryManageIconSection(
        title = "娱乐",
        slots = listOf(
            CategoryManageIconSlot("sports_esports", Icons.Outlined.SportsEsports),
            CategoryManageIconSlot("sports_basketball", Icons.Outlined.SportsBasketball),
            CategoryManageIconSlot("sports_soccer", Icons.Outlined.SportsSoccer),
            CategoryManageIconSlot("movie", Icons.Outlined.Movie),
            CategoryManageIconSlot("theaters", Icons.Outlined.Theaters),
            CategoryManageIconSlot("attractions", Icons.Outlined.Attractions),
            CategoryManageIconSlot("casino", Icons.Outlined.Casino),
            CategoryManageIconSlot("nightlife", Icons.Outlined.Nightlife),
            CategoryManageIconSlot("music_note", Icons.Outlined.MusicNote),
            CategoryManageIconSlot("park", Icons.Outlined.Park),
            CategoryManageIconSlot("beach_access", Icons.Outlined.BeachAccess),
            CategoryManageIconSlot("sports_bar", Icons.Outlined.SportsBar)
        )
    ),
    CategoryManageIconSection(
        title = "饮食",
        slots = listOf(
            CategoryManageIconSlot("restaurant", Icons.Outlined.Restaurant),
            CategoryManageIconSlot("local_bar", Icons.Outlined.LocalBar),
            CategoryManageIconSlot("local_pizza", Icons.Outlined.LocalPizza),
            CategoryManageIconSlot("ramen_dining", Icons.Outlined.RamenDining),
            CategoryManageIconSlot("lunch_dining", Icons.Outlined.LunchDining),
            CategoryManageIconSlot("bakery_dining", Icons.Outlined.BakeryDining),
            CategoryManageIconSlot("icecream", Icons.Outlined.Icecream),
            CategoryManageIconSlot("cake", Icons.Outlined.Cake),
            CategoryManageIconSlot("coffee", Icons.Outlined.Coffee),
            CategoryManageIconSlot("wine_bar", Icons.Outlined.WineBar),
            CategoryManageIconSlot("set_meal", Icons.Outlined.SetMeal)
        )
    ),
    CategoryManageIconSection(
        title = "出行",
        slots = listOf(
            CategoryManageIconSlot("directions_car", Icons.Outlined.DirectionsCar),
            CategoryManageIconSlot("train", Icons.Outlined.Train),
            CategoryManageIconSlot("flight", Icons.Outlined.Flight),
            CategoryManageIconSlot("two_wheeler", Icons.Outlined.TwoWheeler),
            CategoryManageIconSlot("local_taxi", Icons.Outlined.LocalTaxi),
            CategoryManageIconSlot("directions_bus", Icons.Outlined.DirectionsBus),
            CategoryManageIconSlot("directions_boat", Icons.Outlined.DirectionsBoat)
        )
    ),
    CategoryManageIconSection(
        title = "居家与生活",
        slots = listOf(
            CategoryManageIconSlot("home", Icons.Outlined.Home),
            CategoryManageIconSlot("weekend", Icons.Outlined.Weekend),
            CategoryManageIconSlot("shopping_cart", Icons.Outlined.ShoppingCart),
            CategoryManageIconSlot("shopping_bag", Icons.Outlined.ShoppingBag),
            CategoryManageIconSlot("pets", Icons.Outlined.Pets),
            CategoryManageIconSlot("child_care", Icons.Outlined.ChildCare),
            CategoryManageIconSlot("family_restroom", Icons.Outlined.FamilyRestroom),
            CategoryManageIconSlot("face", Icons.Outlined.Face),
            CategoryManageIconSlot("build", Icons.Outlined.Build),
            CategoryManageIconSlot("flash_on", Icons.Outlined.FlashOn),
            CategoryManageIconSlot("phone", Icons.Outlined.Phone),
            CategoryManageIconSlot("smartphone", Icons.Outlined.Smartphone)
        )
    ),
    CategoryManageIconSection(
        title = "健康与学习",
        slots = listOf(
            CategoryManageIconSlot("local_hospital", Icons.Outlined.LocalHospital),
            CategoryManageIconSlot("fitness_center", Icons.Outlined.FitnessCenter),
            CategoryManageIconSlot("school", Icons.Outlined.School),
            CategoryManageIconSlot("menu_book", Icons.Outlined.MenuBook),
            CategoryManageIconSlot("people", Icons.Outlined.People),
            CategoryManageIconSlot("business", Icons.Outlined.Business),
            CategoryManageIconSlot("category", Icons.Outlined.Category)
        )
    )
)

/** 收入图标分组（条目较少，按用途分块） */
val incomeManageIconSections: List<CategoryManageIconSection> = listOf(
    CategoryManageIconSection(
        title = "常见收入",
        slots = listOf(
            CategoryManageIconSlot("account_balance_wallet", Icons.Outlined.AccountBalanceWallet),
            CategoryManageIconSlot("emoji_events", Icons.Outlined.EmojiEvents),
            CategoryManageIconSlot("work_outline", Icons.Outlined.WorkOutline),
            CategoryManageIconSlot("savings", Icons.Outlined.Savings),
            CategoryManageIconSlot("redeem", Icons.Outlined.Redeem),
            CategoryManageIconSlot("undo", Icons.Outlined.Undo),
            CategoryManageIconSlot("swap_horiz", Icons.Outlined.SwapHoriz),
            CategoryManageIconSlot("card_giftcard", Icons.Outlined.CardGiftcard),
            CategoryManageIconSlot("business", Icons.Outlined.Business),
            CategoryManageIconSlot("category", Icons.Outlined.Category)
        )
    )
)

fun firstSlotForExpenseIncome(isExpense: Boolean): CategoryManageIconSlot {
    val sections = if (isExpense) expenseManageIconSections else incomeManageIconSections
    return sections.first().slots.first()
}

fun manageSlotForIconKey(isExpense: Boolean, iconKey: String): CategoryManageIconSlot {
    val sections = if (isExpense) expenseManageIconSections else incomeManageIconSections
    sections.forEach { s ->
        s.slots.find { it.iconKey == iconKey }?.let { return it }
    }
    return firstSlotForExpenseIncome(isExpense)
}
