/*
 * Created on Aug 15, 2005
 *
 */
package se.su.it.smack.pubsub.elements;



public class PublishElement extends PubSubElement {
	
	@Override
	public boolean isSingleChild() { return true; }
	
	@Override
	public String getName() {
		return "publish";
	}

	public PublishElement() { super(); }
	
	public PublishElement(String node) { super(node); }

	@Override
	public String getBeginXMLAttributes() {
		return "node=\'"+this.getNode()+"\'";
	}
	
}
