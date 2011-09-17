/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * @author Michael Voigt
 */

package de.hdm.cefx.client.net;

import jabberSrpc.JabberClient;

import java.util.Collection;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hdm.cefx.concurrency.operations.DeleteOperationImpl;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationFactory;
import de.hdm.cefx.concurrency.operations.StateVector;
import de.hdm.cefx.concurrency.operations.UpdateDeleteOperation;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateOperations;
import de.hdm.cefx.concurrency.operations.UpdateSetOperation;
import de.hdm.cefx.exceptions.NodePositionException;
import de.hdm.cefx.util.CEFXUtil;
import de.hdm.cefx.util.XMLHelper;

public class OperationXMLTransformer {
	
	private static String messageBody = "";

	public static Message transformOperation2Message(Operation o,String to) {
		Message msg=null;

		OperationExtension ex=null;
		if (o.getType()==Operation.UPDATE) {
			UpdateOperationImpl uo=(UpdateOperationImpl)o;
			if (uo.getDISOperation().getOperation()==UpdateOperations.DELETE) {
				ex=createUpdateDeleteMessage(uo);
			}
			if (uo.getDISOperation().getOperation()==UpdateOperations.INSERT) {
				ex=createUpdateInsertMessage(uo);
			}
			if (uo.getDISOperation().getOperation()==UpdateOperations.SET) {
				ex=createUpdateSetMessage(uo);
			}
		}
		if (o.getType()==Operation.INSERT) {
			InsertOperationImpl io = (InsertOperationImpl)o;
			ex = createInsertMessage(io);
			
			// provide inserted element to be serialized into the message body
//			Node insertNode = io.getInsertNode();
//			if (insertNode.getNodeType() == Node.ELEMENT_NODE) {
//				Element element = (Element) insertNode;
//				// serialize the Element
//				messageBody = XMLHelper.getElementString(element, false);
//			}
		}
		if (o.getType()==Operation.DELETE) {
			DeleteOperationImpl od=(DeleteOperationImpl)o;
			ex=createDeleteMessage(od);
		}

		if (ex!=null) {
			msg=new Message();
			msg.setType(Message.Type.groupchat);
			msg.setTo(to);
//			msg.setBody(" ");
			msg.setBody(messageBody);
			msg.setFrom(JabberClient.getInstance().getJID());
			msg.addExtension(ex);
		}
		return msg;
	}

	private static void setValues(OperationExtension ex, UpdateOperations disOp,StateVector sv,int clientID) {
		ex.setParentID(disOp.getNodePosition().getParentNodeId());
		ex.setFixNodeID(disOp.getNodePosition().getFixNodeId());
		ex.setBeforeAfter(disOp.getNodePosition().getRelativeInsertPosition());

		ex.setStateVector(sv);
		ex.setAttrName(disOp.getAttributName());
		if (disOp.getNodeType()==UpdateOperations.TEXT) {
			ex.setType(OperationExtension.TEXT_TYPE);
		} else {
			ex.setType(OperationExtension.ATTR_TYPE);
		}

		ex.setCliendID(clientID);
	}

	private static OperationExtension createUpdateDeleteMessage(UpdateOperationImpl o) {
		UpdateDeleteExtension ex=new UpdateDeleteExtension();
		UpdateDeleteOperation disOp=(UpdateDeleteOperation)o.getDISOperation();

		setValues(ex, disOp, o.getStateVector(), o.getClientId());
		ex.setPos(disOp.getTextPos());
		ex.setLength(disOp.getLength());

		return ex;
	}

	private static OperationExtension createUpdateInsertMessage(UpdateOperationImpl o) {
		UpdateInsertExtension ex=new UpdateInsertExtension();
		UpdateInsertOperation disOp=(UpdateInsertOperation)o.getDISOperation();

		setValues(ex, disOp, o.getStateVector(), o.getClientId());
		ex.setPos(disOp.getTextPos());
		ex.setContent(disOp.getText());

		return ex;
	}

	private static OperationExtension createUpdateSetMessage(UpdateOperationImpl o) {
		UpdateSetExtension ex=new UpdateSetExtension();
		UpdateSetOperation disOp=(UpdateSetOperation)o.getDISOperation();

		setValues(ex, disOp, o.getStateVector(), o.getClientId());
		ex.setContent(disOp.getText());

		return ex;
	}

	private static OperationExtension createInsertMessage(InsertOperationImpl o) {
		InsertExtension ex=new InsertExtension();
		NodePosition np=o.getInsertPosition();

		ex.setParentID(np.getParentNodeId());
		ex.setFixNodeID(np.getFixNodeId());
		ex.setBeforeAfter(np.getRelativeInsertPosition());

		ex.setStateVector(o.getStateVector());
		ex.setCliendID(o.getClientId());
		ex.setElement((Element)o.getInsertNode());

		return ex;
	}

	private static OperationExtension createDeleteMessage(DeleteOperationImpl o) {
		DeleteExtension ex=new DeleteExtension();

		ex.setParentID(o.getTargetId());
		ex.setStateVector(o.getStateVector());
		ex.setCliendID(o.getClientId());
		ex.setDOMLevel(o.getOperationID().getDOMLevel());

		return ex;
	}

