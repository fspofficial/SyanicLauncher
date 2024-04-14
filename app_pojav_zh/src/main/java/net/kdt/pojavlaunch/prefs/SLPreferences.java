package net.kdt.pojavlaunch.prefs;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.SharedPreferences;

import net.kdt.pojavlaunch.PojavZHTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Set;

public class SLPreferences {
    public static Set<String> getPrefs(){
        return DEFAULT_PREF.getAll().keySet();
    }

    public static synchronized void save(String name) throws Exception {
        //创建一个新的属性文件用于存储所有的偏好设置
        File prefsFile = new File(PojavZHTools.DIR_PREFS, "/" + name + ".prefs");

        Properties properties = new Properties();
        for (String pref : getPrefs()) {
            properties.setProperty(pref, String.valueOf(DEFAULT_PREF.getAll().get(pref)));
        }

        properties.store(new FileWriter(prefsFile), "PojavZH Prefs");
    }

    public static synchronized void load(File prefsFile) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader(prefsFile));

        // 更新SharedPreferences
        SharedPreferences.Editor editor = DEFAULT_PREF.edit();
        for (String pref : properties.stringPropertyNames()) {
            editor.putString(pref, properties.getProperty(pref));
        }
        editor.apply();
    }
}
