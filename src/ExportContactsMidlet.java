import executor.ExecutorThread;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Midlet allowing one to export his contacts to a file on file system or remote Bluetooth device.
 */
public class ExportContactsMidlet extends MIDlet {

    private final ExecutorThread executorThread = new ExecutorThread();

    protected void startApp() throws MIDletStateChangeException {
        if (PimContactsDao.getPimVersion() == null) {
            Display.getDisplay(this).setCurrent(new Alert("PIM not available"));
            return;
        }

        executorThread.start();

        SelectContactsScreen scrn = new SelectContactsScreen(this, new PimContactsDao());
        Display.getDisplay(this).setCurrent(scrn);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean b) throws MIDletStateChangeException {
    }

    public void displayError(String title, String text) {
        Display.getDisplay(this).setCurrent(new Alert(title, text, null, AlertType.ERROR));
    }

    public void doInThread(Runnable r) {
        executorThread.doInThread(r);
    }
}
