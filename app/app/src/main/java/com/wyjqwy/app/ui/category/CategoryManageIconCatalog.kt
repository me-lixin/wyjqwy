package com.wyjqwy.app.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/** 添加分类页：分组展示的图标槽位（iconKey 与后端 icon 字段对齐，英文 snake_case） */
data class CategoryManageIconSlot(
    val iconKey: String,
    val icon: ImageVector
)

data class CategoryManageIconSection(
    val title: String,
    val slots: List<CategoryManageIconSlot>
)

/** 自定义支出图标库（系统默认分类不在这里编辑） */
val expenseManageIconSections: List<CategoryManageIconSection> = listOf(
    CategoryManageIconSection(
        title = "吃喝",
        slots = listOf(
            CategoryManageIconSlot("breakfast", Icons.Outlined.BakeryDining),
            CategoryManageIconSlot("lunch", Icons.Outlined.RiceBowl),
            CategoryManageIconSlot("dinner", Icons.Outlined.DinnerDining),
            CategoryManageIconSlot("late_night", Icons.Outlined.RamenDining),
            CategoryManageIconSlot("takeout", Icons.Outlined.TakeoutDining),
            CategoryManageIconSlot("grocery", Icons.Outlined.LocalGroceryStore),
            CategoryManageIconSlot("fruit", Icons.Outlined.Eco),
            CategoryManageIconSlot("coffee", Icons.Outlined.LocalCafe),
            CategoryManageIconSlot("milk_tea", Icons.Outlined.EmojiFoodBeverage),
            CategoryManageIconSlot("bakery", Icons.Outlined.Cake),
            CategoryManageIconSlot("liquor", Icons.Outlined.Liquor),
            CategoryManageIconSlot("snack_candy", Icons.Outlined.Cookie),
            CategoryManageIconSlot("group_dining", Icons.Outlined.Celebration)
        )
    ),
    CategoryManageIconSection(
        title = "出行",
        slots = listOf(
            CategoryManageIconSlot("bus", Icons.Outlined.DirectionsBus),
            CategoryManageIconSlot("subway", Icons.Outlined.DirectionsSubway),
            CategoryManageIconSlot("taxi", Icons.Outlined.LocalTaxi),
            CategoryManageIconSlot("bike_share", Icons.Outlined.DirectionsBike),
            CategoryManageIconSlot("train", Icons.Outlined.Train),
            CategoryManageIconSlot("high_speed_rail", Icons.Outlined.DirectionsRailway),
            CategoryManageIconSlot("flight", Icons.Outlined.Flight),
            CategoryManageIconSlot("ferry", Icons.Outlined.DirectionsBoat),
            CategoryManageIconSlot("parking", Icons.Outlined.LocalParking),
            CategoryManageIconSlot("toll_road", Icons.Outlined.AddRoad),
            CategoryManageIconSlot("gas", Icons.Outlined.LocalGasStation),
            CategoryManageIconSlot("ev_charging", Icons.Outlined.EvStation),
            CategoryManageIconSlot("car_wash", Icons.Outlined.LocalCarWash),
            CategoryManageIconSlot("driving", Icons.Outlined.DriveEta)
        )
    ),
    CategoryManageIconSection(
        title = "穿搭",
        slots = listOf(
            CategoryManageIconSlot("clothes", Icons.Outlined.Checkroom),
            CategoryManageIconSlot("pants", Icons.Outlined.Accessibility),
            CategoryManageIconSlot("shoes", Icons.Outlined.RollerSkating),
            CategoryManageIconSlot("hat", Icons.Outlined.Face),
            CategoryManageIconSlot("underwear", Icons.Outlined.Checkroom),
            CategoryManageIconSlot("bag", Icons.Outlined.ShoppingBag),
            CategoryManageIconSlot("watch", Icons.Outlined.Watch),
            CategoryManageIconSlot("dry_clean", Icons.Outlined.DryCleaning)
        )
    ),
    CategoryManageIconSection(
        title = "日用",
        slots = listOf(
            CategoryManageIconSlot("toiletries", Icons.Outlined.Soap),
            CategoryManageIconSlot("tissue", Icons.Outlined.Sanitizer),
            CategoryManageIconSlot("cleaning", Icons.Outlined.CleaningServices),
            CategoryManageIconSlot("kitchenware", Icons.Outlined.Kitchen),
            CategoryManageIconSlot("bedding", Icons.Outlined.Bed),
            CategoryManageIconSlot("hardware", Icons.Outlined.Hardware),
            CategoryManageIconSlot("shipping", Icons.Outlined.LocalShipping)
        )
    ),
    CategoryManageIconSection(
        title = "居住",
        slots = listOf(
            CategoryManageIconSlot("rent", Icons.Outlined.House),
            CategoryManageIconSlot("mortgage", Icons.Outlined.RealEstateAgent),
            CategoryManageIconSlot("property_fee", Icons.Outlined.Domain),
            CategoryManageIconSlot("water", Icons.Outlined.WaterDrop),
            CategoryManageIconSlot("electric", Icons.Outlined.Bolt),
            CategoryManageIconSlot("gas_home", Icons.Outlined.LocalFireDepartment),
            CategoryManageIconSlot("heating", Icons.Outlined.Thermostat),
            CategoryManageIconSlot("broadband", Icons.Outlined.Router),
            CategoryManageIconSlot("repair", Icons.Outlined.Handyman),
            CategoryManageIconSlot("garden", Icons.Outlined.Yard),
            CategoryManageIconSlot("hotel", Icons.Outlined.Hotel)
        )
    ),
    CategoryManageIconSection(
        title = "通讯数码",
        slots = listOf(
            CategoryManageIconSlot("phone_plan", Icons.Outlined.Smartphone),
            CategoryManageIconSlot("data_plan", Icons.Outlined.CellWifi),
            CategoryManageIconSlot("phone_accessory", Icons.Outlined.Headphones),
            CategoryManageIconSlot("pc_peripheral", Icons.Outlined.Mouse),
            CategoryManageIconSlot("software_topup", Icons.Outlined.CloudDownload),
            CategoryManageIconSlot("game_spend", Icons.Outlined.VideogameAsset),
            CategoryManageIconSlot("video_member", Icons.Outlined.Movie),
            CategoryManageIconSlot("music_member", Icons.Outlined.MusicNote),
            CategoryManageIconSlot("photo_gear", Icons.Outlined.CameraAlt)
        )
    ),
    CategoryManageIconSection(
        title = "美妆个护",
        slots = listOf(
            CategoryManageIconSlot("skincare", Icons.Outlined.Spa),
            CategoryManageIconSlot("makeup", Icons.Outlined.FaceRetouchingNatural),
            CategoryManageIconSlot("perfume", Icons.Outlined.Air),
            CategoryManageIconSlot("haircut", Icons.Outlined.ContentCut),
            CategoryManageIconSlot("nail", Icons.Outlined.PanTool),
            CategoryManageIconSlot("cosmetic_surgery", Icons.Outlined.HealthAndSafety),
            CategoryManageIconSlot("sanitary", Icons.Outlined.WaterDrop)
        )
    ),
    CategoryManageIconSection(
        title = "娱乐休闲",
        slots = listOf(
            CategoryManageIconSlot("cinema", Icons.Outlined.Theaters),
            CategoryManageIconSlot("escape_room", Icons.Outlined.VpnKey),
            CategoryManageIconSlot("ktv", Icons.Outlined.Mic),
            CategoryManageIconSlot("show", Icons.Outlined.TheaterComedy),
            CategoryManageIconSlot("travel", Icons.Outlined.CardTravel),
            CategoryManageIconSlot("ticket", Icons.Outlined.ConfirmationNumber),
            CategoryManageIconSlot("amusement_park", Icons.Outlined.Park),
            CategoryManageIconSlot("board_game", Icons.Outlined.Casino)
        )
    ),
    CategoryManageIconSection(
        title = "健康医疗",
        slots = listOf(
            CategoryManageIconSlot("medicine", Icons.Outlined.Medication),
            CategoryManageIconSlot("outpatient", Icons.Outlined.LocalHospital),
            CategoryManageIconSlot("hospital_stay", Icons.Outlined.BedroomParent),
            CategoryManageIconSlot("checkup", Icons.Outlined.MonitorHeart),
            CategoryManageIconSlot("dental", Icons.Outlined.MedicalInformation),
            CategoryManageIconSlot("insurance_med", Icons.Outlined.VerifiedUser),
            CategoryManageIconSlot("supplement", Icons.Outlined.Healing)
        )
    ),
    CategoryManageIconSection(
        title = "运动健身",
        slots = listOf(
            CategoryManageIconSlot("gym", Icons.Outlined.FitnessCenter),
            CategoryManageIconSlot("swim", Icons.Outlined.Pool),
            CategoryManageIconSlot("ball_sports", Icons.Outlined.SportsBasketball),
            CategoryManageIconSlot("yoga", Icons.Outlined.SelfImprovement),
            CategoryManageIconSlot("outdoor_gear", Icons.Outlined.Terrain),
            CategoryManageIconSlot("sportswear", Icons.Outlined.Sports)
        )
    ),
    CategoryManageIconSection(
        title = "教育提升",
        slots = listOf(
            CategoryManageIconSlot("books", Icons.Outlined.MenuBook),
            CategoryManageIconSlot("course", Icons.Outlined.Class),
            CategoryManageIconSlot("exam_fee", Icons.Outlined.TextSnippet),
            CategoryManageIconSlot("stationery", Icons.Outlined.Edit),
            CategoryManageIconSlot("knowledge_pay", Icons.Outlined.Paid),
            CategoryManageIconSlot("hobby_class", Icons.Outlined.Palette)
        )
    ),
    CategoryManageIconSection(
        title = "人情社交",
        slots = listOf(
            CategoryManageIconSlot("gift_cash_out", Icons.Outlined.FavoriteBorder),
            CategoryManageIconSlot("baby_gift", Icons.Outlined.ChildFriendly),
            CategoryManageIconSlot("lucky_money_out", Icons.Outlined.Redeem),
            CategoryManageIconSlot("elder_care", Icons.Outlined.Elderly),
            CategoryManageIconSlot("treat_gift", Icons.Outlined.CardGiftcard),
            CategoryManageIconSlot("charity", Icons.Outlined.VolunteerActivism),
            CategoryManageIconSlot("lend_money", Icons.Outlined.MoneyOff)
        )
    ),
    CategoryManageIconSection(
        title = "金融保险",
        slots = listOf(
            CategoryManageIconSlot("credit_repay", Icons.Outlined.CreditCard),
            CategoryManageIconSlot("installment", Icons.Outlined.AccountBalanceWallet),
            CategoryManageIconSlot("biz_insurance", Icons.Outlined.Gavel),
            CategoryManageIconSlot("service_fee", Icons.Outlined.Receipt),
            CategoryManageIconSlot("penalty", Icons.Outlined.WarningAmber),
            CategoryManageIconSlot("fund_sip", Icons.Outlined.ShowChart),
            CategoryManageIconSlot("stock_loss", Icons.Outlined.TrendingDown)
        )
    ),
    CategoryManageIconSection(
        title = "宠物伴侣",
        slots = listOf(
            CategoryManageIconSlot("pet_food", Icons.Outlined.Pets),
            CategoryManageIconSlot("pet_snack", Icons.Outlined.CrueltyFree),
            CategoryManageIconSlot("pet_toy", Icons.Outlined.SmartToy),
            CategoryManageIconSlot("pet_vet", Icons.Outlined.MedicalServices),
            CategoryManageIconSlot("pet_spa", Icons.Outlined.Shower),
            CategoryManageIconSlot("pet_hotel", Icons.Outlined.House)
        )
    ),
    CategoryManageIconSection(
        title = "母婴育儿",
        slots = listOf(
            CategoryManageIconSlot("baby_food", Icons.Outlined.ChildCare),
            CategoryManageIconSlot("diaper", Icons.Outlined.BabyChangingStation),
            CategoryManageIconSlot("baby_clothes", Icons.Outlined.ChildFriendly),
            CategoryManageIconSlot("stroller", Icons.Outlined.Toys)
        )
    )
)

