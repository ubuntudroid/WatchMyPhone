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
 * Copyright 2007 Ansgar Gerlicher.
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 */
package de.hdm.cefx.concurrency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationExecutor;
import de.hdm.cefx.concurrency.operations.OperationIDComparator;
import de.hdm.cefx.concurrency.operations.StateVector;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import de.hdm.cefx.concurrency.operations.UpdateOperations;
import de.hdm.cefx.concurrency.operations.UpdateOperationsProcessor;
import de.hdm.cefx.exceptions.NodeNotFoundException;
import de.hdm.cefx.server.DocumentData;
import de.hdm.cefx.util.DOM3Methods;

/**
 * The methods <code>prepareExecution(Operation o)</code> and
 * <code>checkConflict(Operation o)</code> are implemented by the
 * OrderingConcurrencyControllerImpl (OCCI) class. Additionally the OCCI
 * implements the <code>setOperationExecutor(OperationExecutor oe)</code>
 * method of the ConcurrencyController interface. A third party concurrency
 * control implementer would only need to implement those three methods and hook
 * the new implementation into CEFX by using the extension point mechanism
 *
 * @author Ansgar Gerlicher
 *
 */
public class OrderingConcurrencyControllerImpl extends AbstractConcurrencyControllerImpl {

	private boolean debug = false;

	/*
	 * Begin implemented AbstractConcurrencyControllerImpl methods
	 */

