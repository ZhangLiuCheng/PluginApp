package com.ayw.demo.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    private ImageView girlIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        girlIv = (ImageView) findViewById(R.id.pluginImage);
    }

    // 将assets里面plugin1.apk拷贝的手机，实际项目应该是从远处服务器下载.
    public void loadPlugin(View view) {
        final String fileName = "plugin1.apk";
        final String packageName = "com.ayw.magic";
        String filePath = getFilesDir() + File.separator + fileName;

        final File apkFile = new File(filePath);
        if (apkFile.exists()) {
            // loadPluginImage1和loadPluginImage2都可以实现加载图片功能
            loadPluginImage1(apkFile, packageName);
//            loadPluginImage2(apkFile, fileName, packageName);
        } else {
            Toast.makeText(this, "下载插件中...", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copyFile(fileName, apkFile);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadPlugin(null);
                        }
                    });
                }
            }).start();
        }
    }

    private void copyFile(String fileName, File apkFile) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = getAssets().open(fileName);
            os = new FileOutputStream(apkFile);
            byte[] buff = new byte[1024];
            int len ;
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(is);
            closeStream(os);
        }
    }

    private void closeStream(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 方法1，直接通过Resouces去获取资源id
    private void loadPluginImage1(File apkFile, String packageName) {
        AssetManager assetManager = PluginResources.getPluginAssetManager(apkFile);
        PluginResources resources = PluginResources.getPluginResources(getResources(), assetManager);
        int girlId = resources.getIdentifier("girl", "drawable", packageName);
        girlIv.setImageDrawable(resources.getDrawable(girlId));
    }

    // 方法2，通过DexClassLoader加载R.class文件，然后通过反射获取到对应图片的id，在通过PluginResouces获取图片
    private void loadPluginImage2(File apkFile, String fileName, String packageName) {
        AssetManager assetManager = PluginResources.getPluginAssetManager(apkFile);
        PluginResources resources = PluginResources.getPluginResources(getResources(), assetManager);
        DexClassLoader classLoader = new DexClassLoader(apkFile.getAbsolutePath(),
                getDir(fileName, Context.MODE_PRIVATE).getAbsolutePath(), null, getClassLoader());
        try {
            Class<?> clazz = classLoader.loadClass(packageName + ".R$drawable");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("girl")) {
                    int id = field.getInt(R.drawable.class);
                    girlIv.setImageDrawable(resources.getDrawable(id));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
