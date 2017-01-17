package akhil.alltrans;

class StringEscape {
    public static String XMLUnescape(String s) {
        String retVal = s.replaceAll("&amp;", "&");
        retVal = retVal.replaceAll("&quot;", "\"");
        retVal = retVal.replaceAll("&apos;", "'");
        retVal = retVal.replaceAll("&lt;", "<");
        retVal = retVal.replaceAll("&gt;", ">");
        retVal = retVal.replaceAll("&#xD;", "\r");
        retVal = retVal.replaceAll("&#xA;", "\n");

        retVal = retVal.replaceAll("&amp;", "&");
        retVal = retVal.replaceAll("&quot;", "\"");
        retVal = retVal.replaceAll("&apos;", "'");
        retVal = retVal.replaceAll("&lt;", "<");
        retVal = retVal.replaceAll("&gt;", ">");
        retVal = retVal.replaceAll("&#xD;", "\r");
        retVal = retVal.replaceAll("&#xA;", "\n");
        return retVal;
    }
}
