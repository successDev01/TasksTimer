package jeremiah.adewole.tasktimer

import android.content.ContentValues
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView

import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val TAG = "MainActivity"
private val DIALOG_ID_CANCEL_EDIT = 2
class MainActivity : AppCompatActivity(),
                    AddEditFragment.OnSaveClicked,
                    MainActivityFragment.OnTaskEdit,
                    AppDialog.DialogEvents{

    private val fab : FloatingActionButton by lazy { findViewById(R.id.fab) }

    private lateinit var db : SQLiteDatabase
    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            Log.d(TAG, "onCreate : fragment is up")
            showEditPane()
        } else {
            Log.d(TAG, "onCreate : fragment is not up")
            findViewById<FrameLayout>(R.id.task_details_container).visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            findViewById<FragmentContainerView>(R.id.main_fragment)?.visibility = View.VISIBLE
        }

//        val appDatabase = AppDatabase.getInstance(this)
//        db = appDatabase.readableDatabase
//        appDatabase.onCreate(db)

//        dropTable()

//        insertSomeRecord()

//        queryRecord()

//        insertRecordContentResolver()
//        updateRecordContentResolver()
//        updateMultipleRecordContentResolver()
//        deleteRecordContentResolver()
        queryRecordContentResolver()

        fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                Toast.makeText(this@MainActivity, "Hello I am clicked", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onSaveClicked() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null) {
            Log.d(TAG, "removeEditPane : fragment is alive")
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        // set the visibility of the right hand pane
        findViewById<FrameLayout>(R.id.task_details_container).visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        //and show the left hand pane
        findViewById<FragmentContainerView>(R.id.main_fragment)?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }
    private fun taskEditRequest(task : Task?) {
        Log.d(TAG, "taskEditRequest start")
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()
    }

    private fun showEditPane() {
        findViewById<FrameLayout>(R.id.task_details_container).visibility = View.VISIBLE
        // hide the left hand pane, if in single pane view
        findViewById<FragmentContainerView>(R.id.main_fragment)?.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {

        Log.d(TAG, "onPositiveDialogResult clicked with id : $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
            Log.d(TAG, "onPositiveDialogResult : clicked")
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancel_edit_dialog_message),
                    R.string.cancel_edit_dialog_positive_caption,
                    R.string.cancel_edit_dialog_negative_caption)
            } else {
                removeEditPane(fragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

       when(item.itemId) {
            R.id.mainmenu_settigs -> {
                Log.d(TAG, "onOptionsItemSelected : Settings is selected")
            }

           R.id.mainmenu_addTask -> {
               Log.d(TAG, "onOptionsItemSelected : Settings is selected")
                taskEditRequest(null)
           }

           android.R.id.home -> {
               val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
               if ((fragment is AddEditFragment) && fragment.isDirty()) {
                   showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                       getString(R.string.cancel_edit_dialog_message),
                       R.string.cancel_edit_dialog_positive_caption,
                       R.string.cancel_edit_dialog_negative_caption)
               } else {
                   removeEditPane(fragment)
               }
           }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun dropTable() : Unit {
        val dr = "DROP TABLE IF EXISTS ${TaskContract.TABLE_NAME}"
        db.execSQL(dr)
    }

    private fun insertSomeRecord() : Unit {

        val sql = "INSERT INTO ${TaskContract.TABLE_NAME} (${TaskContract.Column.TASK_NAME}," +
                " ${TaskContract.Column.TASK_DESCRIPTION}," +
                " ${TaskContract.Column.TASK_SORT_ORDER}) VALUES('Swimming', 'I want to swim', 1)"
        db.execSQL(sql)

        var contentValues = ContentValues().apply {
            put(TaskContract.Column.TASK_NAME, "Reading")
            put(TaskContract.Column.TASK_DESCRIPTION, "Learning")
            put(TaskContract.Column.TASK_SORT_ORDER, 0)
        }

        var id = db.insert("${TaskContract.TABLE_NAME}", null, contentValues)
        Log.d(TAG, "Effected row is $id")

        contentValues = ContentValues().apply {
            put(TaskContract.Column.TASK_NAME, "Learning")
            put(TaskContract.Column.TASK_SORT_ORDER, 2)
        }

        id = db.insert("${TaskContract.TABLE_NAME}", null, contentValues)
        Log.d(TAG, "Effected row is $id")
    }

    // Select using regular query for test purpose
    private fun queryRecord() : Unit {

        val cursor = db.rawQuery("SELECT * FROM ${TaskContract.TABLE_NAME}", null)
        Log.d(TAG, "queryRecord Start")
        Log.d(TAG, "****************************")
        cursor.use {

            while(it.moveToNext()) {
                //Cycle all the record
                with(cursor) {
                    val id = getLong(0)
                    val name = getString(1)
                    val description = getString(2)
                    val sortedBy = getLong(3)
                    val result = "id : $id, name : $name, description : $description, sorted by : $sortedBy"
                    Log.d(TAG, "Result : $result")
                }
            }
        }

        Log.d(TAG, "****************************")
    }

    private fun queryRecordContentResolver() : Unit {

        val projection = arrayOf(TaskContract.Column.TASK_ID,
                                TaskContract.Column.TASK_NAME,
                                TaskContract.Column.TASK_DESCRIPTION,
                                TaskContract.Column.TASK_SORT_ORDER)

        val sortColumn = TaskContract.Column.TASK_SORT_ORDER

//        val selection = "${TaskContract.Column.TASK_NAME} = ?"
//        val selectionArg = arrayOf("Task 3")
//
//        val cursor = contentResolver.query(TaskContract.buildUriFromId(1),
        val cursor = contentResolver.query(TaskContract.CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortColumn)

        Log.d(TAG, "cursor.count : ${cursor?.count}")

        Log.d(TAG, "****************************")

        cursor.use {
                while (it!!.moveToNext()) {
                    //Cycle all the record
                    with(it) {
                        val id = getLong(0)
                        val name = getString(1)
                        val description = getString(2)
                        val sortedBy = getLong(3)
                        val result =
                            "id : $id, name : $name, description : $description, sorted by : $sortedBy"
                        Log.d(TAG, "Result : $result")
                    }
                }
        }

        Log.d(TAG, "****************************")
    }

    private fun insertRecordContentResolver() {
        var contentValues = ContentValues().apply {
            put(TaskContract.Column.TASK_NAME, "Running")
            put(TaskContract.Column.TASK_DESCRIPTION, "I am running to my GLory")
            put(TaskContract.Column.TASK_SORT_ORDER, 3)
        }

        val uri = contentResolver.insert(TaskContract.CONTENT_URI, contentValues)
        Log.d(TAG, "insertRecordContentResolver uri : $uri")
        Log.d(TAG, "insertRecordContentResolver row id : ${TaskContract.getId(uri!!)}")
    }

    private fun updateRecordContentResolver() {
        var contentValues = ContentValues().apply {
            put(TaskContract.Column.TASK_NAME, "Playing")
            put(TaskContract.Column.TASK_DESCRIPTION, "I am Playing chess")
            put(TaskContract.Column.TASK_SORT_ORDER, 1)
        }

        val count = contentResolver.update(TaskContract.buildUriFromId(4), contentValues, null, null)
        Log.d(TAG, "updateRecordContentResolver id : $count")
    }

    private fun updateMultipleRecordContentResolver() {
        var contentValues = ContentValues().apply {
            put(TaskContract.Column.TASK_NAME, "Eating")
            put(TaskContract.Column.TASK_DESCRIPTION, "I am eating pizza")
        }

//        val selection = "${TaskContract.Column.TASK_SORT_ORDER} = 1"
//        val count = contentResolver.update(TaskContract.CONTENT_URI, contentValues, selection, null)

        //OR
        val selection = "${TaskContract.Column.TASK_SORT_ORDER} = ?"
        val selectionArgs = arrayOf("1")
        val count = contentResolver.update(TaskContract.CONTENT_URI, contentValues, selection, selectionArgs)

        Log.d(TAG, "updateMultipleRecordContentResolver id : $count")
    }

    private fun deleteRecordContentResolver() {
//        val count = contentResolver.delete(TaskContract.buildUriFromId(2), null, null)
//        Log.d(TAG, "deleteRecordContentResolver id : $count")

        //OR
        val selection = "${TaskContract.Column.TASK_NAME} = ?"
        val selectionArgs = arrayOf("Learning")
        val count = contentResolver.delete(TaskContract.CONTENT_URI, selection, selectionArgs)
    }
}