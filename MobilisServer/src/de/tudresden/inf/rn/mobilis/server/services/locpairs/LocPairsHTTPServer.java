package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jibble.simplewebserver.SimpleWebServer;

import de.tudresden.inf.rn.mobilis.server.services.MobilisService;

/**
 * The Class LocPairsHTTPServer provides access to the collected network 
 * finger prints that are saved as XML files. LocPairsHTTPServer creates an
 * instance of a mini web server.
 * 
 * @author Reik Mueller
 */
public class LocPairsHTTPServer extends MobilisService{

	/**
	 * Instantiates a new server.
	 *
	 */

	public LocPairsHTTPServer() {
		String dirName = generiereDateipfad("/mobilisLocpairs/fingerprints/");
		File f = new File(dirName);
		if (!f.isDirectory()) {
			f = new File(generiereDateipfad("/mobilisLocpairs/"));
			f.mkdir();
			f = new File(generiereDateipfad("/mobilisLocpairs/highscores/"));
			f.mkdir();
			f = new File(generiereDateipfad("/mobilisLocpairs/fingerprints/"));
			f.mkdir();
			f = new File(generiereDateipfad("/mobilisLocpairs/fingerprints/printsPerGame/"));
			f.mkdir();
			f = new File(generiereDateipfad("/mobilisLocpairs/fingerprints/"));
		}

		String arrayString = "<html><body><head> <title>LocPairs</title><h1>Mobilis LocPairs<h1><br></br><h2>A location based version of the famous game Locpairs</h2><p>via this web-interface you can retrieve the collected network finger <br></br>prints as xml documents.</p><br></br><a href='http://localhost:1231/printsPerGame/AllNetworkFingerprints.xml'>all collected network finger prints in one xml file</a><br></br><a href='http://localhost:1231/printsPerGame/'>collected network finger prints per game</a></body></html>";

		try {
			BufferedWriter out = new BufferedWriter(
					new FileWriter(
							generiereDateipfad("/mobilisLocpairs/fingerprints/index.html")));
			out.write(arrayString);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			new SimpleWebServer(f, 1231);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("LocPairs-HTTP-Server started.");
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

	@Override
	protected void registerPacketListener() {}
}
