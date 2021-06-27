package com.zebra.rfidreader.demo.common;

/**
 * Created by A24194 on 6/26/2018.
 */

public class asciitohex {

    public static String convert(String mData) {

        if (mData != null) {
            if (hextoascii.isDatainHex(mData))
                return mData;

            mData = mData.substring(1, mData.length() - 1);
            byte[] ch = mData.getBytes();
            StringBuilder builder = new StringBuilder();
            for (int c : ch) {
                builder.append(Integer.toHexString(c));
            }
            return builder.toString();
        }
        return mData;
    }
}
