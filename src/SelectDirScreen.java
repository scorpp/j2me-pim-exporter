import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.*;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Screen allowing to select a directory on phone file system.
 */
public class SelectDirScreen extends List implements CommandListener {

    private static final String PATH_SEPARATOR = System.getProperty("file.separator");
    private static final String PARENT_DIR = PATH_SEPARATOR + "..";

    private static final Command cmdSelect = new Command("", Command.ITEM, 1);
    private static final Command cmdChooseDir = new Command("Choose", Command.ITEM, 1);

    private final DirListing dirListing = new DirListing();

    private SelectContactsScreen screen;
    private ExportContactsMidlet midlet;

    private String currentDirUrl;

    public SelectDirScreen(ExportContactsMidlet midlet, SelectContactsScreen screen) {
        super("Directories", IMPLICIT);

        this.midlet = midlet;
        this.screen = screen;

        this.currentDirUrl = "file://";

        String galleryPath = System.getProperty("fileconn.dir.photos");
        if (null != galleryPath && !"".equals(galleryPath)) {
            currentDirUrl = galleryPath;
            listCurrentDir();
        } else {
            Enumeration e = FileSystemRegistry.listRoots();
            while (e.hasMoreElements()) {
                append('/' + e.nextElement().toString(), null);
            }
        }

        setSelectCommand(cmdSelect);
        addCommand(cmdChooseDir);
        setCommandListener(this);
    }

    protected void listCurrentDir() {
        deleteAll();
        System.out.println("List dir " + currentDirUrl);
        midlet.doInThread(dirListing);
    }

    public synchronized int append(String stringPart, Image imagePart) {
        return super.append(stringPart, imagePart);
    }

    protected String getDirToChangeTo() {
        String dir = getString(getSelectedIndex());
        if (PARENT_DIR.equals(dir)) {
            // remove trailing / if exists
            if (currentDirUrl.endsWith(PATH_SEPARATOR)) {
                currentDirUrl = currentDirUrl.substring(0, currentDirUrl.length() - PATH_SEPARATOR.length() - 1);
            }
            return currentDirUrl.substring(0, currentDirUrl.lastIndexOf('/', currentDirUrl.length() - 1) + 1);
        } else {
            return currentDirUrl + dir;
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (cmdSelect == command) {
            onSelect();
        } else if (cmdChooseDir == command) {
            onChooseDir();
        }
    }

    /**
     * Select the directory of your choice.
     */
    private void onChooseDir() {
        Display.getDisplay(midlet).setCurrent(screen);
        screen.doExportInternal(currentDirUrl);
    }

    /**
     * Change to selected directory.
     */
    private void onSelect() {
        currentDirUrl = getDirToChangeTo();
        listCurrentDir();
    }


    private class DirListing implements Runnable {
        public void run() {
            try {

                FileConnection fc = (FileConnection) Connector.open(currentDirUrl);
                currentDirUrl = fc.getURL();
                Enumeration e = fc.list();

                append(PARENT_DIR, null);
                while (e.hasMoreElements()) {
                    append(e.nextElement().toString(), null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                midlet.displayError("Can't list dir", e.getMessage());
            } catch (SecurityException e) {
                e.printStackTrace();
                midlet.displayError("No access to dir", e.getMessage());
            }
        }
    }
}
