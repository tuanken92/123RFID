package com.zebra.rfidreader.demo.common;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfidreader.demo.R;

/**
 * Custom Toast to show battery critical/low messages
 */
public class CustomToast {
    private final Context context;
    private final int toast_Layout;
    private final String message;

    /**
     * constructor of the class
     *
     * @param context      context to be used
     * @param toast_Layout layout for the custom toast
     * @param message      toast message
     */
    public CustomToast(Context context, int toast_Layout, String message) {
        this.context = context;
        this.toast_Layout = toast_Layout;
        this.message = message;
    }

    /**
     * method used to show custom toast
     */
    public void show() {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(toast_Layout,
                (ViewGroup) ((Activity) context).findViewById(R.id.toast_layout_root));
        ((TextView) layout.findViewById(R.id.text)).setText(message);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
