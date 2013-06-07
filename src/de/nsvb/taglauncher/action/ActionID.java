package de.nsvb.taglauncher.action;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.nsvb.taglauncher.util.Log;


public class ActionID {
	public static final byte EMPTY = 0x00;
	public static final byte WLAN_ON = 0x01;
	public static final byte WLAN_OFF = 0x02;
	public static final byte WLAN_TOGGLE = 0x03;
	public static final byte BLUETOOTH_ON = 0x04;
	public static final byte BLUETOOTH_OFF = 0x05;
	public static final byte BLUETOOTH_TOGGLE = 0x06;
	public static final byte BLUETOOTH_CONNECT = 0x07;
	public static final byte BLUETOOTH_DISCONNECT = 0x08;
	public static final byte GPS_ON = 0x09;
	public static final byte GPS_OFF = 0x0A;
	public static final byte GPS_TOGGLE = 0x0B;
	public static final byte MOBILE_DATA_ON = 0x0C;
	public static final byte MOBILE_DATA_OFF = 0x0D;
	public static final byte MOBILE_DATA_TOGGLE = 0x0E;
	public static final byte FLIGHT_MODE_ON = 0x0F;
	public static final byte FLIGHT_MODE_OFF = 0x10;
	public static final byte FLIGHT_MODE_TOGGLE = 0x11;
	public static final byte RINGER_SILENT = 0x12;
	public static final byte RINGER_VIBRATE = 0x13;
	public static final byte RINGER_NORMAL = 0x14;
	public static final byte MEDIA_VOLUME = 0x15;
	
	public static final byte LAUNCH_APP = 0x20;
	
	public static final byte MEDIA_PLAY = 0x24;
	public static final byte MEDIA_PAUSE = 0x25;

	/**
	 * Gibt entsprechend zu {@code id} die dazugehörige Aktion zurück
	 * 
	 * @param id Ein Wert aus {@link ActionID}
	 * @return Aktion
	 */
	public static Action getAction(byte id) {
		switch (id) {
		case EMPTY:
			return null;
		case WLAN_ON:
			return new WlanOn();
		case WLAN_OFF:
			return new WlanOff();
		case WLAN_TOGGLE:
			return new WlanToggle();
		case BLUETOOTH_ON:
			return new BluetoothOn();
		case BLUETOOTH_OFF:
			return new BluetoothOff();
		case BLUETOOTH_TOGGLE:
			return new BluetoothToggle();
		case RINGER_SILENT:
			return new RingerSilent();
		case RINGER_VIBRATE:
			return new RingerVibrate();
		case RINGER_NORMAL:
			return new RingerNormal();
		case MEDIA_VOLUME:
			return new MediaVolume();
		case LAUNCH_APP:
			return new LaunchApp();
		case MEDIA_PLAY:
			return new MediaPlay();
		case MEDIA_PAUSE:
			return new MediaPause();
		default:
			Log.d(id + " is not implemented");
			return null;
		}
	}
	
	//darf nicht static sein, getClass() ist sonst nicht möglich
	/**
	 * Lädt eine Liste aller verfügbaren Aktionen
	 * 
	 * @return Liste aller verfügbaren Aktionen, leer wenn keine Aktionen vorhanden
	 */
	public List<Action> getActionList() {
		Field[] fields = ActionID.class.getFields();
		List<Action> actions = new ArrayList<Action>();
        for (Field field : fields) {
            try {
                Action temp = getAction(field.getByte(getClass()));
                if (temp != null) {
                    actions.add(temp);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
		Collections.sort(actions);
		return actions;
	}
}
