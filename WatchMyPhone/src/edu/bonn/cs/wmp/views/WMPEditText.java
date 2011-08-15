package edu.bonn.cs.wmp.views;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import de.hdm.cefx.concurrency.operations.NodePosition;
import edu.bonn.cs.wmp.application.WMPApplication;
import edu.bonn.cs.wmp.awarenesswidgets.LineChange;
import edu.bonn.cs.wmp.awarenesswidgets.ContentChange;
import edu.bonn.cs.wmp.awarenesswidgets.ViewportChange;
import edu.bonn.cs.wmp.awarenesswidgets.WMPAwarenessWidget;
import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.viewupdater.EditTextViewUpdater;

public class WMPEditText extends EditText implements WMPView {
	private CollabEditingService collabService;
	private List<WMPAwarenessWidget> awarenessWidgets = new ArrayList<WMPAwarenessWidget>();

	private WMPApplication app;
	
	private String nodeID = "edit_text";
	
	/**
	 * Overwrite this class (and only this!) if you want to supply your own
	 * InputConnection to a {@link WMPEditText} view.
	 * 
	 * @author Sven Bendel
	 */
	
	/**
	 * TODO: atm node name is hardcoded, obtain ID from WMPComponentRegistry on
	 * view creation * using the following code. The problem with this is, that
	 * this node will be created by all participants * which probably leads to *
	 * 1) a XML with more than one node having the same name (not sure if this
	 * is possible or if CEFX merges the nodes then) * 2) a XML with several
	 * nodes having different names but should be the same * Therefore we need
	 * to obtain the node name either from the server or from the ressource id.
	 */
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
		app = WMPApplication.getInstance();
		collabService = app.getCollabEditingService();

		registerViewUpdater();
		
