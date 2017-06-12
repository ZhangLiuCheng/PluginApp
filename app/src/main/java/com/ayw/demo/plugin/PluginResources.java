package com.ayw.demo.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Method;

public class PluginResources extends Resources {

    public PluginResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
    }


    public static PluginResources getPluginResources(Resources res, AssetManager assetManager) {
        PluginResources pluginResources = new PluginResources(assetManager, res.getDisplayMetrics(),
                res.getConfiguration());
        return pluginResources;
    }

    public static AssetManager getPluginAssetManager(File akpFile) {
        try {
            Class<?> clazz = Class.forName("android.content.res.AssetManager");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("addAssetPath")) {
                    AssetManager assetManager = AssetManager.class.newInstance();
                    method.invoke(assetManager, akpFile.getAbsolutePath());
                    return assetManager;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