	public static Operation transformMessage2Operation(Message msg,CEFXSessionImpl session,int clientID) {
		Collection<PacketExtension> col=msg.getExtensions();
		PacketExtension[] ea=(PacketExtension[]) col.toArray(new PacketExtension[0]);
		Operation o=null;

		int ii;
		for (ii=0; ii<ea.length; ii++) {
			PacketExtension e=ea[ii];
			String type=e.getNamespace();
			String tmp=type;
			if (tmp.length()>=OperationExtension.NAMESPACE.length()) {
				tmp=tmp.substring(0, OperationExtension.NAMESPACE.length());
			}

			if (tmp.equals(OperationExtension.NAMESPACE)) {
				OperationExtension ox=(OperationExtension)e;
				if (ox.getCliendID()==clientID) { //ignore own operations
					return null;
				}
			}
			if (type.equals(OperationExtension.NAMESPACE+OperationExtension.UPDATE_DELETE)) {
				o=createUpdateDeleteOperation((UpdateDeleteExtension)e,session);
				return o;
			}
			if (type.equals(OperationExtension.NAMESPACE+OperationExtension.UPDATE_INSERT)) {
				o=createUpdateInsertOperation((UpdateInsertExtension)e,session);
				return o;
			}
			if (type.equals(OperationExtension.NAMESPACE+OperationExtension.UPDATE_SET)) {
				o=createUpdateSetOperation((UpdateSetExtension)e,session);
				return o;
			}
			if (type.equals(OperationExtension.NAMESPACE+OperationExtension.INSERT)) {
				o=createInsertOperation((InsertExtension)e,session);
//				o = createInsertOperation((InsertExtension) e, msg, session);
				return o;
			}
			if (type.equals(OperationExtension.NAMESPACE+OperationExtension.DELETE)) {
				o=createDeleteOperation((DeleteExtension)e,session);
				return o;
			}
		}

		return null;
	}

	private static Operation createDeleteOperation(DeleteExtension ex,CEFXSessionImpl session) {
		Operation o=null;
	    o=OperationFactory.newDeleteOperation(ex.getParentID(),ex.getStateVector(),session.getClient(ex.getCliendID()),ex.getDOMLevel());
		return o;
	}

	private static Operation createInsertOperation(InsertExtension ex,CEFXSessionImpl session) {
		Operation o=null;

		NodePosition np;
		try {
			np = new NodePosition(ex.getParentID(),ex.getFixNodeID(),ex.getBeforeAfter());
		    o=OperationFactory.newInsertOperation(ex.getElement(), np,ex.getStateVector(),session.getClient(ex.getCliendID()));
		} catch (NodePositionException e) {
			e.printStackTrace();
		}

		return o;
	}

	private static Operation createInsertOperation(InsertExtension ex, Message msg, CEFXSessionImpl session) {
		Operation o = null;
		NodePosition np;
		try {
			np = new NodePosition(ex.getParentID(), ex.getFixNodeID(), ex.getBeforeAfter());
			o = OperationFactory.newInsertOperation(
					XMLHelper.getElement(StringUtils.unescapeNode(ex.getContent())), 
					np, ex.getStateVector(), session.getClient(ex.getCliendID()));
		} catch (NodePositionException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	private static Operation createUpdateDeleteOperation(UpdateDeleteExtension ex,CEFXSessionImpl session) {
		Operation o=null;

		int nodeType=-1;
		if (ex.getType().equals(OperationExtension.TEXT_TYPE)) {
			nodeType=UpdateOperations.TEXT;
		} else {
			nodeType=UpdateOperations.ATTRIBUT;
		}
		NodePosition np;
		try {
			np = new NodePosition(ex.getParentID(),ex.getFixNodeID(),ex.getBeforeAfter());
			UpdateDeleteOperation dis=new UpdateDeleteOperation(ex.getPos(),ex.getLength(),nodeType,np,ex.getAttrName());
		    o=OperationFactory.newUpdateOperation(dis, ex.getStateVector(),session.getClient(ex.getCliendID()));
		} catch (NodePositionException e) {
			e.printStackTrace();
		}

		return o;
	}

	private static Operation createUpdateInsertOperation(UpdateInsertExtension ex,CEFXSessionImpl session) {
		Operation o=null;

		int nodeType=-1;
		if (ex.getType().equals(OperationExtension.TEXT_TYPE)) {
			nodeType=UpdateOperations.TEXT;
		} else {
			nodeType=UpdateOperations.ATTRIBUT;
		}
		NodePosition np;
		try {
			np = new NodePosition(ex.getParentID(),ex.getFixNodeID(),ex.getBeforeAfter());
			UpdateInsertOperation dis=new UpdateInsertOperation(ex.getContent(),ex.getPos(),nodeType,np,ex.getAttrName());
		    o=OperationFactory.newUpdateOperation(dis, ex.getStateVector(),session.getClient(ex.getCliendID()));
		} catch (NodePositionException e) {
			e.printStackTrace();
		}

		return o;
	}

	private static Operation createUpdateSetOperation(UpdateSetExtension ex,CEFXSessionImpl session) {
		Operation o=null;
		int nodeType=-1;
		if (ex.getType().equals(OperationExtension.TEXT_TYPE)) {
			nodeType=UpdateOperations.TEXT;
		} else {
			nodeType=UpdateOperations.ATTRIBUT;
		}
		NodePosition np;
		try {
			np = new NodePosition(ex.getParentID(),ex.getFixNodeID(),ex.getBeforeAfter());
			UpdateSetOperation dis=new UpdateSetOperation(ex.getContent(),nodeType,np,ex.getAttrName());
		    o=OperationFactory.newUpdateOperation(dis, ex.getStateVector(),session.getClient(ex.getCliendID()));
		} catch (NodePositionException e) {
			e.printStackTrace();
		}
		return o;
	}
}
