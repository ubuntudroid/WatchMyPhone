package edu.bonn.cs.wmp.views;

import java.lang.Character.UnicodeBlock;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.hdm.cefx.concurrency.operations.NodePosition;

import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.service.SessionService;
import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;

import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
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
		collabService = SessionService.getInstance().getCollabEditingService();
		registerViewUpdater();
	}
	
	private void registerViewUpdater(){
		viewUpdater = new EditTextViewUpdater(this);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO: should be done in an own thread so that the UI isn't blocked
		if (collabService != null && collabService.isReadyForEditing()){
			Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
			if (keyCode == KeyEvent.KEYCODE_DEL && getSelectionStart() != 0){
				// DELETE
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, getSelectionStart()-1, 1);
			} else if (keyCode == KeyEvent.KEYCODE_SPACE){
				// SPACE
				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, " ", getSelectionStart());
			} else if (event.isPrintingKey()){
				// INSERT
				String output = Character.toString(event.getDisplayLabel());
				if (!event.isShiftPressed()){
					output = output.toLowerCase();
				}
				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, String.valueOf(Character.toChars(event.getUnicodeChar())), getSelectionStart());
			} else {
				// TODO: ?
			}
			Log.i("WMP", "document state: " + collabService.getDocumentString());
		}
		return super.onKeyDown(keyCode, event);
	}
	
//	@Override
//	protected void onTextChanged(CharSequence text, int start, int before,
//			int after) {
//		
//		/* TODO: shouldn't be triggered for remote modifications - could be solved by:
//		 * - implementing onKeyDown() - just called during local actions
//		 * - implementing setText() - just called during remote actions (*but* can also be called by 
//		 *   other methods in our code - so that doesn't seem suitable)
//		 */
//		
//		if (collabService != null && collabService.isReadyForEditing()){
//			Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
//			if (after > 0){
//				// INSERT
//				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, text.subSequence(start, start+after).toString(), start);
//			} else if (before > 0){
//				// DELETE
//				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, start, before);
//			} else {
//				// TODO: ?
//			}
//			Log.i("WMP", "document state: " + collabService.getDocumentString());
//		}
//		super.onTextChanged(text, start, before, after);
//	}
	
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
