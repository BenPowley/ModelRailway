package modelrailway;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import modelrailway.core.Event;

import org.slf4j.LoggerFactory;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.locomon.Llnmon;
import jmri.jmrix.loconet.pr3.PR3Adapter;
import jmri.jmrix.loconet.pr3.PR3SystemConnectionMemo;
import jmri.util.Log4JUtil;
import jmri.web.server.WebServerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelRailway implements LocoNetListener, ThrottleListener, Event.Listener {
	/**
	 * The PR3 Adaptor is the physical interface to the railway. It contains a
	 * USB port through which all loconet traffic is routed
	 */
	private PR3Adapter connection;
	
	/**
	 * The SystemConnectionMemo provides access to all the components of the
	 * LocoNet interface.
	 */
	private PR3SystemConnectionMemo memo;
	
	/**
	 * The list of locomotives on the system.
	 */
	private DccLocoAddress[] locomotives;
	
	/**
	 * The list of active locomotive throttles
	 */
	private DccThrottle[] throttles;
	
	private ArrayList<Event.Listener> eventListeners = new ArrayList<Event.Listener>();

	private Llnmon linmon;
	
	/**
	 * Constructor starts the JMRI application running, and then returns.
	 * @throws Exception 
	 */
	public ModelRailway(String portName, int... locomotives) throws Exception {
		// Configure Log4J
		initLog4J();
		log.info(Log4JUtil.startupInfo("Main"));

		this.locomotives = new DccLocoAddress[locomotives.length];
		this.throttles = new DccThrottle[locomotives.length];
		for(int i = 0;i!=locomotives.length;++i) {
			this.locomotives[i] = new DccLocoAddress(locomotives[i],false);
		}
		// ============================================================
		// Create the loconet connection
		// ============================================================
		connection = new PR3Adapter();
		connection.openPort(portName, "Modelrailway App");
		connection.setCommandStationType("DCS51 (Zephyr Xtra)");
		System.out.println("OPTIONS: " + connection.getPortNames() + ", " + Arrays.toString(connection.getOptions()));
		connection.setOptionState("CommandStation", "false");
		connection.connect();
		connection.configure();
		memo = (PR3SystemConnectionMemo) connection.getSystemConnectionMemo();
		memo.configureCommandStation(true, true, "DCS51 (Zephyr Xtra)", false, false);
		memo.getLnTrafficController().addLocoNetListener(LnTrafficController.ALL, this);
		requestThrottles();		
	}

	/**
	 * Destroy this railway connection, dropping all resources.
	 */
	public void destroy() {
		connection.dispose();
	}
	
	/**
	 * Request throttles for all locomotives
	 */
	private void requestThrottles() {
		ThrottleManager manager = memo.getThrottleManager();
		for (DccLocoAddress loco : locomotives) {
			System.out.println("Requesting throttle for locomotive: " + loco);
			manager.requestThrottle(loco,this);
		}
	}
	
	@Override
	public void notifyFailedThrottleRequest(DccLocoAddress arg0, String arg1) {
		System.out.println("FAILED REQUESTING THROTTLE: " + arg0);
	}

	@Override
	public void notifyThrottleFound(DccThrottle arg0) {
		System.out.println("OBTAINED THROTTLE: " + arg0);
		for(int i=0;i!=locomotives.length;++i) {
			if(locomotives[i].equals(arg0.getLocoAddress())) {
				System.out.println("MATCHED THROTTLE: " + arg0.getLocoAddress());
				throttles[i] = arg0;
			}
		}
	}
	
	public void setTrainSpeed(int id, float speed) {
		throttles[id].setIsForward(true);
		throttles[id].setSpeedSetting(speed);
	}
	
	// ===============================================================
	// Message Listeners and Handlers
	// ===============================================================
	

	public void addEventListener(Event.Listener listener) {
		this.eventListeners.add(listener);
	}
	
	/**
	 * The message listener which is called for all broadcasted loconet
	 * messages. This processes each message and turns it into an appropriate
	 * instance of Event.
	 */
	@Override
	public void message(LocoNetMessage arg0) {
		Event event = null;
		
		if(linmon == null) {
			linmon = new Llnmon();
		}
		System.out.println("MESSAGE: " + linmon.displayMessage(arg0));
		
		// First, process loconet message
		int opcode = arg0.getOpCode();
		switch(opcode) {
		case LnConstants.OPC_GPON:
		case LnConstants.OPC_GPOFF:
			event = new Event.PowerChanged(opcode == LnConstants.OPC_GPON);
			break;
		case LnConstants.OPC_LOCO_DIRF:
			boolean isForward = (arg0.getElement(2) & LnConstants.DIRF_DIR) == LnConstants.DIRF_DIR;
			event = new Event.DirectionChanged(arg0.getElement(1), isForward);
			break;
		case LnConstants.OPC_LOCO_SPD:
			int speed = arg0.getElement(2);
			if(speed == 1) {
				speed = -1;
			} else if(speed > 1) {
				speed = speed - 1;
			}
			event = new Event.SpeedChanged(arg0.getElement(1), speed, 0x7F-1);
			break;
		case LnConstants.OPC_LOCO_ADR:
			System.out.println("GOT ADDRESS - " + arg0.getElement(1) + ","  + arg0.getElement(2));
			break;
		default:
			// this is an unrecognised message, which we'll just silently ignore
			// for now.
		}
		
		// Second, dispatch message as event (if understood)
		if(event != null) {
			for(Event.Listener listener : eventListeners) {
				listener.notify(event);
			}
		}		
	}
	
	public void notify(Event event) {
		
	}
		
	/**
	 * Static method to get Log4J working before the rest of JMRI starts up.
	 */
	static protected void initLog4J() {
		// initialize log4j - from logging control file (lcf) only
		// if can find it!
		String logFile = "default.lcf";
		try {
			if (new java.io.File(logFile).canRead()) {
				org.apache.log4j.PropertyConfigurator.configure(logFile);
			} else {
				org.apache.log4j.BasicConfigurator.configure();
				org.apache.log4j.Logger.getRootLogger().setLevel(
						org.apache.log4j.Level.ERROR);
			}
		} catch (java.lang.NoSuchMethodError e) {
			log.error("Exception starting logging: " + e);
		}
		// install default exception handlers
		System.setProperty("sun.awt.exception.handler",
				jmri.util.exceptionhandler.AwtHandler.class.getName());
		Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
	}

	static Logger log = LoggerFactory.getLogger(ModelRailway.class.getName());
}
