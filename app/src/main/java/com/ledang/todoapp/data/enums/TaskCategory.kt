package com.ledang.todoapp.data.enums

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.ledang.todoapp.R

enum class TaskCategory(
    @DrawableRes val iconRes: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val lightColorRes: Int,
    val displayName: String
) {
    WORK(
        iconRes = R.drawable.ic_work_list,
        colorRes = R.color.category_work,
        lightColorRes = R.color.category_work_light,
        displayName = "Work"
    ),
    PERSONAL(
        iconRes = R.drawable.ic_personal_list,
        colorRes = R.color.category_personal,
        lightColorRes = R.color.category_personal_light,
        displayName = "Personal"
    ),
    STUDY(
        iconRes = R.drawable.ic_study_list,
        colorRes = R.color.category_study,
        lightColorRes = R.color.category_study_light,
        displayName = "Study"
    ),
    SPORTS(
        iconRes = R.drawable.ic_sport_list,
        colorRes = R.color.category_sports,
        lightColorRes = R.color.category_sports_light,
        displayName = "Sports"
    )
}
