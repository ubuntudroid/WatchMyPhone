package edu.bonn.cs.wmp.service;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.w3c.dom.Document;
import de.hdm.cefx.dom.adapter.CEFXDOMAdapterImpl;
import de.hdm.cefx.exceptions.NodeNotFoundException;
import de.hdm.cefx.server.ServerObject;
import de.hdm.cefx.util.CEFXUtil;
import de.hdm.cefx.util.XMLHelper;
import de.hdm.cefx.CEFXControllerImpl;
import de.hdm.cefx.awareness.AwarenessControllerImpl;
import edu.bonn.cs.wmp.DocumentFactory;
import edu.bonn.cs.wmp.MainActivity;
import edu.bonn.cs.wmp.Monitoring;
import de.hdm.cefx.client.net.NetworkController;
import de.hdm.cefx.client.net.NetworkControllerImpl;
import de.hdm.cefx.concurrency.AbstractConcurrencyControllerImpl;
import de.hdm.cefx.concurrency.OrderingConcurrencyControllerImpl;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.mapdraw.MonitoringBean;
import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;

import jabberSrpc.JabberClient;

import java.io.InputStream;
import java.util.Vector;

public class CollabEditingService implements MobilisAndroidService {
	private static final String TAG = "CollabEditingService";

	private boolean DEBUG = false;
	private Document document;

	public Document getDocument() {
		return this.document;
	}

	private CEFXDOMAdapterImpl da;
	private CEFXControllerImpl cefx;
	private AwarenessControllerImpl ac;
	private DocumentFactory docFac;
	private OrderingConcurrencyControllerImpl cc;
	private boolean readyForEditing;
	private Messenger mConnectMessenger;
	private MXAListener mMXAListener;
	private MXAController mxaController;
	private IXMPPService xmppService;
	private Context ctx;
	
	boolean connected;

