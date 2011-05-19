package de.tudresden.inf.rn.mobilis.xmpp.pubsub.elements;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;

import se.su.it.smack.pubsub.elements.PubSubElement;

/**
 *
 * @author Istvan
 */
public class ConfigureElement extends PubSubElement {

    private DataForm mForm;

    @Override
    public String getName() {
        return "configure";
    }

    /**
     * Creates a new ConfigureElement to be used in create node stanzas.
     * @param nodeType The node type of the newly created node, whether "collection" or "leaf".
     */
    public ConfigureElement(String nodeType) {
        Form f = new Form(Form.TYPE_SUBMIT);
        FormField ffFormType = new FormField("FORM_TYPE");
        ffFormType.addValue("http://jabber.org/protocol/pubsub#node_config");
        ffFormType.setType("hidden");
        f.addField(ffFormType);
        FormField ffNodeType = new FormField("pubsub#node_type");
        ffNodeType.addValue(nodeType);
        f.addField(ffNodeType);
        mForm = f.getDataFormToSend();
    }
    
    /**
     * Sets the parent node of the new node. Should be called if nodeType was leaf.
     * @param parentNode
     */
    public void setParentNode(String parentNode) {
        FormField ffCollection = new FormField("pubsub#collection");
        ffCollection.addValue(parentNode);
        mForm.addField(ffCollection);
    }

    @Override
    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<configure>");
        buf.append(mForm.toXML());
        buf.append("</configure>");
        return buf.toString();
    }
}
