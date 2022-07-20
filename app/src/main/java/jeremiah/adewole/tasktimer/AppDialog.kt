package jeremiah.adewole.tasktimer

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import java.lang.IllegalArgumentException
import kotlin.ClassCastException
import kotlin.coroutines.coroutineContext

const val DIALOG_ID = "id"
const val DIALOG_MESSAGE = "message"
const val DIALOG_POSITIVE_RID = "positive_rid"
const val DIALOG_NEGATIVE_RID = "negative_rid"

private val TAG = "AppDialog"
class AppDialog : AppCompatDialogFragment() {

    private var dialogEvent : DialogEvents? = null

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach : called")
        super.onAttach(context)


        // Activity/Fragment must implement AppDialog.DialogEvents interface
        dialogEvent = try {
             parentFragment as DialogEvents
        } catch (e : NullPointerException) {
            try {
                // No parent fragment, so call back the Activity instead
                context as DialogEvents
            }
            catch (e: ClassCastException) {
                // Activity doesn't implement the interface
                throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
            }
        } catch (e : ClassCastException) {
            throw ClassCastException("Fragment $parentFragment must implements AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val arguments = arguments
        var messageString : String? = null
        var dialogId : Int = 0
        var positiveStringId : Int = 0
        var negativeStringId : Int = 0

        if(arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID)
            messageString = arguments.getString(DIALOG_MESSAGE)

            if (dialogId == 0 || messageString == null) {
                throw IllegalArgumentException("DIALOG_ID/DIALOG_MESSAGE is not present in the bundle")
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID)
            if (positiveStringId == 0) {
                positiveStringId = R.string.ok
            }

            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID)
            if (negativeStringId == 0) {
                negativeStringId = R.string.cancel
            }
        } else {
            throw IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle")
        }

        val builder = AlertDialog.Builder(activity)

        return builder.setMessage(messageString)
            .setPositiveButton(positiveStringId) { dialogInterface, which ->
                dialogEvent?.onPositiveDialogResult(dialogId, arguments)
            }
            .setNegativeButton(negativeStringId) { dialogInterface, which ->
//                dialogEvent?.onNegativeDialogResult(dialogId, arguments)
            }
            .create()

    }

    override fun onDetach() {
        super.onDetach()
        dialogEvent = null
    }

    override fun onCancel(dialog: DialogInterface) {
//        super.onCancel(dialog) // commented out because it does nothing
//        dialogEvent?.onDialogCanceled(dialogId)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog) // comment out if any strange result
    }

    internal interface DialogEvents {
        fun onPositiveDialogResult(dialogId : Int, args : Bundle)
//        fun onNegativeDialogResult(dialogId : Int, args: Bundle)
//        fun onDialogCanceled(dialogId : Int)
    }
}