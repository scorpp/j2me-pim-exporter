import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.*;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Screen allowing to find and select a remote Bluetooth device.
 */
public class SelectRemoteDeviceScreen extends List implements CommandListener, DiscoveryListener {

    private static final UUID UUID_OBEX_OBJECT_PUSH = new UUID(0x1105);

    private final ExportContactsMidlet midlet;
    private final SelectContactsScreen contactsScrn;

    private final DiscoveryAgent agent;

    private final Command cmdSelectCommand = new Command("", Command.ITEM, 1);
    private final Command cmdBack = new Command("Back", Command.BACK, 1);

    private Vector remoteDevices = new Vector(5);

    /**
     * Is inquiry currently in progress
     */
    private boolean inquiry;
    /**
     * Is service search currently in progress
     */
    private boolean service;


    public SelectRemoteDeviceScreen(ExportContactsMidlet m, SelectContactsScreen contactsScrn) {
        super("Bluetooth Device - Searching...", List.IMPLICIT);

        this.midlet = m;
        this.contactsScrn = contactsScrn;

        try {
            agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
            if (!agent.startInquiry(DiscoveryAgent.GIAC, this)) {
                System.err.println("Inquiry didn't start");
            } else {
                inquiry = true;
            }

        } catch (BluetoothStateException e) {
            throw new RuntimeException("Can't get discovery agent: " + e.getMessage());
        }

        setSelectCommand(cmdSelectCommand);
        addCommand(cmdBack);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (cmdSelectCommand == command) {
            doSelectCommand();
        } else if (cmdBack == command) {
            if (!inquiry && !service) {
                Display.getDisplay(midlet).setCurrent(contactsScrn);
            }
        }
    }

    private void doSelectCommand() {
        try {
            RemoteDevice rd = (RemoteDevice) remoteDevices.elementAt(getSelectedIndex());
            agent.searchServices(null, new UUID[]{UUID_OBEX_OBJECT_PUSH}, rd, this);
            service = true;
            setTitle("Searching services");

        } catch (BluetoothStateException e) {
            e.printStackTrace();
            midlet.displayError("Can't list BT devices", e.getMessage());
        }
    }

    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        remoteDevices.addElement(remoteDevice);
        try {
            append(remoteDevice.getFriendlyName(true), null);
        } catch (IOException e) {
            append(remoteDevice.getBluetoothAddress(), null);
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
        String url = null;
        for (int i = 0; i < serviceRecords.length; i++) {
            url = serviceRecords[i].getConnectionURL(ServiceRecord.
                    AUTHENTICATE_ENCRYPT, false);
        }

        if (url == null) {
            midlet.displayError("File transfer failed", "File transfer is not supported by remote device");
            return;
        }

        final String finalUrl = url;
        midlet.doInThread(new Runnable() {
            public void run() {
                try {
                    ClientSession sc = (ClientSession) Connector.open(finalUrl, Connector.READ_WRITE);
                    HeaderSet hs = sc.connect(null);

                    if (hs.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
                        midlet.displayError("File transfer failed", "Cannot connect client session");
                        sc.close();
                        return;
                    }

                    byte[] bytes = contactsScrn.exportContacts();
                    hs = sc.createHeaderSet();
                    hs.setHeader(HeaderSet.NAME, "contacts.vcf");
                    hs.setHeader(HeaderSet.TYPE, "text/x-vcard");
                    hs.setHeader(HeaderSet.LENGTH, new Long(bytes.length));

                    Operation op = sc.put(hs);
                    OutputStream os = op.openOutputStream();

                    os.write(bytes);

                    os.close();
                    op.close();
                    sc.disconnect(null);
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    midlet.displayError("File transfer failed", e.getMessage());
                } finally {
                    setTitle("Contacts sent OK");
                }
            }
        });
    }

    public void serviceSearchCompleted(int transId, int respCode) {
        service = false;
        Display.getDisplay(midlet).setCurrent(this);
        System.out.println("Service search completed " + transId + " with code " + respCode);
        switch (respCode) {
            case SERVICE_SEARCH_COMPLETED:
                setTitle("Service search done");
                break;
            case SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                midlet.displayError("No file transfer", "Device unreachable");
                break;
            case SERVICE_SEARCH_ERROR:
                midlet.displayError("No file transfer", "Error searching services");
                break;
            case SERVICE_SEARCH_NO_RECORDS:
                midlet.displayError("No file transfer", "No services found");
                break;
            case SERVICE_SEARCH_TERMINATED:
                midlet.displayError("No file transfer", "Terminated");
                break;
        }
    }

    public void inquiryCompleted(int i) {
        inquiry = false;
        setTitle("Bluetooth Device - Done");
        System.out.println("Inquiry completed " + i + " with " + remoteDevices.size() + " devices found");
    }
}
