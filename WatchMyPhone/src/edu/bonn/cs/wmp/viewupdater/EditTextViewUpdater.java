package edu.bonn.cs.wmp.viewupdater;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.DeleteOperationImpl;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateDeleteOperation;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import edu.bonn.cs.wmp.MainActivity;
import edu.bonn.cs.wmp.service.SessionService;
import edu.bonn.cs.wmp.views.WMPEditText;

public class EditTextViewUpdater extends ViewUpdater {
	
	private WMPEditText editText;
	
	// TODO: hasInterestIn implementieren
	
	public EditTextViewUpdater(WMPEditText editText){
		this.editText = editText;
		SessionService.getInstance().getCollabEditingService().getAwarenessController().registerWidget(this);
	}
	
	@Override
	public void notifyOfAwarenessEvent(AwarenessEvent event) {
		
		/*
		 * TODO: necessary? CollabEditingService is needed to call this method, thus it should be already there
		 * so there is no need to create an instance
		 */
		SessionService.getInstance().getCollabEditingService();
		
		// TODO: create getter for operation instead of protected variable
		if (operation instanceof InsertOperationImpl) {
			// nothing to do for WMPEditText
		} else if (operation instanceof UpdateOperationImpl) {
			UpdateOperationImpl updateOperation = (UpdateOperationImpl) operation;
			if (updateOperation.getDISOperation() instanceof UpdateInsertOperation) {
				final UpdateInsertOperation upInsOp = (UpdateInsertOperation) updateOperation.getDISOperation();
				MainActivity.getInstance().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						editText.setRemoteEditMode(true);
						// TODO: color should be chosen based on modifying collaborator
						BackgroundColorSpan background = new BackgroundColorSpan(Color.argb(100, 255, 255, 0));
						int start = upInsOp.getTextPos();
						Editable text = Editable.Factory.getInstance().newEditable(upInsOp.getText());
						text.setSpan(background, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						editText.getText().replace(start, start, text);
						editText.invalidate();
						editText.setRemoteEditMode(false);
					}
				});
			} else if (updateOperation.getDISOperation() instanceof UpdateDeleteOperation) {
				final UpdateDeleteOperation upDelOp = (UpdateDeleteOperation) updateOperation.getDISOperation();
				MainActivity.getInstance().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						editText.setRemoteEditMode(true);
						editText.getText().replace(upDelOp.getTextPos(), upDelOp.getTextPos()+upDelOp.getLength(), "");
						editText.setRemoteEditMode(false);
					}
				});
			}
		} else if (operation instanceof DeleteOperationImpl){
			// XXX: not necessary for WMP?
		}
		
	}

}
