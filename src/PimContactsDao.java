import util.StringUtils;

import javax.microedition.pim.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * DAO for contacts.
 */
public class PimContactsDao {

    private PIM pim = PIM.getInstance();


    public static String getPimVersion() {
        return System.getProperty("microedition.pim.version");
    }

    public String getSupportedSerialFormats() {
        return StringUtils.join(pim.supportedSerialFormats(PIM.CONTACT_LIST), ", ");
    }

    /**
     * @return {@link Vector} of {@link javax.microedition.pim.PIMItem}s
     * @throws PIMException
     */
    public Vector loadAllContacts() throws PIMException {
        String[] lists = pim.listPIMLists(PIM.CONTACT_LIST);
        Vector res = new Vector(100);
        for (int i = 0; i < lists.length; i++) {
            PIMList pimList = pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY, lists[i]);
            Enumeration items = pimList.items();
            while (items.hasMoreElements()) {
                res.addElement(items.nextElement());
            }
        }
        return res;
    }

    public String getPimDisplayName(PIMItem pi) {
        try {
            return pi.getString(Contact.FORMATTED_NAME, 0);
        } catch (IndexOutOfBoundsException ex) {
            try {
                return pi.getString(Contact.NICKNAME, 0);
            } catch (IndexOutOfBoundsException ex1) {
                return "E:" + ex1.getMessage();
            }
        }
    }
}
