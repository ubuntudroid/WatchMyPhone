package de.tudresden.inf.rn.mobilis.server.services.xhunt.helper;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Route;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Station;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Ticket;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.AreaInfo;

public class SqlHelper {
	
	private Connection mMysqlConnection;
	private Statement mStatement = null;
	private PreparedStatement mPreparedStatement = null;
	private ResultSet mResultSet = null;
	
	private String mServerAddress;
	private String mServerPort;
	
	private String mDbName;
	private String mDbUsername;
	private String mDbPassword;
	
	private static final String TABLE_AREA = "XHunt_Area";
	private static final String TABLE_AREA_HAS_ROUTES = "XHunt_Area_has_Routes";
	private static final String TABLE_ROUTE = "XHunt_Route";
	private static final String TABLE_ROUTE_HAS_STATIONS = "XHunt_Route_has_Stations";
	private static final String TABLE_STATION = "XHunt_Station";
	private static final String TABLE_TICKET = "XHunt_Ticket";
	
	private static final String FK_AREA_HAS_ROUTES_AREA = "fk_Area_has_Routes_Area";
	private static final String FK_AREA_HAS_ROUTES_ROUTES = "fk_Area_has_Routes_Routes";
	private static final String FK_ROUTE_HAS_STATIONS_ROUTE = "fk_Route_has_Stations_Route";
	private static final String FK_ROUTE_HAS_STATIONS_STATIONS = "fk_Route_has_Stations_Stations";
	private static final String FK_ROUTE_TICKET = "fk_Route_Ticket";
	
	private static final String COLUMN_ID = "ID";
	private static final String COLUMN_NAME = "Name";
	private static final String COLUMN_DESCRIPTION = "Description";
	private static final String COLUMN_VERSION = "Version";
	
	private static final String COLUMN_AREA_ID = "Area_ID";
	private static final String COLUMN_ROUTE_ID = "Route_ID";
	
	private static final String COLUMN_TICKET_ID = "Ticket_ID";
	private static final String COLUMN_STARTNAME = "StartName";
	private static final String COLUMN_ENDNAME = "EndName";
	
	private static final String COLUMN_ICON = "Icon";
	private static final String COLUMN_ISSUPERIOR = "Is_Superior";
	
	private static final String COLUMN_STATION_ID = "Station_ID";
	private static final String COLUMN_POSITION = "Position";
	
	private static final String COLUMN_ABBREAVIATION = "Abbreviation";
	private static final String COLUMN_LATITUDE = "Latitude";
	private static final String COLUMN_LONGITUDE = "Longitude";
	
	private static final String XML_TAG_AREA = "area";
	private static final String XML_TAG_ID = "id";
	private static final String XML_TAG_NAME = "name";
	private static final String XML_TAG_DESC = "desc";
	private static final String XML_TAG_VERSION = "version";
	
	private static final String XML_TAG_TICKETS = "Tickets";
	private static final String XML_TAG_TICKET = "Ticket";
	
	private static final String XML_TAG_STATIONS = "Stations";
	private static final String XML_TAG_STATION = "Station";
	
	private static final String XML_TAG_ROUTES = "Routes";
	private static final String XML_TAG_ROUTE = "Route";
	private static final String XML_TAG_STOP = "stop";
	
	private static final String XML_ATTR_ID = "id";
	private static final String XML_ATTR_NAME = "name";
	private static final String XML_ATTR_TYPE = "type";
	private static final String XML_ATTR_START = "start";
	private static final String XML_ATTR_END = "end";
	
	private static final String XML_ATTR_POS = "pos";
	
	private static final String XML_ATTR_ABBREV = "abbrev";
	private static final String XML_ATTR_LATITUDE = "latitude";
	private static final String XML_ATTR_LONGITUDE = "longitude";
	
	private static final String XML_ATTR_ICON = "icon";
	private static final String XML_ATTR_ISSUPERIOR = "issuperior";
	
	private XHunt mController;
	

	public SqlHelper(XHunt control) {
		this.mController = control;
		loadDbDriver();
	}
	