	public boolean isReadyForEditing() {
		return this.readyForEditing;
	}


	
	IXMPPIQCallback monitoringIQCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			Log.i(TAG, "monitoringIQCallback --> processBean: " + b.toString());
			if (b instanceof MonitoringBean) {
				MonitoringBean bb = (MonitoringBean) b;
				if (b.getType() == XMPPBean.TYPE_GET) {
					Log.i(TAG, "MonitoringBean type=GET arrived.");

					String statusMsg = bb.getStatusMsg();
					if (statusMsg.equals(MonitoringBean.PING)) {
						MonitoringBean beanAnswer = new MonitoringBean();
						beanAnswer.setType(XMPPBean.TYPE_RESULT);
						beanAnswer.setFrom(bb.getTo());
						beanAnswer.setTo(bb.getFrom());
						xmppService.sendIQ(
								new Messenger(ackHandler),
								null,
								1,
								Parceller.getInstance().convertXMPPBeanToIQ(
										beanAnswer, true));
					} else if (statusMsg.equals(MonitoringIQ.START_TIMER)) {
						// System.out.println("Start Remote IQ");
						Monitoring.get().startTimer();
						if (Monitoring.get().valueCounter == 0) {
							System.out.println("Init File");
							Monitoring.get().init("feedthrough.txt", null);
						}
					}
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					Log.i(TAG, "MonitoringBean type=RESULT arrived.");
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					Log.e(TAG, "MonitoringBean type=ERROR arrived. IQ-Payload:"
							+ iq.payload);
				}
			}
		}
	};
	
	private Handler ackHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String toast = "ack: ";

			switch (msg.what) {
			case ConstMXA.MSG_SEND_MESSAGE:
				toast += "message ";
				break;
			case ConstMXA.MSG_SEND_IQ:
				toast += "iq ";
				break;
			case ConstMXA.MSG_SEND_PRESENCE:
				toast += "presence ";
				break;
			case ConstMXA.MSG_SEND_FILE:
				toast += "file ";
				break;
			}
			switch (msg.arg1) {
			case ConstMXA.MSG_STATUS_DELIVERED:
				toast += "delivered";
				break;
			case ConstMXA.MSG_STATUS_ERROR:
				toast += "error";
				break;
			case ConstMXA.MSG_STATUS_REQUEST:
				toast += "request";
				break;
			case ConstMXA.MSG_STATUS_SUCCESS:
				toast += "success";
				break;
			case ConstMXA.MSG_STATUS_IQ_ERROR:
				toast += "iq_error";
				break;
			case ConstMXA.MSG_STATUS_IQ_RESULT:
				toast += "iq_result";
				break;
			}
			Log.i(TAG, "Ack received. " + toast);
			// makeToast(toast);

		}
	};
	
	private Handler xmppResultHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (DEBUG)
				Log.i(TAG, "XMPP connected: " + msg);
		};

	};

	public CollabEditingService(Context ctx) {
		xmppService = null;
		mxaController = MXAController.get();
		readyForEditing = false;
		this.ctx = ctx;
		da = new CEFXDOMAdapterImpl();
		docFac = new DocumentFactory();
		da.setDocumentFactory(docFac);
		document = null;
		// xmlDoc=da.createDocument(uri);
		cefx = (CEFXControllerImpl) da.getCEFXController();
		// cefx.getClient().setID(id);
		ac = new AwarenessControllerImpl();
		cefx.setAwarenessController(ac);
		NetworkControllerImpl nc = new NetworkControllerImpl();
		cefx.setNetworkController(nc);
		cc = new OrderingConcurrencyControllerImpl();
		cefx.setConcurrencyController(cc);
		registerBeanPrototypes();
		connectToServiceAndServer();
	}
	
	protected void connectToServiceAndServer() {
		mConnectMessenger = new Messenger(xmppResultHandler);
		mMXAListener = new MXAListener() {

			@Override
			public void onMXADisconnected() {
				Log.i(TAG, "Disconnected from MXA Remote Service");
				connected = false;
			}

			@Override
			public void onMXAConnected() {
				Log.i(TAG, "Connected to MXA Remote Service");
				xmppService = mxaController.getXMPPService();
//				try {
//					xmppService.connect(mConnectMessenger);
					Log.i(TAG,
							"MXA Remote service successfully connected to XMPP Server");
					connectToXMPPViaCEFX();
//				} catch (RemoteException e) {
//					Log.e(TAG,
//							"MXA Remote service couldn't connect to XMPP Server");
//				}
			}
		};
		mxaController.connectMXA(ctx, mMXAListener);
	}

	protected void registerBeanPrototypes() {
		// TODO: add additionally needed packet types here (Chat, Buddy-List etc.)
		MonitoringBean beanPrototype1 = new MonitoringBean();
		Parceller.getInstance().registerXMPPBean(beanPrototype1);
	}
	
	public boolean isConnected() {
		return connected;
	}

	public boolean connectToXMPPViaCEFX() {
		// read bundled connection params
		// Bundle connectionParams =
		// XMPPController.getInstance().readXMPPPreferences();
		Bundle connectionParams = null;
		try {
			connectionParams = xmppService.getXMPPConnectionParameters();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (connectionParams == null) {
			return false;
		}

		String host = connectionParams.getString("xmpp_host");
		int port = connectionParams.getInt("xmpp_port", 5222);
		String serviceName = connectionParams.getString("xmpp_service");
		String username = connectionParams.getString("xmpp_user");
		String password = connectionParams.getString("xmpp_password");
		String resource = connectionParams.getString("xmpp_resource");

		cefx.setJabberHost(host);
		cefx.setJabberPort(port);
		cefx.setJabberServiceName(serviceName);
		cefx.setJabberUserName(username);
		cefx.setJabberPassword(password);
		cefx.setJabberResource(resource);
		cefx.setJabberResourceSuffix("");

		// Log.i(TAG, "Connecting to XMPP with: " + username + "@" + host + ":"
		// + port + "/" + resource + " service: " + serviceName);

		cefx.init();
		cefx.jabberConnect();

		NetworkController nc = cefx.getNetworkController();
		if (nc != null) {
			cefx.getConcurrencyController().setSession(nc.getSession());
		}

		XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
		xmpp.addPacketListener(Monitoring.get().getMonitoringIQHandler(),
				new AndFilter(new IQTypeFilter(IQ.Type.GET),
						new PacketTypeFilter(MonitoringIQ.class)));

		String server = "mobilis@sven-ubuntu-big/CollabEditing";
		Log.i(TAG, "Establishing connection to CEFX-Server: " + server);
		cefx.connect(server);
		connected = true;
		return true;
	}

	public void insertAttributeText(String text, int pos) {
		// da.Element_AttributeInsert(...);
	}

	public void deleteAttributeText(int pos, int length) {
		// da.Element_AttributeDelete(...);
	}

	public void insertAttribute(String text) {
		// da.Element_AttributeSet(...);
	}

	public void insertText(String parentNodeID, String fixNodeID, int before,
			String text, int textPos) {
		Element parentNode = getElementForId(parentNodeID);
		Element fixNode = getElementForId(fixNodeID);
		da.Element_TextInsert(parentNode, fixNode, before, text, textPos);
	}

	public void deleteText(String parentNodeID, String fixNodeID, int before,
			int pos, int len) {
		if (len != 0) {
			Element parentNode = getElementForId(parentNodeID);
			Element fixNode = getElementForId(fixNodeID);
			da.Element_TextDelete(parentNode, fixNode, before, pos, len);
		}
	}

	public void setText(String parentNodeID, String fixNodeID, int before,
			String text) {
		Element parentNode = getElementForId(parentNodeID);
		Element fixNode = getElementForId(fixNodeID);
		da.Element_TextSet(parentNode, fixNode, before, text);
	}

	public void deleteNode(String id) {
		Element f;
		try {
			((AbstractConcurrencyControllerImpl) cefx
					.getConcurrencyController()).refreshNodeMap();
			f = (Element) (((AbstractConcurrencyControllerImpl) cefx
					.getConcurrencyController()).getNodeForId(id));
			da.Node_removeChild(f);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean uploadDocument(String filePath) {
		return cefx.uploadDocument(filePath);
	}

	public CEFXDOMAdapterImpl getDOMAdapter() {
		return da;
	}

	public CEFXControllerImpl getCEFXController() {
		return cefx;
	}

	public AwarenessControllerImpl getAwarenessController() {
		return ac;
	}

	/**
	 * Returns the DOM Element matching the given CEFX-UID or null if no such
	 * Element could be found. * @param id the CEFX-UID for the Element to
	 * retrieve * @return Element
	 */
	public Element getElementForId(String id) {
		try {
			return (Element) cc.getNodeForId(id);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Shows the current XML document in the console, just for debugging.
	 */
	public void showDocument() {
		System.out.println("Current document:");
		XMLHelper.showDocumentContent(da.getDocument());
	}

	public void showStateVector() {
		Log.i(TAG, "Client " + cefx.getClient().getID() + " - State Vector: "
				+ cefx.getConcurrencyController().getStateVector().toString());
	}

	public String getDocumentString() {
		return XMLHelper.getDocumentString(da.getDocument(), true);
	}

	public boolean loadDocumentFromServer(String uri) {
		Vector<ServerObject> files = cefx.listFiles();
		if (files != null) {
			int docID = cefx.getDocumentID(uri, files);
			cefx.openDocument(docID);
			document = da.getDocument();
			return true;
		} else {
			Log.e(TAG, uri + " could not be found on the server");
			return false;
		}
	}

	public boolean joinSession(String sessionName) {
		if (cefx.joinSession(sessionName).booleanValue()) {
			document = da.getDocument();
			readyForEditing = true;
			Log.i(TAG, "Successfully joined collaboration session "
					+ sessionName);
			return true;
		} else {
			Log.e(TAG, "Could not join session: " + sessionName);
			return false;
		}
	}

	public Element createElement(String name) {
		return document.createElement(name);
	}

	public Element createElementNS(String namespaceURI, String name) {
		return document.createElementNS(namespaceURI, name);
	}

	public Text createTextNode(String textContent) {
		return document.createTextNode(textContent);
	}

	@Override
	public void initIntentReceiver() {
	}

	@Override
	public void unregisterIntentReceiver() {
	}

	/**
	 * Shows a short Toast message on the map
	 */
	public void makeToast(String text) {
		Toast.makeText(MainActivity.getInstance(), text, Toast.LENGTH_SHORT)
				.show();
	}

	public IXMPPService getXMPPService() {
		return xmppService;
	}

	public void insertText(Element parent, Element fixNode, int before,
			String text, int textPos) {
		da.Element_TextInsert(parent, fixNode, before, text, textPos);
	}

	public void deleteText(Element parent, Element fixNode, int before,
			int pos, int len) {
		if (len != 0) {
			da.Element_TextDelete(parent, fixNode, before, pos, len);
		}
	}

	public void setText(Element parent, Element fixNode, int before, String text) {
		da.Element_TextSet(parent, fixNode, before, text);
	}

	public Node insertNode(Node parentNode, Node newNode, Node fixNode,
			int before) {
		return da.Node_insert(parentNode, newNode, fixNode, before);
	}

	public Node insertNode(String parentNodeID, Node newNode, String fixNodeID,
			int before) {
		Element parentNode = getElementForId(parentNodeID);

		// will throw warning if fixNodeID is null, but that's okay anyway
		Element fixNode = getElementForId(fixNodeID);
		// TODO necessary to check if the new node is an attribute / ...?
		return da.Node_insert(parentNode, newNode, fixNode, before);
	}

	public boolean uploadDocument(String fileName, InputStream inputStream) {
		return cefx.uploadDocument(fileName, inputStream);
	}

	public String getNodeId(Node node) {
		return CEFXUtil.getNodeId(node);
	}

	public void replaceText(Element parent, Element fixNode, int before,
			String text, int pos, int len) {
		this.deleteText(parent, fixNode, NodePosition.INSERT_BEFORE, pos, len);
		this.insertText(parent, fixNode, NodePosition.INSERT_BEFORE, text, pos);
	}

	public void replaceText(String parentNodeID, String fixNodeID, int before,
			String text, int pos, int len) {
		Element parent = getElementForId(parentNodeID);
		Element fixNode = getElementForId(fixNodeID);
		this.deleteText(parent, fixNode, NodePosition.INSERT_BEFORE, pos, len);
		this.insertText(parent, fixNode, NodePosition.INSERT_BEFORE, text, pos);
	}

	public boolean leaveSession(String sessionName) {
		if (cefx.leaveSession(sessionName).booleanValue()) {
			document = null;
			readyForEditing = false;
			return true;
		} else {
			Log.e(TAG, "Could not disconnect from session: " + sessionName);
			return false;
		}
	}

}
