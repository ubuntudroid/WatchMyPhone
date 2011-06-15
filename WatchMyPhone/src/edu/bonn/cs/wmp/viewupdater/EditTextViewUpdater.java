package edu.bonn.cs.wmp.viewupdater;

import org.jivesoftware.smack.packet.Registration;

import android.text.Editable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView.BufferType;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.DeleteOperationImpl;
import de.hdm.cefx.concurrency.operations.ExecutionContext;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateDeleteOperation;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import edu.bonn.cs.wmp.MainActivity;
import edu.bonn.cs.wmp.service.CollabEditingService;
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

		CollabEditingService collabEditingService = SessionService.getInstance().getCollabEditingService();
		
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
						editText.getText().replace(upInsOp.getTextPos(), upInsOp.getTextPos(), upInsOp.getText());
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
