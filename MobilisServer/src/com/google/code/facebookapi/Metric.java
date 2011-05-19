package com.google.code.facebookapi;

import java.util.HashMap;
import java.util.Map;

/**
 * This class enumerates the various metrics that are available through the admin.getMetrics API call. Typically, you will pass a set containing the metrics you are
 * interested in to the API call.
 * 
 * See http://wiki.developers.facebook.com/index.php/Admin.getMetrics for details.
 * 
 * @author aroth
 */
public enum Metric {
	/**
	 * Daily active users for your app. For use with Admin.getDailyMetrics only.
	 */
	@Deprecated
	DAILY_ACTIVE_USERS("daily_active_users"),
	/**
	 * Active users for your app. For use with Admin.getMetrics only.
	 */
	ACTIVE_USERS("active_users"),
	/**
	 * Number of unique users adding your app.
	 */
	UNIQUE_ADDS("unique_adds"),
	/**
	 * Number of unique users removing your app.
	 */
	UNIQUE_REMOVES("unique_removes"),
	/**
	 * Number of unique users blocking your app.
	 */
	UNIQUE_BLOCKS("unique_blocks"),
	/**
	 * Number of unique users unblocking your app.
	 */
	UNIQUE_UNBLOCKS("unique_unblocks"),
	/**
	 * Number of API calls made by your app.
	 */
	API_CALLS("api_calls"),
	/**
	 * Number of users making API calls through your app.
	 */
	UNIQUE_API_CALLS("unique_api_calls"),
	/**
	 * Number of canvas page views.
	 */
	CANVAS_PAGE_VIEWS("canvas_page_views"),
	/**
	 * Number of unique users viewing your canvas page.
	 */
	UNIQUE_CANVAS_PAGE_VIEWS("unique_canvas_page_views"),
	/**
	 * Average time required to load your app's canvas page.
	 */
	REQUEST_TIME_AVG("canvas_http_request_time_avg"),
	/**
	 * Average time required to render your app's FBML.
	 */
	FBML_RENDER_TIME_AVG("canvas_fbml_render_time_avg"),
	/**
	 * Number of requests that timed out.
	 */
	REQUEST_TIMEOUT("canvas_page_views_http_code_0"),
	/**
	 * Number of requests, that returned http code 100.
	 */
	REQUEST_CONTINUE("canvas_page_views_http_code_100"),
	/**
	 * Number of requests that completed successfully.
	 */
	REQUEST_OK("canvas_page_views_http_code_200"),
	/**
	 * Number of requests that returned status 200, but with no data.
	 */
	REQUEST_OK_NO_DATA("canvas_page_views_http_code_200ND"),
	/**
	 * Number of requests that produced a status 301 error.
	 */
	REQUEST_ERROR_301("canvas_page_views_http_code_301"),
	/**
	 * Number of requests that produced a status 302 error.
	 */
	REQUEST_ERROR_302("canvas_page_views_http_code_302"),
	/**
	 * Number of requests that produced a status 303 error.
	 */
	REQUEST_ERROR_303("canvas_page_views_http_code_303"),
	/**
	 * Number of requests that produced a status 400 error.
	 */
	REQUEST_ERROR_400("canvas_page_views_http_code_400"),
	/**
	 * Number of requests that produced a status 401 error.
	 */
	REQUEST_ERROR_401("canvas_page_views_http_code_401"),
	/**
	 * Number of requests that produced a status 403 error.
	 */
	REQUEST_ERROR_403("canvas_page_views_http_code_403"),
	/**
	 * Number of requests that produced a status 404 error.
	 */
	REQUEST_ERROR_404("canvas_page_views_http_code_404"),
	/**
	 * Number of requests that produced a status 405 error.
	 */
	REQUEST_ERROR_405("canvas_page_views_http_code_405"),
	/**
	 * Number of requests that produced a status 413 error.
	 */
	REQUEST_ERROR_413("canvas_page_views_http_code_413"),
	/**
	 * Number of requests that produced a status 422 error.
	 */
	REQUEST_ERROR_422("canvas_page_views_http_code_422"),
	/**
	 * Number of requests that produced a status 500 error.
	 */
	REQUEST_ERROR_500("canvas_page_views_http_code_500"),
	/**
	 * Number of requests that produced a status 502 error.
	 */
	REQUEST_ERROR_502("canvas_page_views_http_code_502"),
	/**
	 * Number of requests that produced a status 503 error.
	 */
	REQUEST_ERROR_503("canvas_page_views_http_code_503"),
	/**
	 * Number of requests that produced a status 505 error.
	 */
	REQUEST_ERROR_505("canvas_page_views_http_code_505");

	/**
	 * Use in Admin.getMetrics calls to specify a daily time-period.
	 */
	public static final Long PERIOD_DAY = 86400l;
	/**
	 * Use in Admin.getMetrics calls to specify a weekly time-period.
	 */
	public static final Long PERIOD_WEEK = 604800l;
	/**
	 * Use in Admin.getMetrics calls to specify a monthly time-period.
	 */
	public static final Long PERIOD_MONTH = 2592000l;

	private String name;

	private Metric( String name ) {
		this.name = name;
	}

	protected static final Map<String,Metric> METRIC_TABLE;
	static {
		METRIC_TABLE = new HashMap<String,Metric>();
		for ( Metric metric : Metric.values() ) {
			METRIC_TABLE.put( metric.getName(), metric );
		}
	}

	/**
	 * Get the name by which Facebook refers to this metric.
	 * 
	 * @return the Facebook-supplied name of this metric.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Lookup a metric by name.
	 * 
	 * @param name
	 *            the Facebook-supplied name of the metric to lookup, such as "daily_active_users".
	 * 
	 * @return the metric the corresponds to the supplied name, or null if none exists.
	 */
	public static Metric getMetric( String name ) {
		return METRIC_TABLE.get( name );
	}

	/**
	 * Lookup a metric by HTTP error code.
	 * 
	 * @param errorCode
	 *            the code to get the metric for, such as 500, 404, 401, 200, etc..
	 * 
	 * @return the metric the corresponds to the supplied HTTP error-code, or null if none exists.
	 */
	public static Metric getErrorMetric( int errorCode ) {
		return METRIC_TABLE.get( "canvas_page_views_http_code_" + Integer.toString( errorCode ) );
	}

}
