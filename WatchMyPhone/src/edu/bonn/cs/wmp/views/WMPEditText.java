package edu.bonn.cs.wmp.views;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import de.hdm.cefx.concurrency.operations.NodePosition;
import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.service.SessionService;
import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;

public class WMPEditText extends EditText {
	
	private CollabEditingService collabService;
	private boolean remoteEditMode = false;
	
	/* 
	* TODO: atm node name is hardcoded, obtain ID from WMPComponentRegistry on view creation
	* using the following code. The problem with this is, that this node will be created by all participants
	* which probably leads to
	* 	1) a XML with more than one node having the same name (not sure if this is possible or if CEFX merges the nodes then)
	* 	2) a XML with several nodes having different names but should be the same
	* Therefore we need to obtain the node name either from the server or from the ressource id.
	*/ 
//	Text content = collabService.createTextNode(this.getText().toString());
//	el.appendChild(content);
//	collabService.insertNode("100", el, null, NodePosition.INSERT_BEFORE);
//	Log.i("WMP", "document state: " + collabService.getDocumentString());
	
	public WMPEditText(Context context) {
		super(context);
		collabService = SessionService.getInstance().getCollabEditingService();
		registerViewUpdater();
	}

	public WMPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		collabService = SessionService.getInstance().getCollabEditingService();
		registerViewUpdater();
	}

	public WMPEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		collabService = SessionService.getInstance().getCollabEditingService();
		registerViewUpdater();
	}
	
	public void setRemoteEditMode(boolean remoteEditMode){
		this.remoteEditMode = remoteEditMode;
	}
	
	public boolean isInRemoteEditMode() {
		return remoteEditMode;
	}
	
	private void registerViewUpdater(){
		new EditTextViewUpdater(this);
	}
	
	@Override
	public boolean onCheckIsTextEditor() {
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		/*
		 * TODO: check for this line, if it was already in the viewport 
		 * (alternatively check the scroll direction:
		 *  up -> line leaves viewport -> has been seen by user -> delete all spans
		 *  down -> line approaches viewport -> has not yet been seen by user -> do nothing)  
		 */
		
		super.onDraw(canvas);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (t > oldt) {
			// scroll down
			int topVisibleLine = this.getLayout().getLineForVertical(t) - 1;
			int oldTopVisibleLine = this.getLayout().getLineForVertical(oldt);
			BackgroundColorSpan[] highlights = this.getText().getSpans(this.getLayout().getLineStart(oldTopVisibleLine), this.getLayout().getLineEnd(topVisibleLine), BackgroundColorSpan.class);
			for (BackgroundColorSpan highlight : highlights) {
				this.getText().removeSpan(highlight);
			}
		} else if (t < oldt) {
			// scroll up
			Rect r = new Rect();
			this.getDrawingRect(r);
			int bottomVisibleLine = this.getLayout().getLineForVertical(t + r.height());
			int oldBottomVisibleLine = this.getLayout().getLineForVertical(oldt + r.height());
			BackgroundColorSpan[] highlights = this.getText().getSpans(this.getLayout().getLineStart(bottomVisibleLine), this.getLayout().getLineEnd(oldBottomVisibleLine), BackgroundColorSpan.class);
			for (BackgroundColorSpan highlight : highlights) {
				this.getText().removeSpan(highlight);
			}
		}
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int before,
			int after) {
		
		if (!isInRemoteEditMode() && collabService != null && collabService.isReadyForEditing()){
			Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
			if (before == 0){
				// INSERT
				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, text.subSequence(start, start+after).toString(), start);
			} else if (after == 0){
				// DELETE
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, start, before);
			} else {
				// UPDATE
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, start, before);
				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, text.subSequence(start, start+after).toString(), start);
			}
		}
		if (collabService != null) {
			Log.i("WMP", "document state: " + collabService.getDocumentString());
		}
		super.onTextChanged(text, start, before, after);
	}
	
}
