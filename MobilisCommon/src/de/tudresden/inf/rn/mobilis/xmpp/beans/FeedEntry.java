package de.tudresden.inf.rn.mobilis.xmpp.beans;

/**
 * Represents a feed entry.
 * 
 * @author István
 * 
 */
public class FeedEntry {
	private String id;
	private String title;
	private String published;
	private String updated;
	private String summary;
	private String content;
	private String gmlPoint;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getGmlPoint() {
		return gmlPoint;
	}

	public void setGmlPoint(String gmlPoint) {
		this.gmlPoint = gmlPoint;
	}

	public FeedEntry(String title, String content) {
		this.title = title;
		this.content = content;
	}

	/**
	 * Returns an XML representation of the feed that can be included in a
	 * pubsub message.
	 */
	public String toXml() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("<entry xmlns='http://www.w3.org/2005/Atom'>\n");
		buf.append("<title>");
		buf.append(title);
		buf.append("</title>");
		buf.append("<content>");
		buf.append(content);
		buf.append("</content>");
		buf.append("</entry>");
		
		return buf.toString();
	}
}