	public boolean checkDbStructure(){
		boolean isStructureOk = false;
		
		try {
			mMysqlConnection = DriverManager
				.getConnection(getConnectionURI());
			
			mPreparedStatement = mMysqlConnection
				.prepareStatement("select ID, Name, Description, Version from " + mDbName + "." + TABLE_AREA);			
			mPreparedStatement.executeQuery();
			
			mPreparedStatement = mMysqlConnection
				.prepareStatement("select Area_ID, Route_ID from " + mDbName + "." + TABLE_AREA_HAS_ROUTES);			
			mPreparedStatement.executeQuery();
		
			mPreparedStatement = mMysqlConnection
				.prepareStatement("select ID, Ticket_ID, Name, StartName, EndName from " + mDbName + "." + TABLE_ROUTE);			
			mPreparedStatement.executeQuery();
	
			mPreparedStatement = mMysqlConnection
				.prepareStatement("select Route_ID, Station_ID, Position from " + mDbName + "." + TABLE_ROUTE_HAS_STATIONS);			
			mPreparedStatement.executeQuery();

			mPreparedStatement = mMysqlConnection
				.prepareStatement("select ID, Name, Abbreviation, Latitude, Longitude from " + mDbName + "." + TABLE_STATION);			
			mPreparedStatement.executeQuery();

			mPreparedStatement = mMysqlConnection
				.prepareStatement("select ID, Name, Icon, Is_Superior from " + mDbName + "." + TABLE_TICKET);			
			mPreparedStatement.executeQuery();
			
			isStructureOk = true;			
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
			isStructureOk = false;
		}
		
		return isStructureOk;
	}
	
