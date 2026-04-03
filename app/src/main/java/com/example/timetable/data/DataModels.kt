package com.example.timetable.data

import kotlinx.serialization.Serializable

@Serializable
data class SyllabusItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Serializable
data class ExamPlan(
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val commonStartTime: String = "09:30",
    val commonEndTime: String = "12:30",
    val subjectExamDates: Map<String, Long> = emptyMap()
)

@Serializable
enum class AssignmentPriority {
    Low,
    Medium,
    High
}

@Serializable
enum class AssignmentStatus {
    Pending,
    InProgress,
    Done
}

@Serializable
data class AssignmentItem(
    val id: String,
    val title: String,
    val subject: String = "",
    val dueAtMillis: Long,
    val priority: AssignmentPriority = AssignmentPriority.Medium,
    val status: AssignmentStatus = AssignmentStatus.Pending,
    val reminderOffsetsMinutes: List<Long> = emptyList(),
    val notes: String = ""
)

@Serializable
data class ClassSlot(
    val subject: String,
    val time: String,
    val startTime: String, // 24h "HH:MM"
    val endTime: String,   // 24h "HH:MM"
    val room: String,
    val icon: String,
    val iconBg: String,
    val iconColor: String,
    val type: String? = null,
    val faculty: String? = null
)

@Serializable
enum class AppDay(val shortName: String, val fullName: String) {
    Mon("Mon", "Monday"),
    Tue("Tue", "Tuesday"),
    Wed("Wed", "Wednesday"),
    Thu("Thu", "Thursday"),
    Fri("Fri", "Friday"),
    Sat("Sat", "Saturday"),
    Sun("Sun", "Sunday")
}

@Serializable
enum class AppMealType {
    breakfast, lunch, dinner
}

@Serializable
data class AppMeal(
    val type: AppMealType,
    val label: String,
    val emoji: String,
    val items: List<String>,
    val nonVegItems: List<String>? = null
)

@Serializable
enum class AppFullDay(val shortName: String, val fullName: String) {
    Sun("Sun", "Sunday"),
    Mon("Mon", "Monday"),
    Tue("Tue", "Tuesday"),
    Wed("Wed", "Wednesday"),
    Thu("Thu", "Thursday"),
    Fri("Fri", "Friday"),
    Sat("Sat", "Saturday")
}

val DefaultTimetable = mapOf(
    AppDay.Mon to listOf(
        ClassSlot("Computer Org & Arch", "09:50 – 10:40", "09:50", "10:40", "L-15", "memory", "bg-orange-500/10", "text-orange-500", faculty = "Dr. Kajal"),
        ClassSlot("VLSI Design Lab", "10:40 – 12:20", "10:40", "12:20", "Lab", "science", "bg-purple-500/10", "text-purple-500", type = "Lab", faculty = "Dr. S.A. Ahsan"),
        ClassSlot("DSP Lab", "02:00 – 03:40", "14:00", "15:40", "Lab", "graphic_eq", "bg-green-500/10", "text-green-500", type = "Lab", faculty = "Dr. Omkar Singh"),
        ClassSlot("VLSI Technology (Elective-II)", "03:40 – 04:30", "15:40", "16:30", "L-15", "auto_stories", "bg-indigo-500/10", "text-indigo-500", faculty = "Dr. Bisma Bilal"),
        ClassSlot("Open Elective", "04:30 – 05:20", "16:30", "17:20", "L-15", "menu_book", "bg-teal-500/10", "text-teal-500", faculty = "TBA")
    ),
    AppDay.Tue to listOf(
        ClassSlot("VLSI Design", "09:50 – 11:30", "09:50", "11:30", "L-15", "science", "bg-purple-500/10", "text-purple-500", faculty = "Dr. S.A. Ahsan"),
        ClassSlot("Digital Signal Processing", "11:30 – 01:10", "11:30", "13:10", "L-15", "graphic_eq", "bg-green-500/10", "text-green-500", faculty = "Dr. Omkar Singh"),
        ClassSlot("Open Elective", "04:30 – 05:20", "16:30", "17:20", "L-15", "menu_book", "bg-teal-500/10", "text-teal-500", faculty = "TBA")
    ),
    AppDay.Wed to listOf(
        ClassSlot("Data Comm. & Networking", "09:00 – 10:40", "09:00", "10:40", "L-15", "cell_tower", "bg-pink-500/10", "text-pink-500", faculty = "Dr. Yusra Banday"),
        ClassSlot("Digital Signal Processing", "10:40 – 11:30", "10:40", "11:30", "L-15", "graphic_eq", "bg-green-500/10", "text-green-500", faculty = "Dr. Omkar Singh"),
        ClassSlot("VLSI Design", "11:30 – 12:20", "11:30", "12:20", "L-15", "science", "bg-purple-500/10", "text-purple-500", faculty = "Dr. S.A. Ahsan"),
        ClassSlot("VLSI Technology (Elective-II)", "03:40 – 04:30", "15:40", "16:30", "L-15", "auto_stories", "bg-indigo-500/10", "text-indigo-500", faculty = "Dr. Bisma Bilal"),
        ClassSlot("Open Elective", "04:30 – 05:20", "16:30", "17:20", "L-15", "menu_book", "bg-teal-500/10", "text-teal-500", faculty = "TBA")
    ),
    AppDay.Thu to listOf(
        ClassSlot("Computer Org & Arch", "09:50 – 11:30", "09:50", "11:30", "L-15", "memory", "bg-orange-500/10", "text-orange-500", faculty = "Dr. Kajal"),
        ClassSlot("Data Comm. & Networking", "11:30 – 12:20", "11:30", "12:20", "L-15", "cell_tower", "bg-pink-500/10", "text-pink-500", faculty = "Dr. Yusra Banday")
    ),
    AppDay.Fri to listOf(
        ClassSlot("VLSI Technology (Elective-II)", "03:40 – 04:30", "15:40", "16:30", "L-15", "auto_stories", "bg-indigo-500/10", "text-indigo-500", faculty = "Dr. Bisma Bilal")
    )
)