	protected boolean prepareExecution(Operation o) {
		Vector<Operation> ops=new Vector<Operation>();
		ops.add(o);
		return prepareExecution(ops);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.AbstractConcurrencyControllerImpl#prepareExecution(de.hdm.cefx.concurrency.operations.Operation)
	 */
	@SuppressWarnings("unchecked")
	protected synchronized boolean prepareExecution(Vector<Operation> ops) {
		if (ops.size()<1) return true;

		// if the history buffer is empty execute the operation directly
		if ((historyBuffer.isEmpty()) && (ops.size()==1)) {
			Operation o=ops.elementAt(0);
			boolean b=executeAndUpdateState(o);
			if (o.getType()==Operation.UPDATE) {
				UpdateOperationImpl uo=(UpdateOperationImpl)o;
				String text=uo.getTargetText(this);
				if (text!=null) {
					text=UpdateOperationsProcessor.process(null, uo, text);
					uo.setTargetText(this, text);
				}
			}
			return b;
		}

		// Find the right position for the new operation
		int lastPreceding = findLastPreceedingOperaton(ops.elementAt(0));
		int ii;
		for (ii=1; ii<ops.size(); ii++) {
			int i=findLastPreceedingOperaton(ops.elementAt(ii));
			if (i<lastPreceding) {
				lastPreceding=i;
			}
		}

		// copy concurrent operations from HB to this list
		List<Operation> concurrentOpVector = copyHBTail(lastPreceding, null);
		// add the new operation to the vector

		concurrentOpVector.addAll(ops);

		OperationIDComparator cmp = new OperationIDComparator();
		Collections.sort(concurrentOpVector, cmp);

		// Undo the operations in the History Buffer until the last
		// preceeding operation
		undoOperationsInHBTo(lastPreceding);

		Operation lastOp=null;
		for (Operation op : concurrentOpVector) {
			op.setDiscarded(false);
			if (lastOp!=null) {
				if ((lastOp.getType()==Operation.DELETE) && (op.getType()==Operation.DELETE)) {
					if (lastOp.getTargetId().equals(op.getTargetId())) {
						op.setDiscarded(true);
					}
				}
			}
			lastOp=op;
		}

		// now redo the operations from the ordered operations list
		executeOperationSet(concurrentOpVector);

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.ConcurrencyController#setOperationExecutor(de.hdm.cefx.concurrency.operations.OperationExecutor)
	 */
	public void setOperationExecutor(OperationExecutor impl) {
		init(impl);

	}

	/**
	 * Checks if the target node of the operation is in the context of the
	 * current document. That is, if it can be found in the current document.
	 *
	 * @param operation
	 *            the operation to be executed.
	 * @return true if the target node of the operation was found in the
	 *         document.
	 */
	private boolean isExecutableInContext(Operation operation) {
		String targetId = operation.getTargetId();
		try {
			getNodeForId(targetId);
		} catch (NodeNotFoundException e) {
			// target node has been removed in this context
			return false;
		}
		return true;
	}

	/**
	 * Debugs information.
	 *
	 * @param infos
	 *            the debug information.
	 */
	private void debugInfos(Object[] infos) {
		System.out.println();
		System.out.println("******************************************************************\n" + this.context + "local SV:" + this.getStateVector() + "\n" + "Remote Operation: " + infos + "\n"
				+ "historybuffer after  ordering: " + this.historyBuffer + "\n" + "******************************************************************\n");

	}

	class ExecuteOperationData {
		public Vector<UpdateOperations> ops;
		public String text;
		public UpdateOperationImpl op;
	}

	//fügt dem Vector v alle Operationen aus dem HistoryBuffer hinzu, die im Konflikt mit derm Update uo stehen
	private void addConflictHBUpdateOperations(UpdateOperationImpl uo,String uoId,Vector<UpdateOperations> v) {
		if (uo.getDISOperation().getOperation()==UpdateOperations.SET) {
			return;
		}
		for (Operation hbop : historyBuffer) {
			if (hbop.getType()==Operation.UPDATE) {

				if (UpdateOperationsProcessor.isTransformationNeeded(hbop.getStateVector(), uo.getStateVector())) {
					UpdateOperationImpl hbuo=(UpdateOperationImpl)hbop;
					String id=hbuo.getUniqueNodePositionId();
					if (id.equals(uoId)) {
						v.add(hbuo.getDISOperation());
					}
				}

			}
		}
	}

	/**
	 * Executes all operations in the given list.
	 *
	 * @param concurrentOpVector
	 *            the list of operations to be executed. In this case this is
	 *            the COV.
	 */
	private void executeOperationSet(List<Operation> concurrentOpVector) {
		Hashtable<String ,ExecuteOperationData> table=new Hashtable<String ,ExecuteOperationData>();

		//boolean updateOperationsExecuted=false;

System.out.println("@@@@@@@@@@@_START Executing remote operations");
		for (Operation operation : concurrentOpVector) {

System.out.println("@@@@@@@@@@@ executeOperation: "+operation.toString());

			// check if the operation can still be executed in the new context
			// if it cannot be executed anymore, then set it as discarded,
			// execute it an undiscard it again
			if (isExecutableInContext(operation)) {
				executeAndUpdateState(operation);
				if (operation.getType()==Operation.UPDATE) {
					UpdateOperationImpl uo=(UpdateOperationImpl)operation;
					String id=uo.getUniqueNodePositionId();
					ExecuteOperationData data;
					if (table.containsKey(id)) {
						data=table.get(id);
					} else {
						data=new ExecuteOperationData();
						data.op=uo;
						data.text=uo.getTargetText(this);
						if (data.text==null) {
							data.text="";
						}
						data.ops=new Vector<UpdateOperations>();
						addConflictHBUpdateOperations(uo,id,data.ops);
						table.put(id, data);
					}
					data.text=UpdateOperationsProcessor.process(data.ops, uo, data.text);
					uo.setTargetText(this, data.text);
					//updateOperationsExecuted=true;
				}
			} else {
				System.out.println("OrderingConcurrencyControllerImpl.resolveConflict() Operation: " + operation + " is not executable in this context " + concurrentOpVector);
				// if the operation is already discarded, we do not change the
				// flag
				if (!operation.isDiscarded()) {
					operation.setDiscarded(true);
					executeAndUpdateState(operation);
					operation.setDiscarded(false);
				} else {
					executeAndUpdateState(operation);
				}
			}
		}
		System.out.println("@@@@@@@@@@@_END");
		System.out.println("Executed remote operations and added to history buffer! " + 
				this.getName() + " id=" + getClientId() + " localstate: " + getStateVector());
	}

	public void executeLateJoinOperations() {
		//delete duplicate operation
		HashMap<StateVector,Operation> hbs=new HashMap<StateVector,Operation>();

		int ii;
		for (ii=0; ii<historyBuffer.size(); ii++) {
			Operation o=historyBuffer.elementAt(ii);
			hbs.put(o.getStateVector(),o);
		}

		ii=lateJoinBuffer.size()-1;
		while (ii>=0) {
			Operation o=lateJoinBuffer.elementAt(ii);
			if (hbs.containsKey(o.getStateVector())) {
				lateJoinBufferLock.lock();
				lateJoinBuffer.remove(ii);
				lateJoinBufferLock.unlock();
			}
			ii=ii-1;
		}

		//execute operations
		while (lateJoinBuffer.size()>0) {

			lateJoinBufferLock.lock();
			Stack<Operation> tmp=(Stack<Operation>)lateJoinBuffer.clone();
			lateJoinBuffer.clear();
			lateJoinBufferLock.unlock();

			System.out.println("Executing operations from late join buffer ...");
			prepareExecution(tmp);
		}

		cleanHistoryBuffer();
	}

	public Document setDocumentData(DocumentData data) {
		doc=data.getDocument();
		historyBuffer=data.getHistoryBuffer();
		deletedNodesMap=data.getDeletedNodesMap();

		Collection<Node> col=deletedNodesMap.values();
//		for (Node node : col) {
//			DOM3Methods.adoptNode(doc, node);
//		}
		refreshNodeMap();

		executeLateJoinOperations();

		return doc;
	}
}