	public void disconnect(){
		try {
			if (mMysqlConnection != null) {
				mMysqlConnection.close();
			}
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
	}
	
	private void flush() {
		try {
			if (mResultSet != null) {
				mResultSet.close();
			}

			if (mStatement != null) {
				mStatement.close();
			}
		} catch (Exception e) {

		}
	}
	
	public boolean createDbStructure(){
		boolean isStructureCreated = false;
		try {
			mMysqlConnection = DriverManager
				.getConnection(getConnectionURI());
			
			// create table station
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_STATION + " (" +
						COLUMN_ID + " INT NOT NULL ," +
						COLUMN_NAME + " VARCHAR(45) NOT NULL ," +
						COLUMN_ABBREAVIATION + " VARCHAR(5) NULL ," +
						COLUMN_LATITUDE + " INT NOT NULL ," +
						COLUMN_LONGITUDE + " INT NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_ID + ") )");
			mPreparedStatement.executeUpdate();
			
			//create tabel ticket
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_TICKET + " (" +
						COLUMN_ID + " INT NOT NULL ," +
						COLUMN_NAME + " VARCHAR(45) NOT NULL ," +
						COLUMN_ICON + " VARCHAR(45) NOT NULL ," +
						COLUMN_ISSUPERIOR + " INT NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_ID + ") )");
			mPreparedStatement.executeUpdate();
			
			// create table route
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_ROUTE + " (" +
						COLUMN_ID + " INT NOT NULL ," +
						COLUMN_TICKET_ID + " INT NOT NULL ," +
						COLUMN_NAME + " VARCHAR(45) NOT NULL ," +
						COLUMN_STARTNAME + " VARCHAR(45) NOT NULL ," +
						COLUMN_ENDNAME + " VARCHAR(45) NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_TICKET_ID + ") ," +
						" CONSTRAINT " + FK_ROUTE_TICKET + 
						" FOREIGN KEY (" + COLUMN_TICKET_ID + " )" +
						" REFERENCES " + mDbName + "." + TABLE_TICKET + " (" + COLUMN_ID + " )" +
						" ON DELETE NO ACTION" +
						" ON UPDATE NO ACTION)");
			
			mPreparedStatement.executeUpdate();
			
			// create fk route ticket index
			/*mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE INDEX " + FK_ROUTE_TICKET + " ON " + mDbName + "." + TABLE_ROUTE +
						" (" + COLUMN_TICKET_ID + " ASC)");
			mPreparedStatement.execute();*/
			
			// create table area
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_AREA + " (" +
						COLUMN_ID + " INT NOT NULL ," +
						COLUMN_NAME + " VARCHAR(45) NOT NULL ," +
						COLUMN_DESCRIPTION + " VARCHAR(45) NULL ," +
						COLUMN_VERSION + " INT NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_ID + ") )");
			mPreparedStatement.executeUpdate();
			
			// create table area has routes
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_AREA_HAS_ROUTES + " (" +
						COLUMN_AREA_ID + " INT NOT NULL ," +
						COLUMN_ROUTE_ID + " INT NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_AREA_ID + ", " + COLUMN_ROUTE_ID + ") ," +
						" CONSTRAINT " + FK_AREA_HAS_ROUTES_AREA + 
						" FOREIGN KEY (" + COLUMN_AREA_ID + " )" +
						" REFERENCES " + mDbName + "." + TABLE_AREA + " (" + COLUMN_ID + " )" +
						" ON DELETE NO ACTION" +
						" ON UPDATE NO ACTION," +
						" CONSTRAINT " + FK_AREA_HAS_ROUTES_ROUTES + 
						" FOREIGN KEY (" + COLUMN_ROUTE_ID + " )" +
						" REFERENCES " + mDbName + "." + TABLE_ROUTE + " (" + COLUMN_ID + " )" +
						" ON DELETE NO ACTION" +
						" ON UPDATE NO ACTION)");
			mPreparedStatement.executeUpdate();
			
			// create index area has routes
			/*mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE INDEX " + FK_AREA_HAS_ROUTES_ROUTES + " ON " + mDbName + "." + TABLE_AREA_HAS_ROUTES + 
						" (" + COLUMN_ROUTE_ID + " ASC)");
			mPreparedStatement.executeUpdate();*/
			
			// create table route has stations
			mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE  TABLE IF NOT EXISTS " + mDbName + "." + TABLE_ROUTE_HAS_STATIONS + " (" +
						COLUMN_ROUTE_ID + " INT NOT NULL ," +
						COLUMN_STATION_ID + " INT NOT NULL ," +
						COLUMN_POSITION + " INT NOT NULL ," +
						" PRIMARY KEY (" + COLUMN_ROUTE_ID + ", " + COLUMN_STATION_ID + ") ," +
						" CONSTRAINT " + FK_ROUTE_HAS_STATIONS_ROUTE + 
						" FOREIGN KEY (" + COLUMN_ROUTE_ID + " )" +
						" REFERENCES " + mDbName + "." + TABLE_ROUTE + "(" + COLUMN_ID + " )" +
						" ON DELETE NO ACTION" +
						" ON UPDATE NO ACTION," +
						" CONSTRAINT " + FK_ROUTE_HAS_STATIONS_STATIONS + 
						" FOREIGN KEY (" + COLUMN_STATION_ID + " )" +
						" REFERENCES " + mDbName + "." + TABLE_STATION + " (" + COLUMN_ID + " )" +
						" ON DELETE NO ACTION" +
						" ON UPDATE NO ACTION)");
			mPreparedStatement.executeUpdate();
			
			// create index route has stations
			/*mPreparedStatement = mMysqlConnection
				.prepareStatement("CREATE INDEX " + FK_ROUTE_HAS_STATIONS_STATIONS + " ON " + mDbName + "." + TABLE_ROUTE_HAS_STATIONS +
						" (" + COLUMN_STATION_ID + " ASC)");
			mPreparedStatement.executeUpdate();*/
			
			isStructureCreated = true;
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
			isStructureCreated = false;
		}
		
		return isStructureCreated;
	}
	
	public File exportAreaData(int areaId, String folderPath) {
		File exportFile = null;
		String areaName = null;
		String areaDescription = null;
		int areaVersion = -1;
		
		ArrayList<Route> routes = new ArrayList<Route>();
		ArrayList<Station> stations = new ArrayList<Station>();
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		try {
			mMysqlConnection = DriverManager
				.getConnection(getConnectionURI());
			
			mStatement = mMysqlConnection.createStatement();
			mResultSet = mStatement
					.executeQuery("select * from " + mDbName + "." + TABLE_AREA
							+ " where " + COLUMN_ID + "=" + areaId);
			
			// query general area data
			if (mResultSet.next()) {
				areaDescription = mResultSet.getString(COLUMN_DESCRIPTION);
				areaName = mResultSet.getString(COLUMN_NAME);
				areaVersion= mResultSet.getInt(COLUMN_VERSION);
			}
			
			tickets = queryAreaTickets(areaId);
			stations = queryAreaStations(areaId);
			routes = queryAreaRoutes(areaId);
			
			exportFile = writeAreaDataToXml(areaId, areaName, areaDescription, areaVersion,
					routes, stations, tickets, folderPath);
			
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		} catch (IOException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
		
		flush();

		return exportFile;
	}
	
	private void loadDbDriver(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
	}
	
	
	public HashMap<Integer, Route> queryAreaRoutesMap(int areaId) {
		HashMap<Integer, Route> routes = new HashMap<Integer, Route>();
		
		try {
			for(Route route : queryAreaRoutes(areaId)){
				routes.put(route.getId(), route);
			}
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
		
		return routes;
	}
	
	public ArrayList<Route> queryAreaRoutes(int areaId) throws SQLException {
		ArrayList<Route> routes = new ArrayList<Route>();
		
		String strStatement = "select routes." + COLUMN_ID + ", routes." + COLUMN_TICKET_ID + 
			", routes." + COLUMN_NAME + ", routes." + COLUMN_STARTNAME +
			", routes." + COLUMN_ENDNAME + " from " + mDbName + "." + TABLE_AREA +
			" as area, ( select distinct route." + COLUMN_ID + ", route." + COLUMN_TICKET_ID +
			", route." + COLUMN_NAME + ", route." + COLUMN_STARTNAME + ", route." + COLUMN_ENDNAME +
			" from " + mDbName + "." + TABLE_AREA_HAS_ROUTES + " as ahr, " + mDbName + 
			"." + TABLE_ROUTE + " as route ) as routes where area." + COLUMN_ID + "=" + areaId;
//		mController.log(strStatement);
		
		mStatement = mMysqlConnection.createStatement();
		mResultSet = mStatement.executeQuery(strStatement);
		
		while (mResultSet.next()) {
			Route route = new Route();
			route.setId(mResultSet.getInt(COLUMN_ID));
			route.setName(mResultSet.getString(COLUMN_NAME));
			route.setTicketId(mResultSet.getInt(COLUMN_TICKET_ID));
			route.setStart(mResultSet.getString(COLUMN_STARTNAME));
			route.setEnd(mResultSet.getString(COLUMN_ENDNAME));
			
			routes.add(route);			
		}
		
		for(Route route : routes){
			String strStatement2 = "select pos." + COLUMN_STATION_ID + ", pos." + COLUMN_POSITION + 
				" from " + mDbName + "." + TABLE_AREA + " as area, ( select pos." +
				COLUMN_ROUTE_ID + ", pos." + COLUMN_STATION_ID + ", pos." + COLUMN_POSITION +
				" from " + mDbName + "." + TABLE_AREA_HAS_ROUTES + " as ahr, ( select rhs." + COLUMN_ROUTE_ID +
				", rhs." + COLUMN_STATION_ID + ", rhs." + COLUMN_POSITION + 
				" from "+ mDbName + "." + TABLE_ROUTE + " as route, " + mDbName + 
				"." + TABLE_ROUTE_HAS_STATIONS + " as rhs) as pos where pos." + COLUMN_ROUTE_ID +
				"=" + route.getId() + " group by pos." + COLUMN_STATION_ID +
				") as pos where area." + COLUMN_ID + "=" + areaId + " order by pos." + COLUMN_POSITION;
//			mController.log(strStatement2);
			
			mStatement = mMysqlConnection.createStatement();
			mResultSet = mStatement.executeQuery(strStatement2);
			
			while (mResultSet.next()) {
				route.addStation(mResultSet.getInt(COLUMN_POSITION), mResultSet.getInt(COLUMN_STATION_ID));
			}
			
			mController.log("Read from DB: " + route.toString());
		}
		
		return routes;
	}
	
	public HashMap<Integer, Station> queryAreaStationsMap(int areaId) {
		HashMap<Integer, Station> stations = new HashMap<Integer, Station>();
		
		try {
			for(Station station : queryAreaStations(areaId)){
				stations.put(station.getId(), station);
			}
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
		
		return stations;
	}
	
	public ArrayList<Station> queryAreaStations(int areaId) throws SQLException {
		ArrayList<Station> stations = new ArrayList<Station>();
		
		String strStatement = "select stations." + COLUMN_ID + ", stations." + COLUMN_NAME +
			", stations." + COLUMN_ABBREAVIATION + ", stations." + COLUMN_LATITUDE +
			" , stations." + COLUMN_LONGITUDE + " from " + mDbName + "." + TABLE_AREA + " as area, " +
			" (	select distinct stations." + COLUMN_ID + ", stations." + COLUMN_NAME +
			", stations." + COLUMN_ABBREAVIATION + ", stations." + COLUMN_LATITUDE +
			" , stations." + COLUMN_LONGITUDE + " from " + mDbName + "." + TABLE_AREA_HAS_ROUTES + " as ahr, " + 
			" (	select stations." + COLUMN_ID + ", stations." + COLUMN_NAME +
			", stations." + COLUMN_ABBREAVIATION + ", stations." + COLUMN_LATITUDE +
			" , stations." + COLUMN_LONGITUDE + " from " + mDbName + "." + TABLE_ROUTE + " as route, " + 
			" (	select distinct station." + COLUMN_ID + ", station." + COLUMN_NAME +
			", station." + COLUMN_ABBREAVIATION + ", station." + COLUMN_LATITUDE +
			" , station." + COLUMN_LONGITUDE + " from " + mDbName + "." + TABLE_ROUTE_HAS_STATIONS + " as rhs, " +
			mDbName + "." + TABLE_STATION + " as station ) as stations " +
			") as stations ) as stations where area." + COLUMN_ID + "=" + areaId;
//		mController.log(strStatement);
			
		mStatement = mMysqlConnection.createStatement();
		mResultSet = mStatement.executeQuery(strStatement);
		
		while (mResultSet.next()) {
			Station station = new Station();
			station.setId(mResultSet.getInt(COLUMN_ID));
			station.setName(mResultSet.getString(COLUMN_NAME));
			station.setAbbrevation(mResultSet.getString(COLUMN_ABBREAVIATION));
			station.setGeoPoint(mResultSet.getInt(COLUMN_LATITUDE), mResultSet.getInt(COLUMN_LONGITUDE));
			
			stations.add(station);
			mController.log("Read from DB: " + station.toString());
		}
		
		return stations;
	}
	
	public HashMap<Integer, Ticket> queryAreaTicketsMap(int areaId) {
		HashMap<Integer, Ticket> tickets = new HashMap<Integer, Ticket>();
		
		try {
			for(Ticket ticket : queryAreaTickets(areaId)){
				tickets.put(ticket.getId(), ticket);
			}
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
		
		return tickets;
	}
	
	public ArrayList<Ticket> queryAreaTickets(int areaId) throws SQLException {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		String strStatement = "select tickets." + COLUMN_ID + ", tickets." + COLUMN_NAME +
			", tickets." + COLUMN_ICON + ", tickets." + COLUMN_ISSUPERIOR + 
			" from " + mDbName + "." + TABLE_AREA + " as area, " +
			" (	select distinct tickets." + COLUMN_ID + ", tickets." + COLUMN_NAME +
			", tickets." + COLUMN_ICON + ", tickets." + COLUMN_ISSUPERIOR +
			" from " + mDbName + "." + TABLE_AREA_HAS_ROUTES + " as ahr, " +
			" (	select ticket." + COLUMN_ID + ", ticket." + COLUMN_NAME +
			", ticket." + COLUMN_ICON + ", ticket." + COLUMN_ISSUPERIOR +
			" from " + mDbName + "." + TABLE_ROUTE + " as route, " + 
			mDbName + "." + TABLE_TICKET + " as ticket ) as tickets " + 
			") as tickets where area." + COLUMN_ID + "=" + areaId;
//		mController.log(strStatement);
		
		mStatement = mMysqlConnection.createStatement();
		mResultSet = mStatement.executeQuery(strStatement);
		
		while (mResultSet.next()) {
			Ticket ticket = new Ticket();
			ticket.setId(mResultSet.getInt(COLUMN_ID));
			ticket.setName(mResultSet.getString(COLUMN_NAME));
			ticket.setIcon(mResultSet.getString(COLUMN_ICON));
			ticket.setSuperior(mResultSet.getInt(COLUMN_ISSUPERIOR) == 1);
			
			tickets.add(ticket);
			mController.log("Read from DB: " + ticket.toString());
		}
		
		return tickets;
	}
	
	public ArrayList<AreaInfo> queryAreas(){
		ArrayList<AreaInfo> areaInfos = new ArrayList<AreaInfo>();
		
		try {
			mMysqlConnection = DriverManager
				.getConnection(getConnectionURI());
			
			mStatement = mMysqlConnection.createStatement();
			mResultSet = mStatement
					.executeQuery("select * from " + mDbName + "." + TABLE_AREA);
			
			while (mResultSet.next()) {
				AreaInfo info = new AreaInfo();
				
				info.AreaId = mResultSet.getInt(COLUMN_ID);
				info.AreaName = mResultSet.getString(COLUMN_NAME);
				info.AreaDescription = mResultSet.getString(COLUMN_DESCRIPTION);				
				info.Version = mResultSet.getInt(COLUMN_VERSION);
				
				areaInfos.add(info);
			}
			
			for(AreaInfo info : areaInfos){
				HashMap<Integer, String> ticketTypes = new HashMap<Integer, String>();
				ArrayList<Ticket> tickets = queryAreaTickets(info.AreaId);
				
				for(Ticket ticket : tickets){
					ticketTypes.put(ticket.getId(), ticket.getName());
				}
				
				info.Tickettypes = ticketTypes;
			}
			
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}
		
		flush();
		return areaInfos;
	}
	
	
	public boolean testConnection(){
		boolean connected = false;
		try {			
			mMysqlConnection = DriverManager
				.getConnection(getConnectionURI());
			
			connected = true;
		} catch (SQLException e) {
			mController.log("!EXCEPTION: " + e.getMessage());
		}

		return connected;
	}	
	
	private File writeAreaDataToXml(int areaId, String areaName, String areaDesc, int areaVersion,
			ArrayList<Route> routes, ArrayList<Station> stations, ArrayList<Ticket> tickets, String folderPath) 
			throws IOException {
		FileWriter fileWriter;
		File xmlFile = new File(folderPath + "area_" + areaId + "_v" + areaVersion + ".xml");
		
		if(xmlFile.exists())
			xmlFile.delete();
		else
			xmlFile.createNewFile();
		
		fileWriter = new FileWriter(xmlFile, true);
		
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); 
		
		bufferedWriter.write("<" + XML_TAG_AREA + ">");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		
		
		// write general area info
		bufferedWriter.write("<" + XML_TAG_ID + ">" + areaId + "</" + XML_TAG_ID + ">");
		bufferedWriter.newLine();
		bufferedWriter.write("<" + XML_TAG_NAME + ">" + areaName + "</" + XML_TAG_NAME + ">");
		bufferedWriter.newLine();
		bufferedWriter.write("<" + XML_TAG_DESC + ">" + areaDesc + "</" + XML_TAG_DESC + ">");
		bufferedWriter.newLine();
		bufferedWriter.write("<" + XML_TAG_VERSION + ">" + areaVersion + "</" + XML_TAG_VERSION + ">");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		
		
		// write tickets
		bufferedWriter.write("<" + XML_TAG_TICKETS + ">");
		bufferedWriter.newLine();
		
		for(Ticket ticket : tickets){
			bufferedWriter.write("<" + XML_TAG_TICKET + " " 
					+ XML_ATTR_ID + "=\"" + ticket.getId() + "\" "
					+ XML_ATTR_NAME + "=\"" + ticket.getName() + "\" "
					+ XML_ATTR_ICON + "=\"" + ticket.getIcon() + "\" "
					+ XML_ATTR_ISSUPERIOR + "=\"" + ticket.isSuperior() + "\" " 
					+ "></" + XML_TAG_TICKET + ">");
			bufferedWriter.newLine();
			mController.log("Wrote to file: " + ticket.toString());
		}
		
		bufferedWriter.write("</" + XML_TAG_TICKETS + ">");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		
		
		// write stations
		bufferedWriter.write("<" + XML_TAG_STATIONS + ">");
		bufferedWriter.newLine();
		
		for(Station station : stations){
			bufferedWriter.write("<" + XML_TAG_STATION + " " 
					+ XML_ATTR_ID + "=\"" + station.getId() + "\" "
					+ XML_ATTR_ABBREV + "=\"" + station.getAbbrevation() + "\" "
					+ XML_ATTR_NAME + "=\"" + station.getName() + "\" "
					+ XML_ATTR_LATITUDE + "=\"" + station.getLatitude() + "\" "
					+ XML_ATTR_LONGITUDE + "=\"" + station.getLongitude() + "\" " 
					+ "></" + XML_TAG_STATION + ">");
			bufferedWriter.newLine();
			mController.log("Wrote to file: " + station.toString());
		}
		
		bufferedWriter.write("</" + XML_TAG_STATIONS + ">");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		
		
		// write routes
		bufferedWriter.write("<" + XML_TAG_ROUTES + ">");
		bufferedWriter.newLine();
		
		for(Route route : routes){
			bufferedWriter.write("<" + XML_TAG_ROUTE + " " 
					+ XML_ATTR_ID + "=\"" + route.getId() + "\" "
					+ XML_ATTR_NAME + "=\"" + route.getName() + "\" "
					+ XML_ATTR_TYPE + "=\"" + route.getTicketId() + "\" "
					+ XML_ATTR_START + "=\"" + route.getStart() + "\" "
					+ XML_ATTR_END + "=\"" + route.getEnd() + "\" >");
			
			for(Map.Entry<Integer, Integer> entry : route.getStationIds().entrySet()){
				bufferedWriter.newLine();
				bufferedWriter.write("<" + XML_TAG_STOP + " " 
						+ XML_ATTR_POS + "=\"" + entry.getKey() + "\" >"
						+ entry.getValue()
						+ "</" + XML_TAG_STOP + ">");				
			}
			
			bufferedWriter.newLine();					
			bufferedWriter.write("</" + XML_TAG_ROUTE + ">");
			
			bufferedWriter.newLine();
			mController.log("Wrote to file: " + route.toString());
		}
		
		bufferedWriter.write("</" + XML_TAG_ROUTES + ">");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		
		
		bufferedWriter.write("</" + XML_TAG_AREA + ">");
		
		bufferedWriter.close(); 
		
		return xmlFile;
	}
	
	
	// Getter And Setter
	
	private String getConnectionURI(){
		return "jdbc:mysql://"
			+ mServerAddress + ":" + mServerPort
			+ "/" + mDbName + "?"
			+ "user=" + mDbUsername + "&password=" + mDbPassword;
	}
	
	public void setSqlConnectionData(String serverAddress, String serverPort, String dbName,
			String dbUsername, String dbPassword) {
		this.mServerAddress = serverAddress;
		this.mServerPort = serverPort;
		this.mDbName = dbName;
		this.mDbUsername = dbUsername;
		this.mDbPassword = dbPassword;
	}
	

	public String getServerAddress() {
		return mServerAddress;
	}

	public void setServerAddress(String mDbServerAddress) {
		this.mServerAddress = mDbServerAddress;
	}

	public String getServerPort() {
		return mServerPort;
	}

	public void setServerPort(String mServerPort) {
		this.mServerPort = mServerPort;
	}

	public String getDbName() {
		return mDbName;
	}

	public void setDbName(String mDbName) {
		this.mDbName = mDbName;
	}

	public String getDbUsername() {
		return mDbUsername;
	}

	public void setDbUsername(String mDbUsername) {
		this.mDbUsername = mDbUsername;
	}

	public String getDbPassword() {
		return mDbPassword;
	}

	public void setDbPassword(String mDbPassword) {
		this.mDbPassword = mDbPassword;
	}	

}
