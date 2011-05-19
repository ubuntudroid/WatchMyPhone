/*
 * Created on Aug 15, 2005
 *
 */
package se.su.it.smack.pubsub.elements;

public class RetractElement extends PubSubElement {

	@Override
	public String getName() {
		return "retract";
	}
	
	public RetractElement(String node) { super(node); }
	public RetractElement() { super(); }

    @Override
	public String toXML() {
        return "  <retract node='"+this.getNode()+"'/>\n";
    }
}
