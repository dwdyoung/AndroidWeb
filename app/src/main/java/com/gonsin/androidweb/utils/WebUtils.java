package com.gonsin.androidweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * 响应请求的用的小工具
 * 包含：
 * 判断请求是否是请求文件
 * 判断请求是否是页面（html页面）
 * 接收上传的文件
 * Created by monday on 2017/8/10.
 */

public class WebUtils {


    /**
     * 判断请求是否是下载文件
     * @param session
     * @return
     */
    public static boolean isRequestFile(NanoHTTPD.IHTTPSession session){
        String uri = session.getUri();          // 获取请求连接，范例：/xxx/yyy/zz.doc
        String ext = getExt(uri);
        if(ext == null){
            return false;
        }
        if(NanoHTTPD.mimeTypes().containsKey(ext)){
            return true;
        }
        return false;
    }



    /**
     * 如果请求是下载文件时，可以直接调用该方法，返回文件
     * @param mimeType
     * @param file
     * @return
     */
    public static NanoHTTPD.Response newFixedLengthResponse(String mimeType, File file){

        long leng = file.length();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, inputStream, leng);
    }


    /**
     * 获取URI的后缀
     * @param uri 例如 输入 "/xxx/yyy.doc"
     * @return 返回 doc
     */
    public static String getExt(String uri){
        if(uri == null){
            return "";
        }

        int point = uri.lastIndexOf('.');

        // 没有找点 .
        if(point == -1){
            return null;
        }
        return uri.substring(uri.lastIndexOf('.') + 1,
                uri.length());
    }

}
