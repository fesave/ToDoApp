package com.machucapps.simpletodo.data

import androidx.room.TypeConverter
import com.machucapps.simpletodo.data.models.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(priority: String): Priority = Priority.valueOf(priority)
}