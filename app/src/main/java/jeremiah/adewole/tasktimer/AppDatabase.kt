package jeremiah.adewole.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException

private const val TAG = "AppDatabase"
private const val DATABASE_NAME =  "TaskTimer.db"
private const val DATABASE_VERSION = 3

// Now we need to make this also a singleton class with Argument
// First step is making the constructor private
// Then add get instance by checking if not null return the instance but if null create the instance
class AppDatabase private constructor(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        Log.d(TAG, "DB initialization")
    }

    override fun onCreate(db: SQLiteDatabase) {
        // CREATE Schema here
        val sSQL = """CREATE TABLE ${TaskContract.TABLE_NAME} (
            ${TaskContract.Column.TASK_ID} INTEGER PRIMARY KEY NOT NULL,
            ${TaskContract.Column.TASK_NAME} TEXT NOT NULL,
            ${TaskContract.Column.TASK_DESCRIPTION} TEXT,
            ${TaskContract.Column.TASK_SORT_ORDER} INTEGER)""".replaceIndent(" ")

        db.execSQL(sSQL)
        Log.d(TAG, "sSQL : $sSQL")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade : start")
        when(oldVersion) {
            1 -> {
                // Upgrade Login from here
            }
            else -> throw IllegalStateException("onUpgrade with unknown new version : $newVersion")
        }
    }

    // Creating singleton this way is horrible but we have a link  that has better code
    companion object {

        @Volatile
        private var instance : AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase =
            instance?: synchronized(this) {
                instance?: AppDatabase(context).also { instance = it }
        }
    }

}