val DefaultMessMenu = mapOf(
    AppFullDay.Mon to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Bread Pakoda 🥪", "Malai 🧈", "Sauce 🥫", "Green Chutney 🌿", "Kashmiri Roti 🫓 with Butter 🧈")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Baingan Bharta 🍆", "Chana Wash Dal 🥣", "Curd 🍶")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "White Chana Dal 🥣", "Matter Paneer 🧀", "Seveiyan 🍜"), listOf("Rice 🍚", "Roti 🫓", "Tomato Chicken 🍗🍅", "Seveiyan 🍜"))
    ),
    AppFullDay.Tue to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Bread 🍞", "Butter 🧈", "Jam 🍓", "Milk 🥛", "Banana 🍌")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Aloo Capsicum 🥔🫑", "Rajma Dal 🫘", "Curd 🍶")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Mixed Vegetables 🥕🥦", "Kabuli Chana 🫘"))
    ),
    AppFullDay.Wed to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Brown Bread 🍞", "Peanut Butter 🥜")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Matter Paneer 🧀", "Black Chana 🫘", "Salad 🥗")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Paneer Butter Masala 🧀", "Kabuli Chana 🫘", "Gulab Jamun 🍮"), listOf("Rice 🍚", "Roti 🫓", "Arhar Dal 🥣 / Chicken 🍗", "Gulab Jamun 🍮", "Amul Milk 🥛"))
    ),
    AppFullDay.Thu to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Pyaz Paratha 🫓🧅", "Sauce 🥫", "Kashmiri Roti 🫓 with Butter 🧈")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Mixed Vegetable 🥕🥦", "Sambar Dal 🥣", "Salad 🥗")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Aloo Palak 🥔🌿", "Rajma Dal 🫘"))
    ),
    AppFullDay.Fri to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Pav Bhaji 🍞🥔", "Kashmiri Roti 🫓")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Tomato Matter 🍅", "Rajma Dal 🫘", "Curd 🍶")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Paneer Do Pyaza 🧀🧅", "Moong Dal 🥣", "Kheer 🍮"), listOf("Rice 🍚", "Roti 🫓", "Tomato Chicken 🍗🍅"))
    ),
    AppFullDay.Sat to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Chola Samosa 🥟", "Curd 🍶")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Rice 🍚", "Roti 🫓", "Aloo Gobhi Fry 🥔🥦", "Moong Dal 🥣", "Curd 🍶")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Mixed Vegetables 🥕🥦", "Rajma Dal 🫘"))
    ),
    AppFullDay.Sun to listOf(
        AppMeal(AppMealType.breakfast, "Breakfast", "☀️", listOf("Tea ☕", "Aloo Paratha 🫓🥔", "Butter 🧈", "Sauce 🥫")),
        AppMeal(AppMealType.lunch, "Lunch", "🌤️", listOf("Vegetable Biryani 🍛", "Rajma Dal 🫘", "Vegetable Raita 🍶🥒")),
        AppMeal(AppMealType.dinner, "Dinner", "🌙", listOf("Rice 🍚", "Roti 🫓", "Paneer Bhurji 🧀", "Chana Wash Dal 🥣", "Gulab Jamun 🍮"), listOf("Rice 🍚", "Roti 🫓", "Egg Curry 🥚🍛", "Gulab Jamun 🍮"))
    )
)

fun String.to12Hour(): String {
    val p = this.split(":")
    if (p.size != 2) return this
    val h = p[0].toIntOrNull() ?: return this
    val m = p[1].toIntOrNull() ?: return this
    val suffix = if (h >= 12) "PM" else "AM"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return String.format(java.util.Locale.getDefault(), "%02d:%02d %s", h12, m, suffix)
}
