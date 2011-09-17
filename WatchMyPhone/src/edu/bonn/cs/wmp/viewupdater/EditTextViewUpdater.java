package edu.bonn.cs.wmp.viewupdater;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import android.app.Activity;
import android.graphics.Color;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.awareness.events.AwarenessEventTypes;
import de.hdm.cefx.concurrency.operations.DeleteOperationImpl;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateDeleteOperation;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import edu.bonn.cs.wmp.application.WMPApplication;
import edu.bonn.cs.wmp.views.WMPEditText;
import edu.bonn.cs.wmp.views.WMPView;

/**
 * This view updater is normally used together with {@link WMPEditText} to
 * provide this {@link WMPView} with updates from the Collaborative Editing Framework.
 * 
 * @author Sven Bendel
 *
 */
public class EditTextViewUpdater extends ViewUpdater {
	private WMPEditText editText;
	private WMPApplication app;
	
	public EditTextViewUpdater(WMPEditText editText) {
		this.editText = editText;
		app = WMPApplication.getInstance();
		app.registerViewUpdater(this);
	}
	
	@Override
	public WMPView getWMPView() {
		return editText;
	}

	@Override
	public void notifyOfAwarenessEvent(final AwarenessEvent event) {
		
		app.getCollabEditingService();
		
		// TODO: create getter for operation instead of protected variable
		
		if (operation instanceof InsertOperationImpl) {
			//TODO: not needed any more - may be removed in the future

			// new node (i.e. in case of late join)
			InsertOperationImpl insOp = (InsertOperationImpl) operation;
			// TODO: very basic implementation which heavily relies on the
			// given structure in our CEFX documents
			if (insOp.getInsertNode() instanceof Element
					&& insOp.getInsertNode().getFirstChild() instanceof Text) {
				final Text textNode = (Text) insOp.getInsertNode()
						.getFirstChild().cloneNode(true);
				((Activity) editText.getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						editText.setText(textNode.getData());
						editText.invalidate();
					}
				});
			}
		} else if (operation instanceof UpdateOperationImpl) {
			final UpdateOperationImpl updateOperation = (UpdateOperationImpl) operation;
			if (updateOperation.getDISOperation() instanceof UpdateInsertOperation) {
				final UpdateInsertOperation upInsOp = (UpdateInsertOperation) updateOperation
						.getDISOperation();

				((Activity) editText.getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						int start = upInsOp.getTextPos();
						Editable text = Editable.Factory.getInstance()
								.newEditable(upInsOp.getText());
						// TODO: color should be chosen based on modifying
						// collaborator

						// TODO: determination of LOCAL or REMOTE operation
						// should happen much earlier, as this is also
						// interesting for other view updaters ->
						// EventPropagator
						try {
							if (updateOperation.getClientId() != app.getCollabEditingService()
									.getCEFXUserID()) {
								/*
								 *  TODO: better use own subclass of BackgroundColorSpan so we don't
								 *  accidently remove other BackgroundColorSpans in WMPEditText#onScrollChanged()
								 */
								BackgroundColorSpan background = new BackgroundColorSpan(
										Color.argb(100, 255, 255, 0));
								text.setSpan(background, 0, text.length(),
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							} else {
								// TODO: advance cursor/selection?
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						editText.getText().replace(start, start, text);
						editText.invalidate();
					}
				});
			} else if (updateOperation.getDISOperation() instanceof UpdateDeleteOperation) {
				final UpdateDeleteOperation upDelOp = (UpdateDeleteOperation) updateOperation
						.getDISOperation();
				((Activity) editText.getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO: colorize delete operation
						editText.getText().replace(upDelOp.getTextPos(),
								upDelOp.getTextPos() + upDelOp.getLength(),
								"");
						editText.invalidate();
					}
				});
			}
		} else if (operation instanceof DeleteOperationImpl) {
			// XXX: not necessary for WMP?
		}

	}

	@Override
	public boolean hasInterestIn(AwarenessEvent event) {
		/* 
		 * TODO: check if the operation is interesting for this particular view updater
		 * by comparing the node id to the resource id of our parent (CollabEditingService has
		 * a method for getting the ResourceID for a CEFX-ID (getNodeName()) 
		 */
		return super.hasInterestIn(event) && event.getType().equals(
				AwarenessEventTypes.OPERATION_EXECUTION.toString());
	}
}