/** 自定义收入图标库 */
val incomeManageIconSections: List<CategoryManageIconSection> = listOf(
    CategoryManageIconSection(
        title = "工作与补贴",
        slots = listOf(
            CategoryManageIconSlot("base_salary", Icons.Outlined.AttachMoney),
            CategoryManageIconSlot("merit_bonus", Icons.Outlined.EmojiEvents),
            CategoryManageIconSlot("overtime_pay", Icons.Outlined.MoreTime),
            CategoryManageIconSlot("attendance_bonus", Icons.Outlined.FactCheck),
            CategoryManageIconSlot("year_end", Icons.Outlined.Celebration),
            CategoryManageIconSlot("commission", Icons.Outlined.MonetizationOn),
            CategoryManageIconSlot("trans_allowance", Icons.Outlined.Commute),
            CategoryManageIconSlot("meal_allowance", Icons.Outlined.RestaurantMenu),
            CategoryManageIconSlot("housing_allowance", Icons.Outlined.Cottage),
            CategoryManageIconSlot("phone_allowance", Icons.Outlined.SettingsCell),
            CategoryManageIconSlot("travel_claim", Icons.Outlined.Luggage),
            CategoryManageIconSlot("season_allowance", Icons.Outlined.WbSunny),
            CategoryManageIconSlot("severance", Icons.Outlined.DirectionsWalk)
        )
    ),
    CategoryManageIconSection(
        title = "副业与商业",
        slots = listOf(
            CategoryManageIconSlot("side_salary", Icons.Outlined.WorkOutline),
            CategoryManageIconSlot("labor_pay", Icons.Outlined.Engineering),
            CategoryManageIconSlot("royalty", Icons.Outlined.HistoryEdu),
            CategoryManageIconSlot("design_income", Icons.Outlined.DesignServices),
            CategoryManageIconSlot("live_tips", Icons.Outlined.LiveTv),
            CategoryManageIconSlot("we_media", Icons.Outlined.Campaign),
            CategoryManageIconSlot("ecommerce", Icons.Outlined.Storefront),
            CategoryManageIconSlot("daigou", Icons.Outlined.ShoppingBag),
            CategoryManageIconSlot("stall", Icons.Outlined.Store)
        )
    ),
    CategoryManageIconSection(
        title = "投资与理财",
        slots = listOf(
            CategoryManageIconSlot("bank_interest", Icons.Outlined.Savings),
            CategoryManageIconSlot("fund_profit", Icons.Outlined.TrendingUp),
            CategoryManageIconSlot("stock_dividend", Icons.Outlined.CandlestickChart),
            CategoryManageIconSlot("bond_yield", Icons.Outlined.RequestQuote),
            CategoryManageIconSlot("money_fund", Icons.Outlined.AccountBalance),
            CategoryManageIconSlot("rent_income", Icons.Outlined.Key),
            CategoryManageIconSlot("equity_dividend", Icons.Outlined.PieChartOutline),
            CategoryManageIconSlot("lottery", Icons.Outlined.Casino)
        )
    ),
    CategoryManageIconSection(
        title = "人情与意外",
        slots = listOf(
            CategoryManageIconSlot("wedding_cash_in", Icons.Outlined.Favorite),
            CategoryManageIconSlot("red_packet_in", Icons.Outlined.CardGiftcard),
            CategoryManageIconSlot("elder_gift", Icons.Outlined.Elderly),
            CategoryManageIconSlot("loan_back", Icons.Outlined.AssignmentReturn),
            CategoryManageIconSlot("resell", Icons.Outlined.Recycling),
            CategoryManageIconSlot("refund_in", Icons.Outlined.CurrencyExchange),
            CategoryManageIconSlot("compensation", Icons.Outlined.Gavel),
            CategoryManageIconSlot("scholarship", Icons.Outlined.School),
            CategoryManageIconSlot("promo_deal", Icons.Outlined.Loyalty),
            CategoryManageIconSlot("serendipity", Icons.Outlined.Search)
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
    (if (isExpense) expenseCategoryPresets else incomeCategoryPresets)
        .find { it.iconKey == iconKey }
        ?.let { return CategoryManageIconSlot(it.iconKey, it.icon) }
    return firstSlotForExpenseIncome(isExpense)
}
