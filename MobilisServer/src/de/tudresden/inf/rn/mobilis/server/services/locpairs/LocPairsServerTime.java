package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairsServerTime;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.NtpMessage;

/**
 * The Class LocPairsServerTime.
 * This class provides the time that should be used in the entire application.
 * It is synchronized with the client applications via ntp-synchronization. 
 * 
 * @author Reik Mueller
 */
public class LocPairsServerTime {

	/** The Constant ntpHost. */
	public static final String ntpHost = "0.de.pool.ntp.org";
	private static long offset;
	@SuppressWarnings("unused")
	private static LocPairsServerTime instance = new LocPairsServerTime();

	private LocPairsServerTime(){
		try {
			getOffset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public static Date getTime(){
		long time = Calendar.getInstance().getTimeInMillis();
		long newTime = time + offset;
		return new Date(newTime);
	}
	
	/**
	 * Gets the time in milliseconds.
	 *
	 * @return the milliseconds
	 */
	public static Long getMilli(){
		long time = Calendar.getInstance().getTimeInMillis();
		long newTime = time + offset;
		return new Long(newTime);
	}
	
	/**
	 * Synchronize.
	 * Synchronizes the LocPairsServerTime.
	 *
	 * @return true, if successful
	 */
	public static boolean synchronize() {
		try {
			getOffset();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void getOffset() throws IOException {
			DatagramSocket socket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(ntpHost);
			byte[] buf = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, 123);
			NtpMessage.encodeTimestamp(packet.getData(), 40, (Calendar.getInstance().getTimeInMillis() / 1000.0) + 2208988800.0);

			socket.send(packet);
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			NtpMessage msg = new NtpMessage(packet.getData());
			double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
//			System.out.println(destinationTimestamp);

			Double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
			offset = localClockOffset.longValue()*1000;
	}
}
