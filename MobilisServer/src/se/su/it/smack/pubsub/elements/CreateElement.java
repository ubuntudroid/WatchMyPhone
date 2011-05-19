/*
 * Created on Aug 15, 2005
 *
 */
package se.su.it.smack.pubsub.elements;

public class CreateElement extends PubSubElement {

    @Override
	public String getName() {
        return "create";
    }

    public CreateElement(String node) {
        super(node);
    }

    public CreateElement() {
        super();
    }

    @Override
    public String toXML() {
        if (this.getNode() == null) {
            return "<create/>\n";
        } else {
            return "  <create node='" + this.getNode() + "'/>\n";
        }
    }
}
