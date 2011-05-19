package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;

/**
 * The Class NetworkFingerprintDAO. Stores all incoming network measurements in
 * an XML-file. The File is named after the full Jid (GameId) of the Game
 * instance.
 * 
 * @author Reik Mueller
 */
public class NetworkFingerprintDAO {

	private Element rootElement = new Element("fingerprints");
	private String rootFolder = "/mobilisLocpairs/";
	private String fingerprintFolder = "/mobilisLocpairs/fingerprints/";
	private String allFingerprintsFolder = "/mobilisLocpairs/fingerprints/printsPerGame/";
	private String allNetworkFingerprintsFile = "/mobilisLocpairs/fingerprints/printsPerGame/AllNetworkFingerprints.xml";
	private Document doc = null;
	private boolean measurements = false;

	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd-kk-mm-ss");

	/**
	 * Instantiates a new network fingerprint dao.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public NetworkFingerprintDAO() {

		String dirName = generiereDateipfad(allFingerprintsFolder);
		File f = new File(dirName);
		if (!f.isDirectory()) {
			f = new File(generiereDateipfad(rootFolder));
			f.mkdir();
			f = new File(generiereDateipfad(fingerprintFolder));
			f.mkdir();
			f = new File(generiereDateipfad(allFingerprintsFolder));
			f.mkdir();
		}

	}

	/**
	 * Adds a fingerprint.
	 * 
	 * @param fingerprint
	 *            the fingerprint
	 * @return true, if successful
	 */
	public boolean addFingerprint(NetworkFingerPrint fingerprint) {
		Map<String, Integer> fp = fingerprint.getNetworkFingerPrint();
//		System.out
//				.println("NetworkFingerPrintDAO.addFingerprint() number of measurments: "
//						+ fp.size());
		if (fp.size() > 0)
			measurements = true;
		GeoPosition position = fingerprint.getPosition();
		Element fpNode = new Element("fingerprint");
		fpNode.setAttribute(new Attribute(GeoPosition.LONGITUDE, String
				.valueOf(position.getLongitude())));
		fpNode.setAttribute(new Attribute(GeoPosition.LATITUDE, String
				.valueOf(position.getLatitude())));
		fpNode.setAttribute(new Attribute(GeoPosition.ALTITUDE, String
				.valueOf(position.getAltitude())));
		fpNode.setAttribute(new Attribute("scantime", format.format(Calendar
				.getInstance().getTime())));
		for (String ssid : fp.keySet()) {
			Element part = new Element("part");
			part.setAttribute(new Attribute("ssid", ssid));
			part.addContent(new Text(fp.get(ssid).toString()));
			fpNode.addContent(part);
		}
		rootElement.addContent(fpNode);
		return true;
	}

	/**
	 * Writes the collected fingerprints into the XML-file.
	 * 
	 * @return true, if successful
	 */
	public boolean close() {
		if (measurements) {
			if (!writeGameOnlyNetworkFingerprintFile())
				return false;
			if (!actualizeAllNetworkFingerprintsFile())
				return false;
		}
		return true;
	}

	private String generiereDateipfad(String dateipfadrelativ) {
		String klassenname = this.getClass().getName();
		String klassenpfadrelativ = "/" + klassenname.replace(".", "/")
				+ ".class";
		String klassenpfadabsolut = getClass().getResource(klassenpfadrelativ)
				.getFile();
		String classespfad = klassenpfadabsolut.replace(klassenpfadrelativ, "");
		String buildpfad = classespfad.substring(0, classespfad
				.lastIndexOf("/"));
		String projektpfad = buildpfad.substring(0, buildpfad.lastIndexOf("/"));
		dateipfadrelativ = dateipfadrelativ.replace("\\", "/");
		if (dateipfadrelativ.charAt(0) != '/')
			dateipfadrelativ = "/" + dateipfadrelativ;
		String dateipfadabsolut = projektpfad + dateipfadrelativ;
		dateipfadabsolut = dateipfadabsolut.replace("%20", " ");
		dateipfadabsolut = dateipfadabsolut.substring(1);
		return dateipfadabsolut;
	}

	private boolean actualizeAllNetworkFingerprintsFile() {

		File file = new File(generiereDateipfad(allNetworkFingerprintsFile));

		if (file.exists()) {
			SAXBuilder builder = new SAXBuilder();
			Document doc = null;
			Element oldRootElement = null;
			try {
				doc = builder.build(new File(
						generiereDateipfad(allNetworkFingerprintsFile)));
			} catch (JDOMException e2) {
				e2.printStackTrace();
				return false;
			} catch (IOException e2) {
				e2.printStackTrace();
				return false;
			}

			oldRootElement = doc.getRootElement();
			oldRootElement.addContent(rootElement.cloneContent());
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			OutputStream out;

			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}

			try {
				outputter.output(doc, out);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			if (doc == null)
				doc = new Document(rootElement);
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			OutputStream out;

			try {
				file.createNewFile();
				out = new FileOutputStream(file);
				outputter.output(doc, out);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private boolean writeGameOnlyNetworkFingerprintFile() {
		if (doc == null)
			doc = new Document(rootElement);
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		OutputStream out;

		String fileName = format.format(Calendar.getInstance().getTime());
		File file = new File(generiereDateipfad(allFingerprintsFolder
				+ fileName + ".xml"));

		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		try {
			outputter.output(doc, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
