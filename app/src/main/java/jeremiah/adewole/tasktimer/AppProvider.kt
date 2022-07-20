package jeremiah.adewole.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import java.lang.IllegalArgumentException

/**
 * Provider for the TaskTimer app. This is the only class that knows about [AppDatabase]
 */
private const val TAG = "AppProvider"
const val CONTENT_AUTHORITY = "jeremiah.adewole.tasktimer.provider"

private const val TASKS = 100
private const val TASKS_ID = 101

private const val TIMINGS = 200
private const val TIMINGS_ID = 201

private const val TASK_DURATIONS = 400
private const val TASK_DURATIONS_ID = 401

// for example, <jeremiah.adewole.tasktimer.provider/table1>
val CONTENT_AUTHORITY_URI : Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

    private  fun buildUriMatcher() : UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        // "jeremiah.adewole.tasktimer.provider/Task"
        matcher.addURI(CONTENT_AUTHORITY, TaskContract.TABLE_NAME, TASKS)
        // "jeremiah.adewole.tasktimer.provider/Task/8"
        matcher.addURI(CONTENT_AUTHORITY, "${TaskContract.TABLE_NAME}/#", TASKS_ID)

        matcher.addURI(CONTENT_AUTHORITY, TimingContract.TABLE_NAME, TIMINGS)
        matcher.addURI(CONTENT_AUTHORITY, "${TimingContract.TABLE_NAME}/#", TIMINGS_ID)

//        matcher.addURI(CONTENT_AUTHORITY, DurationContract.TABLE_NAME, TASK_DURATIONS)
//        matcher.addURI(CONTENT_AUTHORITY, "${DurationContract.TABLE_NAME}/#", TASK_DURATIONS_ID)

        return  matcher
    }

    override fun onCreate(): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun getType(uri: Uri): String? {
        val match = uriMatcher.match(uri)

        return when(match) {
            TASKS -> TaskContract.CONTENT_TYPE
            TASKS_ID -> TaskContract.CONTENT_ITEM_TYPE

            TIMINGS -> TimingContract.CONTENT_TYPE
            TIMINGS_ID -> TimingContract.CONTENT_ITEM_TYPE
//
//            TASK_DURATIONS -> DurationContract.CONTENT_TYPE
//            TASK_DURATIONS_ID -> TimingContract.CONTENT_ITEM_TYPE

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }


    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        Log.d(TAG, "query : called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")
        Log.d(TAG, "query: sortOrder $sortOrder")


        val queryBuilder = SQLiteQueryBuilder()

        when (match) {
            TASKS -> queryBuilder.tables = TaskContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TaskContract.TABLE_NAME
                val taskId = TaskContract.getId(uri)
//                queryBuilder.appendWhere("${TaskContract.Column.TASK_ID} = $taskId")

                // OR
                queryBuilder.appendWhere("${TaskContract.Column.TASK_ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }


            TIMINGS -> queryBuilder.tables = TimingContract.TABLE_NAME

            TIMINGS_ID -> {
                queryBuilder.tables = TimingContract.TABLE_NAME
                val taskId = TimingContract.getId(uri)
                queryBuilder.appendWhere("${TimingContract.Column.TIMING_ID} = $taskId")
            }

            /*
            TASK_DURATIONS -> queryBuilder.tables = DurationContract.TABLE_NAME

            TASK_DURATIONS_ID -> {
                queryBuilder.tables = TaskContract.TABLE_NAME
                val taskId = DurationContract.getId(uri)
                queryBuilder.appendWhereEscapeString("${DurationContract.Column.TASK_DURATIONS_ID} = $taskId")
            }
            */
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context!!).readableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query : rows in returned cursor = ${cursor.count}")

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert : called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        var recordId : Long
        var recordUri : Uri

        when(match) {
            TASKS -> {
                val appDatabase : AppDatabase = AppDatabase.getInstance(context!!)
                val db = appDatabase.writableDatabase

                recordId = db.insert(TaskContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    recordUri = TaskContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert a record")
                }
            }

            TIMINGS -> {
                val appDatabase : AppDatabase = AppDatabase.getInstance(context!!)
                val db = appDatabase.writableDatabase

                recordId = db.insert(TimingContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    recordUri = TimingContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert a record")
                }
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        if (recordId > 0) {
            Log.d(TAG, "insert : setting notifyChange with $uri")
            context!!.contentResolver.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting : insert record $recordUri")
        return recordUri
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete : called with uri $uri, selection : $selection, selectionArgs : $selectionArgs")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        var count : Int
        var selectionCriteria : String

        when(match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(TaskContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TaskContract.getId(uri)
                selectionCriteria = "${TaskContract.Column.TASK_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND $selection"
                }

                count = db.update(TaskContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
                Log.d(TAG, "Selection Criteria : $selectionCriteria")
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.update(TimingContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TimingContract.getId(uri)
                selectionCriteria = "${TimingContract.Column.TIMING_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND $selection"
                }

                count = db.update(TimingContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
                Log.d(TAG, "Selection Criteria : $selectionCriteria")
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        if (count > 0) {
            Log.d(TAG, "update : Setting notifyChange with $uri")
            context!!.contentResolver.notifyChange(uri, null)
        }

        Log.d(TAG, "update exit")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete : called with uri $uri, selection : $selection, selectionArgs : $selectionArgs")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        var count : Int
        var selectionCriteria : String

        when(match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(TaskContract.TABLE_NAME, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TaskContract.getId(uri)
                selectionCriteria = "${TaskContract.Column.TASK_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND $selection"
                }

                count = db.delete(TaskContract.TABLE_NAME, selectionCriteria, selectionArgs)
                Log.d(TAG, "Selection Criteria : $selectionCriteria")
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                count = db.delete(TimingContract.TABLE_NAME, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TimingContract.getId(uri)
                selectionCriteria = "${TimingContract.Column.TIMING_ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND $selection"
                }

                count = db.delete(TimingContract.TABLE_NAME, selectionCriteria, selectionArgs)
                Log.d(TAG, "Selection Criteria : $selectionCriteria")
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        if (count > 0) {
            Log.d(TAG, "update : Setting notifyChange with $uri")
            context!!.contentResolver.notifyChange(uri, null)
        }

        Log.d(TAG, "delete exit")
        return count
    }
}