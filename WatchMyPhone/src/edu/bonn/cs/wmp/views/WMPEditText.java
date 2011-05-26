package edu.bonn.cs.wmp.views;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.hdm.cefx.concurrency.operations.NodePosition;

import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;
import edu.bonn.cs.wmp.viewupdater.SessionService;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class WMPEditText extends EditText {
	
	private CollabEditingService collabService;
	private EditTextViewUpdater viewUpdater;
	
	public WMPEditText(Context context) {
		super(context);
		registerViewUpdater();
	}

	public WMPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		registerViewUpdater();
	}

	public WMPEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		registerViewUpdater();
	}
	
	private void registerViewUpdater(){
		viewUpdater = new EditTextViewUpdater(this);
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int before,
			int after) {
		
		// TODO: shouldn't be triggered for remote modifications
		collabService = SessionService.getInstance().getCollabEditingService();
		
		if (collabService != null && collabService.isReadyForEditing()){
			// TODO: search element by name (edit_text)
			Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
//			if (!el.hasChildNodes()){
//				Text content = collabService.createTextNode("");
//				collabService.insertNode(el, content, null, NodePosition.INSERT_BEFORE);
//			}
			if (after > 0){
				// INSERT
				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, text.subSequence(start, start+after).toString(), start);
			} else if (before > 0){
				// DELETE
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, start, before);
			} else {
				// TODO: ?
			}
			Log.i("WMP", "document state: " + collabService.getDocumentString());
		}
		super.onTextChanged(text, start, before, after);
	}
	
	// TODO: not necessary any more as we obtain the CollabEditingService dynamically in onTextChanged()
	public void setCollabEditingService(CollabEditingService collabService){
		this.collabService = collabService;
		// TODO: atm node name is hardcoded, obtain ID from WMPComponentRegistry
		// we won't run the following code for now, as propagation should start not sooner than all clients have joined the session
//		Text content = collabService.createTextNode(this.getText().toString());
//		el.appendChild(content);
//		collabService.insertNode("100", el, null, NodePosition.INSERT_BEFORE);
//		Log.i("WMP", "document state: " + collabService.getDocumentString());
	}

}
