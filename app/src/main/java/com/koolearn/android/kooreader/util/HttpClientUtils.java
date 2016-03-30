package com.koolearn.android.kooreader.util;

import com.koolearn.android.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by leixun on 2016/3/29.
 */
public class HttpClientUtils {

    /**
     * 将HTTP资源另存为文件
     *
     * @param destUrl String
     * @param fileName String
     * @throws Exception
     */
    public static String downloadAndSaveToFile(String destUrl, String fileName){
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        byte[] buf = new byte[1024];
        int size = 0;

        try{
            //建立链接
            url = new URL(destUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            //连接指定的资源
            httpUrl.connect();
            //获取网络输入流
            bis = new BufferedInputStream(httpUrl.getInputStream());
            //建立文件
            File file = new File(fileName);
            if(!file.exists()) {
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            fos = new FileOutputStream(fileName);

            LogUtil.i("正在获取链接[" + destUrl + "]的内容...\n将其保存为文件["
                    + fileName + "]");

            //保存文件
            while ((size = bis.read(buf)) != -1)
                fos.write(buf, 0, size);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }finally {
            try{
                fos.close();
                bis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        httpUrl.disconnect();
        return fileName;
    }
}
