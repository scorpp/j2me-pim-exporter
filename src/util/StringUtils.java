package util;

/**
 * Some {@link String} utility operations.
 * <br/>
 * String joining code taken from apache commons-lang.
 *
 * @author scorpp
 */
public class StringUtils {

    /**
     * The empty String <code>""</code>.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * <p>Joins the elements of the provided array into a single String
     * containing the provided list of elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by
     * empty strings.</p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array     the array of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null array input
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>Joins the elements of the provided array into a single String
     * containing the provided list of elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by
     * empty strings.</p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array      the array of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @param startIndex the first index to start joining from.  It is
     *                   an error to pass in an end index past the end of the array
     * @param endIndex   the index to stop joining from (exclusive). It is
     *                   an error to pass in an end index past the end of the array
     * @return the joined String, <code>null</code> if null array input
     */
    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return EMPTY;
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
                + separator.length());

        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }
}
