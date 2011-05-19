package com.google.code.facebookapi;

import java.util.Map;

/**
 * Represents a bactched Facebook API request.
 * 
 * @author aroth
 */
// package-level access intentional (at least for now)
class BatchQuery {

	private IFacebookMethod method;
	private Map<String,String> params;

	public BatchQuery( IFacebookMethod method, Map<String,String> params ) {
		this.method = method;
		this.params = params;
	}

	public IFacebookMethod getMethod() {
		return method;
	}

	public void setMethod( FacebookMethod method ) {
		this.method = method;
	}

	public Map<String,String> getParams() {
		return params;
	}

	public void setParams( Map<String,String> params ) {
		this.params = params;
	}

}
