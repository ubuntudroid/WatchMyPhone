package edu.bonn.cs.wmp;

import java.nio.CharBuffer;

import org.w3c.dom.Element;

import android.text.SpannableStringBuilder;
import de.hdm.cefx.concurrency.operations.NodePosition;
import edu.bonn.cs.wmp.application.WMPApplication;
import edu.bonn.cs.wmp.service.CollabEditingService;

public class EditTextSpannableStringBuilder extends SpannableStringBuilder {
	
	private WMPApplication app;
	
	public EditTextSpannableStringBuilder() {
		app = WMPApplication.getInstance(); 
	}
	
	public synchronized EditTextSpannableStringBuilder insertByCEFX(int where,
			CharSequence tb) {
		return this.insert(where, tb, 0, tb.length());
	}

	public synchronized EditTextSpannableStringBuilder insertByCEFX(int where,
			CharSequence tb, int start, int end) {
		return (EditTextSpannableStringBuilder) super.insert(where, tb, start,
				end);
	}

	public synchronized EditTextSpannableStringBuilder replaceByCEFX(int start,
			int end, CharSequence tb) {
		return this.replaceByCEFX(start, end, tb, 0, tb.length());
	}

	public synchronized EditTextSpannableStringBuilder replaceByCEFX(int start,
			int end, CharSequence tb, int tbstart, int tbend) {
		SpannableStringBuilder editable = super.replace(start, end, tb,
				tbstart, tbend);
		return (EditTextSpannableStringBuilder) editable;
	}

	public synchronized EditTextSpannableStringBuilder appendByCEFX(
			CharSequence text, int start, int end) {
		SpannableStringBuilder editable = super.append(text, start, end);
		return (EditTextSpannableStringBuilder) editable;
	}

	@Override
	public synchronized EditTextSpannableStringBuilder insert(int where,
			CharSequence tb) {
		return this.insert(where, tb, 0, tb.length());
	}

	@Override
	public synchronized EditTextSpannableStringBuilder insert(int where,
			CharSequence tb, int start, int end) {
		CollabEditingService collabService = app.getCollabEditingService();
		if (collabService != null && collabService.isReadyForEditing()) {
			Element el = (Element) collabService.getDOMAdapter().getDocument()
					.getElementsByTagName("edit_text").item(0);
			collabService.insertText(el, null, NodePosition.INSERT_BEFORE, tb
					.subSequence(start, end).toString(), where);
			return this;
		} else {
			return (EditTextSpannableStringBuilder) super.insert(where, tb,
					start, end);
		}
	}

	@Override
	public synchronized EditTextSpannableStringBuilder replace(int start,
			int end, CharSequence tb) {
		return this.replace(start, end, tb, 0, tb.length());
	}

	@Override
	public synchronized EditTextSpannableStringBuilder replace(int start,
			int end, CharSequence tb, int tbstart, int tbend) {
		CollabEditingService collabService = app.getCollabEditingService();
		if (collabService != null && collabService.isReadyForEditing()) {
			Element el = (Element) collabService.getDOMAdapter().getDocument()
					.getElementsByTagName("edit_text").item(0);
			collabService
					.replaceText(el, null, NodePosition.INSERT_BEFORE, tb
							.subSequence(tbstart, tbend).toString(), start, end
							- start);
			return this;
		} else {
			SpannableStringBuilder editable = super.replace(start, end, tb,
					tbstart, tbend);
			return (EditTextSpannableStringBuilder) editable;
		}
	}

	@Override
	public synchronized EditTextSpannableStringBuilder append(char text) {
		char[] ar = { text };
		return this.append(CharBuffer.wrap(ar), 0, 1);
	}

	@Override
	public synchronized EditTextSpannableStringBuilder append(CharSequence text) {
		return this.append(text, 0, text.length());
	}

	@Override
	public synchronized EditTextSpannableStringBuilder append(
			CharSequence text, int start, int end) {
		CollabEditingService collabService = app.getCollabEditingService();
		if (collabService != null && collabService.isReadyForEditing()) {
			Element el = (Element) collabService.getDOMAdapter().getDocument()
					.getElementsByTagName("edit_text").item(0);
			collabService.insertText(el, null, NodePosition.INSERT_BEFORE, text
					.subSequence(start, end).toString(), this.length());
			return this;
		} else {
			SpannableStringBuilder editable = super.append(text, start, end);
			return (EditTextSpannableStringBuilder) editable;
		}
	}

	@Override
	public synchronized EditTextSpannableStringBuilder delete(int start, int end) {
		CollabEditingService collabService = app.getCollabEditingService();
		if (collabService != null && collabService.isReadyForEditing()) {
			Element el = (Element) collabService.getDOMAdapter().getDocument()
					.getElementsByTagName("edit_text").item(0);
			collabService.deleteText(el, null, NodePosition.INSERT_BEFORE,
					start, end - start);
			return this;
		} else {
			SpannableStringBuilder editable = super.delete(start, end);
			return (EditTextSpannableStringBuilder) editable;
		}
	}

	@Override
	public synchronized void setSpan(Object what, int start, int end, int flags) {
		super.setSpan(what, start, end, flags);
	}
}
