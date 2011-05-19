package de.tudresden.inf.rn.mobilis.services.cores;

import org.w3c.dom.Document;

public abstract class DocumentContentBuilder {
	
	protected String fileExtension;
	
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public abstract void createInitialDocumentStructure(Document doc);
	
}
