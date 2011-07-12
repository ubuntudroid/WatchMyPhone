package edu.bonn.cs.wmp.views;

import org.w3c.dom.Element;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import de.hdm.cefx.concurrency.operations.NodePosition;
import edu.bonn.cs.wmp.EditTextSpannableStringBuilder;
import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.service.SessionService;
import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;

public class WMPEditText extends EditText {
	
	private CollabEditingService collabService;
	
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
		init();
	}

	public WMPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WMPEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void init() {
		collabService = SessionService.getInstance().getCollabEditingService();
		
		registerViewUpdater();
	}
	
	private void registerViewUpdater(){
		new EditTextViewUpdater(this);
	}
	
	@Override
	public boolean onCheckIsTextEditor() {
		return true;
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
	
	// TODO: this code is interesting for the hardware keyboard stuff
//	@Override
//	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//		if (collabService != null && collabService.isReadyForEditing()) {
//			Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
//			if (keyCode == KeyEvent.KEYCODE_DEL && getSelectionStart() != 0){
//				// DELETE
//				deleteSelectedText(el);
//			} else if (keyCode == KeyEvent.KEYCODE_SPACE){
//				// SPACE
//				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, " ", getSelectionStart());
//			} else if (event.isPrintingKey()){
//				// INSERT
//				if (getSelectionStart() != getSelectionEnd()){
//					deleteSelectedText(el);
//				}
//				
//				int insertPos;
//				if (getSelectionStart() > getSelectionEnd()){
//					insertPos = getSelectionEnd();
//				} else {
//					insertPos = getSelectionStart();
//				}
//				
//				String output = Character.toString(event.getDisplayLabel());
//				if (!event.isShiftPressed()){
//					output = output.toLowerCase();
//				}
//				collabService.insertText(el, null, NodePosition.INSERT_BEFORE, String.valueOf(Character.toChars(event.getUnicodeChar())), insertPos);
//			} else {
//				return false;
//			}
//			return true;
//		} else {
//			return super.onKeyPreIme(keyCode, event);
//		}
//	}
//	
//	private void deleteSelectedText(Element el) {
//		int deletePos;
//		int length;
//		if (getSelectionStart() < getSelectionEnd()) {
//			deletePos = getSelectionStart();
//			length = getSelectionEnd() - getSelectionStart();
//		} else if (getSelectionStart() > getSelectionEnd()){
//			deletePos = getSelectionEnd();
//			length = getSelectionStart() - getSelectionEnd();
//		} else {
//			deletePos = getSelectionStart() - 1;
//			length = 1;
//		}
//		collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, deletePos, length);
//	}
	
	@Override
	public EditTextInputConnectionWrapper onCreateInputConnection(EditorInfo outAttrs) {
		InputConnection con = super.onCreateInputConnection(outAttrs);
		EditTextInputConnectionWrapper inputConnectionWrapper = new EditTextInputConnectionWrapper(con, true);
		return inputConnectionWrapper;
	}
	
	/**
	 * Overwrite this class (and only this!) if you want to supply your own InputConnection to a {@link WMPEditText} view.
	 * @author Sven Bendel
	 *
	 */
	public class EditTextInputConnectionWrapper extends InputConnectionWrapper {
		
		InputConnection target;
		
		public EditTextInputConnectionWrapper(InputConnection target, boolean mutable) {
			super(target, mutable);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean setComposingText(CharSequence text, int newCursorPosition) {
			if (collabService != null && collabService.isReadyForEditing()) {
				int start = BaseInputConnection.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart(); 
				}
				if (end == -1) {
					end = getSelectionEnd();
				}
				clearComposingText();
				Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
				collabService.replaceText(el, null, NodePosition.INSERT_BEFORE, text.toString(), start, end-start);
				int selectionStart, selectionEnd;
				selectionStart = start + text.length();
				selectionEnd = start + text.length();
				setSelection(selectionStart, selectionEnd);
				finishComposingText();
				return true;
			} else {
				return super.setComposingText(text, newCursorPosition);
			}
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			if (collabService != null && collabService.isReadyForEditing()) {
				int start = BaseInputConnection.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart(); 
				}
				if (end == -1) {
					end = getSelectionEnd();
				}
				Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
				collabService.replaceText(el, null, NodePosition.INSERT_BEFORE, text.toString(), start, end-start);
				int selectionStart, selectionEnd;
				selectionStart = start + text.length();
				selectionEnd = start + text.length();
				setSelection(selectionStart, selectionEnd);
				return true;
			} else {
				return super.commitText(text, newCursorPosition);
			}
		}
//		
//		@Override
//		public boolean commitCompletion(CompletionInfo text) {
//			// TODO Auto-generated method stub
//			return super.commitCompletion(text);
//		}
//		
		@Override
		public boolean deleteSurroundingText(int leftLength, int rightLength) {
			if (collabService != null && collabService.isReadyForEditing()) {
				// TODO: do in Background
				Element el = (Element) collabService.getDOMAdapter()
						.getDocument().getElementsByTagName("edit_text")
						.item(0);
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE,
						getSelectionStart() - leftLength, leftLength
								+ rightLength);
			}
			return true;
		}
		
		@Override
		public boolean sendKeyEvent(KeyEvent event) {
			Log.v("WMPEditText", "sendKeyEvent(), keyCode=" + event.getKeyCode());
			if (collabService != null && collabService.isReadyForEditing()) {
				Element el = (Element) collabService.getDOMAdapter().getDocument().getElementsByTagName("edit_text").item(0);
				int keyCode = event.getKeyCode();
				
				int start = BaseInputConnection.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart(); 
				}
				if (end == -1) {
					end = getSelectionEnd();
				}
				
				if (keyCode == KeyEvent.KEYCODE_DEL){
					// DELETE
					/*
					 *  TODO: check if really necessary - seems, that this is already handled in the other methods.
					 *  Maybe necessary for hard keyboard?
					 */
					deleteText(el, start, end);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
					// ENTER
					// TODO: handle enter key
					return true;
				}
				else return false;
			} else {
				return super.sendKeyEvent(event);
			}
		}
		
		private void deleteText(Element el, int start, int end) {
			int deletePos;
			int length;
			if (start < end) {
				deletePos = start;
				length = end - start;
			} else if (start > end){
				deletePos = end;
				length = start - end;
			} else {
				if (start == 0) return;
				deletePos = start - 1;
				length = 1;
			}
			
			collabService.deleteText(el, null, NodePosition.INSERT_BEFORE, deletePos, length);
		}
		
	}
	
}
