/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * Copyright 2007 Ansgar Gerlicher.
 * @author Ansgar Gerlicher
 */
package de.tudresden.inf.rn.mobilis.server.services.collabed;

import de.tudresden.inf.rn.mobilis.server.services.CollabEditingService;

/**
 * A thread that regularly saves the current document to the servers repository.
 *
 * @author Ansgar Gerlicher
 *
 */
public class DocumentSaver implements Runnable {

	private boolean block = false;
	private boolean isRunning = true;
	private CollabEditingService collabEditingService;

	public DocumentSaver(CollabEditingService collabEditingService) {
		this.collabEditingService = collabEditingService;
	}

	public void run() {
/*		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();

			while (isRunning) {
				if (!block) {
					Thread.yield();
					try {

						Thread.sleep(30000);
						Document doc = server.getDocument();
						if (doc != null) {
							synchronized (doc) {
								System.out.println("DocumentSaver.run() " + doc);

///ACHTUNG: muss geändert werden - nur Behandlung eines Dokumentes möglich

								URI uri = server.getCurrentDocumentLocation();

								if (uri != null) {
									DOMSource source = new DOMSource(doc);
									StreamResult sr = new StreamResult(new File(uri));
									trans.transform(source, sr);
								}
								System.out.println("DocumentSaver stored document at " + uri);
							}
						}
						Thread.yield();

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (TransformerException e) {
						e.printStackTrace();
					}
					Thread.yield();
				}
			}
			System.out.println("DocumentSaver.run() stopping");
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();
		}*/
	}

	/**
	 * This method is used to stop the SaverThread by setting isRunning to
	 * false.
	 *
	 * @param isRunning
	 *            set to false in order to stop the server.
	 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
