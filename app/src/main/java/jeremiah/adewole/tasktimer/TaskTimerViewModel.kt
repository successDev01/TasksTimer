package jeremiah.adewole.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// why we extend AndroidViewModel and not ViewModel is because of the application property
// We need application property to be able to access contentResolver
// AndroidViewModel is the sub-class ViewModel class

private const val TAG = "TaskTimerViewModel"
class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            Log.d(TAG, "contentObserver : onChange called with $uri")
            loadTask()
        }
    }

    private var databaseCursor = MutableLiveData<Cursor>()
    val cursor : LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, "init called")
        // Register the observer for any change in the data
        getApplication<Application>().contentResolver.registerContentObserver(TaskContract.CONTENT_URI, true, contentObserver)

        // Load the Task on Main screen up
        loadTask()
    }

    fun saveTask(task : Task) : Task {

        if (task.name.isNotEmpty()) {

            val values = ContentValues()
            values.apply {
                put(TaskContract.Column.TASK_NAME, task.name)
                put(TaskContract.Column.TASK_DESCRIPTION, task.description)
                put(TaskContract.Column.TASK_SORT_ORDER, task.sortOrder)
            }

            if (task.id == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver.insert(TaskContract.CONTENT_URI, values)
                    if (uri != null) {
                        task.id = TaskContract.getId(uri)
                        Log.d(TAG, "New Task inserted with an id : ${task.id}")
                    }
                }
            } else {
                GlobalScope.launch {
                    getApplication<Application>().contentResolver.update(TaskContract.buildUriFromId(task.id), values, null, null)
                }
            }
        }

        return task
    }

    private fun loadTask() {
        val projection = arrayOf(TaskContract.Column.TASK_ID,
                                TaskContract.Column.TASK_NAME,
                                TaskContract.Column.TASK_DESCRIPTION,
                                TaskContract.Column.TASK_SORT_ORDER)

        val sortOrder = "${TaskContract.Column.TASK_SORT_ORDER}, ${TaskContract.Column.TASK_NAME}"

        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(TaskContract.CONTENT_URI,
                projection, null, null, sortOrder)

            databaseCursor.postValue(cursor)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    fun deleteTask(id: Long) {
        GlobalScope.launch {
            Log.d(TAG, "Deleting a Task")
            getApplication<Application>().contentResolver.delete(TaskContract.buildUriFromId(id), null, null)
        }
    }
}