		Log.i("WMPEditText", "WMPEditText R.id. is " + this.getId());
	}
	
	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public class EditTextInputConnectionWrapper extends InputConnectionWrapper {
		InputConnection target;

		@Override
		public boolean setComposingText(CharSequence text, int newCursorPosition) {
			if (collabService != null && collabService.isReadyForEditing()) {
				int start = BaseInputConnection
						.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart();
				}
				if (end == -1) {
					end = getSelectionEnd();
				}
				clearComposingText();
				Element el = (Element) collabService.getDOMAdapter()
						.getDocument().getElementsByTagName(nodeID)
						.item(0);
				collabService.replaceText(el, null, NodePosition.INSERT_BEFORE,
						text.toString(), start, end - start);
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
				int start = BaseInputConnection
						.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart();
				}
				if (end == -1) {
					end = getSelectionEnd();
				}
				Element el = (Element) collabService.getDOMAdapter()
						.getDocument().getElementsByTagName(nodeID)
						.item(0);
				collabService.replaceText(el, null, NodePosition.INSERT_BEFORE,
						text.toString(), start, end - start);
				int selectionStart, selectionEnd;
				selectionStart = start + text.length();
				selectionEnd = start + text.length();
				setSelection(selectionStart, selectionEnd);
				return true;
			} else {
				return super.commitText(text, newCursorPosition);
			}
		}

		@Override
		public boolean deleteSurroundingText(int leftLength, int rightLength) {
			if (collabService != null && collabService.isReadyForEditing()) {
				// TODO: do in Background
				Element el = (Element) collabService.getDOMAdapter()
						.getDocument().getElementsByTagName(nodeID)
						.item(0);
				collabService.deleteText(el, null, NodePosition.INSERT_BEFORE,
						getSelectionStart() - leftLength, leftLength
								+ rightLength);
				return true;
			} else {
				return super.deleteSurroundingText(leftLength, rightLength);
			}
		}

		private void deleteText(Element el, int start, int end) {
			int deletePos;
			int length;
			if (start < end) {
				deletePos = start;
				length = end - start;
			} else if (start > end) {
				deletePos = end;
				length = start - end;
			} else {
				if (start == 0)
					return;
				deletePos = start - 1;
				length = 1;
			}

			collabService.deleteText(el, null, NodePosition.INSERT_BEFORE,
					deletePos, length);
		}

		public EditTextInputConnectionWrapper(InputConnection target,
				boolean mutable) {
			super(target, mutable);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
//			Log.v("WMPEditText",
//					"sendKeyEvent(), keyCode=" + event.getKeyCode());
			if (collabService != null && collabService.isReadyForEditing()) {
				Element el = (Element) collabService.getDOMAdapter()
						.getDocument().getElementsByTagName(nodeID)
						.item(0);
				int keyCode = event.getKeyCode();

				int start = BaseInputConnection
						.getComposingSpanStart(getText());
				int end = BaseInputConnection.getComposingSpanEnd(getText());
				if (start == -1) {
					start = getSelectionStart();
				}
				if (end == -1) {
					end = getSelectionEnd();
				}

				if (keyCode == KeyEvent.KEYCODE_DEL) {
					// DELETE
					/*
					 * TODO: check if really necessary - seems, that this is
					 * already handled in the other methods. Maybe necessary for
					 * hard keyboard?
					 */
					deleteText(el, start, end);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
					// ENTER
					// TODO: handle enter key
					return true;
				} else
					return false;
			} else {
				return super.sendKeyEvent(event);
			}
		}
	}

	private void registerViewUpdater() {
		new EditTextViewUpdater(this);
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return true;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		// report viewport change to awareness widgets
		ViewportChange change = new ViewportChange();
		change.top = this.getLayout().getLineForVertical(t);
		Rect rect = new Rect();
		this.getDrawingRect(rect);
		change.bottom = this.getLayout().getLineForVertical(
				t + rect.height());
		notifyExternalWMPWidgetsOfContentChange(change);
		
		// remove all changed text formatting the user has already seen
		if (t > oldt) {
			// scroll down
			int topVisibleLine = this.getLayout().getLineForVertical(t) - 1;
			int oldTopVisibleLine = this.getLayout().getLineForVertical(oldt);
			BackgroundColorSpan[] highlights = this.getText().getSpans(
					this.getLayout().getLineStart(oldTopVisibleLine),
					this.getLayout().getLineEnd(topVisibleLine),
					BackgroundColorSpan.class);
			for (BackgroundColorSpan highlight : highlights) {
				this.getText().removeSpan(highlight);
			}
		} else if (t < oldt) {
			// scroll up
			Rect r = new Rect();
			this.getDrawingRect(r);
			int bottomVisibleLine = this.getLayout().getLineForVertical(
					t + r.height());
			int oldBottomVisibleLine = this.getLayout().getLineForVertical(
					oldt + r.height());
			BackgroundColorSpan[] highlights = this.getText().getSpans(
					this.getLayout().getLineStart(bottomVisibleLine),
					this.getLayout().getLineEnd(oldBottomVisibleLine),
					BackgroundColorSpan.class);
			for (BackgroundColorSpan highlight : highlights) {
				this.getText().removeSpan(highlight);
			}
		}
	}

	@Override
	public EditTextInputConnectionWrapper onCreateInputConnection(
			EditorInfo outAttrs) {
		InputConnection con = super.onCreateInputConnection(outAttrs);
		EditTextInputConnectionWrapper inputConnectionWrapper = new EditTextInputConnectionWrapper(
				con, true);
		return inputConnectionWrapper;
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before,
			int after) {
		super.onTextChanged(text, start, before, after);
		LineChange c = new LineChange();
		int count = this.getLineCount();
		c.lineLengths = new float[count];
		for (int i = 0; i < count; i++) {
			float width = this.getLayout().getLineWidth(i);
			c.lineLengths[i] = width;
		}
		notifyExternalWMPWidgetsOfContentChange(c);
	}
	
	@Override
	public void addWidget(WMPAwarenessWidget w) {
		awarenessWidgets.add(w);
	}

	@Override
	public boolean removeWidget(WMPAwarenessWidget w) {
		return awarenessWidgets.remove(w);
	}

	@Override
	public void notifyExternalWMPWidgetsOfContentChange(ContentChange c) {
		/* 
		 * It is important to test for null here, as this method is triggered by onTextChanged
		 * which may be fired before awarenessWidgets is initialized!
		 */
		if (awarenessWidgets != null) {
			for (WMPAwarenessWidget w : awarenessWidgets) {
				if (w.isInterestedIn(c.getClass())) {
					w.onViewContentChange(c);
				}
			}
		}
	}
}
