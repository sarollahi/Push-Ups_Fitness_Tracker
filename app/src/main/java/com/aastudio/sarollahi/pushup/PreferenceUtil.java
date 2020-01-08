package com.aastudio.sarollahi.pushup;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Seyed Ahmad Sarollahi 01/06/2020.
 *
 * A base preference util class that handles retrieving both shared preference files and editors
 */

public class PreferenceUtil {

    /**
     * Returns the shared preference that matches the passed in fileKey
     * @param context
     * @param fileKey
     * @return shared preference of fileKey
     */
    public static SharedPreferences getSharedPref(Context context, String fileKey) {
        return context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }


    /**
     * Returns an editor of the shared preference that matches the fileKey
     * @param context
     * @param fileKey
     * @return ediotr of fileKey shared preference
     */
    public static SharedPreferences.Editor getEditor(Context context, String fileKey) {
        return getSharedPref(context, fileKey).edit();
    }
}
