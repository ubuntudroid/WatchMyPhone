package edu.bonn.cs.wmp.viewupdater;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.DeleteOperationImpl;
import de.hdm.cefx.concurrency.operations.ExecutionContext;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.views.WMPEditText;

public class EditTextViewUpdater extends ViewUpdater {
	
	private WMPEditText editText;
	
	public EditTextViewUpdater(WMPEditText editText){
		this.editText = editText;
	}
	
	@Override
	public void notifyOfAwarenessEvent(AwarenessEvent event) {
		
		CollabEditingService collabEditingService = SessionService.getInstance().getCollabEditingService();
		
		// TODO: create getter for operation instead of protected variable
		if (operation instanceof InsertOperationImpl) {
			// nothing to do for WMPEditText
		} else if (operation instanceof UpdateOperationImpl) {
			UpdateOperationImpl updateOp = (UpdateOperationImpl) operation; // TODO: Typ feststellen und dann zu UpdateInsertOperationImpl casten
//			editText.append(updateOp.getTargetText((ExecutionContext) collabEditingService.getCEFXController().getConcurrencyController()), updateOp.get, end)
		} else if (operation instanceof DeleteOperationImpl){
			// XXX: not necessary for WMP?
		}
		
	}

}
