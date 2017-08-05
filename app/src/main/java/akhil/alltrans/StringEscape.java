/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import java.io.StringWriter;
import java.util.Locale;

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

    // Taken from Apache Commons Lang 2.6 at -
    // https://commons.apache.org/proper/commons-lang/javadocs/api-2.6/src-html/org/apache/commons/lang/StringEscapeUtils.html#line.146
    public static String javaScriptEscape(String str) {
        if (str == null) {
            return null;
        }
        StringWriter writer = new StringWriter(str.length() * 2);
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                writer.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                writer.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                writer.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b':
                        writer.write('\\');
                        writer.write('b');
                        break;
                    case '\n':
                        writer.write('\\');
                        writer.write('n');
                        break;
                    case '\t':
                        writer.write('\\');
                        writer.write('t');
                        break;
                    case '\f':
                        writer.write('\\');
                        writer.write('f');
                        break;
                    case '\r':
                        writer.write('\\');
                        writer.write('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            writer.write("\\u00" + hex(ch));
                        } else {
                            writer.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        writer.write('\\');
                        writer.write('\'');
                        break;
                    case '"':
                        writer.write('\\');
                        writer.write('"');
                        break;
                    case '\\':
                        writer.write('\\');
                        writer.write('\\');
                        break;
                    case '/':
                        writer.write('\\');
                        writer.write('/');
                        break;
                    default:
                        writer.write(ch);
                        break;
                }
            }
        }
        return writer.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
}
