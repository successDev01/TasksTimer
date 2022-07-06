package jeremiah.adewole.tasktimer

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

// Kotlin Singleton without argument
object TimingContract {
    internal const val TABLE_NAME = "Timing"

    /**
     * The URI to Access the Task Table
     */
    val CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Column {
//        const val TASK_ID = "_Id"
        const val TIMING_ID = BaseColumns._ID
        const val TIMING_TASK_ID = "TaskId"
        const val TIMING_START_TIME = "StartTime"
        const val TIMING_DESCRIPTION = "Description"
    }

    fun getId(uri : Uri) : Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id : Long) : Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }


}