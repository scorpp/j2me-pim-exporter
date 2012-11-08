import logger.Logger;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Screen allowing to choose contacts to export.
 */
public class SelectContactsScreen extends List implements CommandListener {

    private final Logger logger;

    private final ExportContactsMidlet midlet;
    private final PimContactsDao dao;

    private final Command cmdExport = new Command("Export", Command.SCREEN, 1);
    private final Command cmdSelectAll = new Command("Select all", Command.SCREEN, 1);
    private final Command cmdShowInfo = new Command("Info", Command.SCREEN, 1);
    private final Command cmdExit = new Command("Exit", Command.EXIT, 1);

    private Vector pimItems;

    public SelectContactsScreen(ExportContactsMidlet m, PimContactsDao dao) {
        super("Contacts", List.MULTIPLE);

        this.midlet = m;
        this.dao = dao;
        this.logger = new Logger(getClass());

        loadContacts();

        addCommand(cmdExport);
        addCommand(cmdSelectAll);
        addCommand(cmdShowInfo);
        addCommand(cmdExit);
        setCommandListener(this);
    }

    private void loadContacts() {
        try {
            this.pimItems = dao.loadAllContacts();

            Enumeration e = pimItems.elements();
            while (e.hasMoreElements()) {
                PIMItem pi = (PIMItem) e.nextElement();
                String name = dao.getPimDisplayName(pi);
                append(name, null);
            }

        } catch (PIMException e) {
            e.printStackTrace();
            midlet.displayError("Can't load contacts", e.getMessage() + "\nReason: " + e.getReason());
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == cmdExport) {
            doExport();
        } else if (command == cmdSelectAll) {
            doSelectAll();
        } else if (command == cmdShowInfo) {
            doShowInfo();
        } else if (command == cmdExit) {
            doExit();
        }
    }

    private void doShowInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Version: ").append(PimContactsDao.getPimVersion()).append('\n');
        sb.append("Formats: ").append(dao.getSupportedSerialFormats()).append('\n');

        Display.getDisplay(midlet).setCurrent(new Alert("PIM Info", sb.toString(), null, AlertType.INFO));
    }

    private void doSelectAll() {
        boolean[] selected = new boolean[size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = true;
        }
        setSelectedFlags(selected);
    }

    private void doExit() {
        midlet.notifyDestroyed();
    }

    private void doExport() {
        Display.getDisplay(midlet).setCurrent(new SelectRemoteDeviceScreen(midlet, this));
//        Display.getDisplay(midlet).setCurrent(new SelectDirScreen(midlet, this));
    }

    public byte[] exportContacts() {
        final boolean[] selection = new boolean[size()];
        getSelectedFlags(selection);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(10240);

        try {
            // write contacts
            for (int i = 0; i < selection.length; i++) {
                if (selection[i]) {
                    doExport(bos, (PIMItem) pimItems.elementAt(i));
                }
            }
            return bos.toByteArray();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            midlet.displayError("Can't export contacts", e.getMessage());
        } catch (PIMException e) {
            e.printStackTrace();
            midlet.displayError("Can't export contacts", e.getMessage() + "\nReason: " + e.getReason());
        }
        return null;
    }

    void doExportInternal(final String url) {
        midlet.doInThread(new Runnable() {
            public void run() {
                logger.info(" ==> Saving contacts");

                FileConnection fc = null;
                OutputStream os = null;

                try {
                    setTitle("Contacts => mem");

                    String contactsFile = url + "contacts.vcf";
                    logger.info("  Saving contacts to " + url);

                    fc = (FileConnection) Connector.open(contactsFile);
                    if (!fc.exists()) {
                        logger.debug("  Creating file");
                        fc.create();
                    } else {
                        logger.debug("  File exists, truncating");
                        fc.truncate(0);
                    }
                    os = fc.openOutputStream();
                    os.write(exportContacts());

                    logger.info(" <== Contacts written");
                } catch (IOException e) {
                    e.printStackTrace();
                    midlet.displayError("Can't export contacts", e.getMessage());
                } finally {
                    if (os != null) {
                        try {
                            os.flush();
                            os.close();
                        } catch (IOException ignore) {
                        }
                    }
                    if (fc != null) {
                        try {
                            fc.close();
                        } catch (IOException ignore) {
                        }
                    }

                    Display.getDisplay(midlet).setCurrent(new Alert("Exported to", url, null, AlertType.INFO));
                }
            }
        });
    }

    private void doExport(OutputStream os, PIMItem pimItem) throws PIMException, UnsupportedEncodingException {
        PIM pim = PIM.getInstance();
        String fmt = pim.supportedSerialFormats(PIM.CONTACT_LIST)[0];
        pim.toSerialFormat(pimItem, os, "UTF-8", fmt);
    }

    public PIMItem[] getSelectedContacts() {
        final boolean[] selection = new boolean[size()];
        int numSel = getSelectedFlags(selection);

        PIMItem[] res = new PIMItem[numSel];
        int i = 0;
        for (int j = 0; j < selection.length; j++) {
            if (selection[j]) {
                res[i++] = (PIMItem) pimItems.elementAt(j);
            }
        }
        return res;
    }
}
