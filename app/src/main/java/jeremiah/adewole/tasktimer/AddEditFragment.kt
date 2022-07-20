package jeremiah.adewole.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.lang.RuntimeException
private const val TAG = "AddEditFragment"
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var task: Task? = null
    private var listener : OnSaveClicked? = null

    private lateinit var name : EditText
    private lateinit var description : EditText
    private lateinit var sortOrder : EditText
    private lateinit var saveButton : ImageButton
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(TaskTimerViewModel::class.java) }
    // I used the activity(requireActivity) so as to share the same ViewModel instance, instead of creating another instance
//    private val viewModel by lazy { ViewModelProvider(this).get(TaskTimerViewModel::class.java) }

    // Note UI element does not exist here,
    // So we cannot do anything like UI here
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate started")
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            task = it.getString(ARG_TASK)
//        }
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView started")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    // UI elements is finally created and accessible here
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // save button on click listener
        saveButton.setOnClickListener{
            listener?.onSaveClicked()
            saveTask()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach started")
        super.onAttach(context)

        val appBar = (context as AppCompatActivity).supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)

        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement onSaveClicked")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)

        name = view.findViewById(R.id.addedit_name)
        description = view.findViewById(R.id.addedit_description)
        sortOrder = view.findViewById(R.id.addedit_sortorder)
        saveButton = view.findViewById(R.id.addedit_save)

        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                // For the purpose of editing the existing task details
                name.setText(task.name)
                description.setText(task.description)
                sortOrder.setText(String.format("${task.sortOrder}"))
            } else {
                Log.d(TAG, "onViewCreated: Adding a new task and not editing the existing one")
            }
        }

    }

    private fun taskFromUi() : Task {
        val sortOrder = if (sortOrder.text.isNotEmpty()) {
            Integer.parseInt(sortOrder.text.toString())
        } else {
            0
        }

        val newTask = Task(name.text.toString(), description.text.toString(), sortOrder)
        newTask.id = task?.id?:0

        return newTask
    }

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return ((newTask != task) &&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.sortOrder != 0)
                )
    }

    private fun saveTask() {
        val newTask = taskFromUi()
        if (newTask != task) {
            task = viewModel.saveTask(newTask)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    // We can decide to save data in onPause or onStop method
    // But this is a method that perform very fast, and also adding dialog to it is not a good ideal
    // So we prefer saving data in the onSaveClicked callback here
    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: called")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: called")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task , task to be edited or null to add a new task
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic
        fun newInstance(task : Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }

    fun createFrag(task : Task) {
        val args = Bundle()
        args.putParcelable(ARG_TASK, task)
        val fragment = AddEditFragment()
        fragment.arguments = args
    }

    fun simpleCreateFrag(task: Task) {
        val fragment = AddEditFragment.newInstance(task)
    }
}