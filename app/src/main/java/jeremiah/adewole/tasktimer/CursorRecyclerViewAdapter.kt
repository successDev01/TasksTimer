package jeremiah.adewole.tasktimer

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException

class TaskViewHolder(private val view: View) : RecyclerView.ViewHolder(view){
    val tliName : TextView = view.findViewById(R.id.tli_name)
    val tliDescription : TextView = view.findViewById(R.id.tli_description)
    val tliDeleteButton : ImageButton = view.findViewById(R.id.tli_delete)
    val tliEditButton : ImageButton = view.findViewById(R.id.tli_edit)

    fun bind(task: Task, listener : CursorRecyclerViewAdapter.OnClickListener) {
        tliName.text = task.name
        tliDescription.text = task.description
        tliDeleteButton.visibility = View.VISIBLE
        tliEditButton.visibility = View.VISIBLE

        tliEditButton.setOnClickListener{
            listener.onEditButtonClicked(task)
        }

        tliDeleteButton.setOnClickListener {
            listener.onDeleteButtonClicked(task)
        }

        view.setOnLongClickListener {
            listener.onLongClicked(task)
            true
        }

    }
}
private const val TAG = "CursorRecycler"
class CursorRecyclerViewAdapter(private var cursor : Cursor?, private val listener : OnClickListener) : RecyclerView.Adapter<TaskViewHolder>() {

    interface OnClickListener {
        fun onEditButtonClicked(task : Task)
        fun onDeleteButtonClicked(task : Task)
        fun onLongClicked(task : Task)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
//        Log.d(TAG, "onCreateViewHolder : called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder : called")

        val cursor = cursor // To avoid smart casting problem

        if (cursor == null || cursor.count == 0) {
            holder.tliName.setText(R.string.instruction_heading)
            holder.tliDescription.setText(R.string.instructions)
            holder.tliDeleteButton.visibility = View.GONE
            holder.tliEditButton.visibility = View.GONE
        } else {

            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Could not move the cursor to the position $position ")
            } else {
//                val task = Task(cursor.getString(cursor.getColumnIndex(TaskContract.Column.TASK_NAME)),
//                                cursor.getString(cursor.getColumnIndex(TaskContract.Column.TASK_DESCRIPTION)),
//                                cursor.getInt(cursor.getColumnIndex(TaskContract.Column.TASK_SORT_ORDER)))
//                task.id = cursor.getLong(cursor.getColumnIndex(TaskContract.Column.TASK_ID))

                Log.d(TAG, "onBindViewHolder : cursor count : ${cursor.count}")

                val task = Task(cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3))
                task.id = cursor.getLong(0)

                holder.bind(task, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount : called")

        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0) {
            1
        } else {
            cursor.count
        }

        Log.d(TAG, "getItemCount : $count")
        return count
    }

    fun swapCursor(newCursor: Cursor?) : Cursor? {
        Log.d(TAG, "swapCursor : called")

        if (newCursor == cursor ) {
            return null
        }

        val numItem = itemCount
        val oldCursor = cursor
        cursor = newCursor

        if (newCursor != null) {
            notifyDataSetChanged()
        } else {
            notifyItemRemoved(numItem)
        }

        return oldCursor
    }
}