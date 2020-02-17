package com.example.test_0217;

import android.util.Log;

import static com.example.test_0217.MainActivity.Data;
import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.id_byte;
import static com.example.test_0217.MainActivity.pdu_size;

public class function {
    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        //String[] strArray = new String[] {output.toString()};
        return output.toString();
    }

    static String byte2HexStr(byte[] b) {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
        }
        //Log.e(TAG,"length"+Integer.toString(b.length));
        //String[] strArray = new String[] {sb.toString().toUpperCase().trim()};
        //Log.e(TAG,"::"+sb.toString().trim());
        return sb.toString().trim();
        //trim:去掉前後空格 ； toUpperCase:變成大寫
    }

    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte intToByte(int x) {
        return (byte) x;
    }

}
