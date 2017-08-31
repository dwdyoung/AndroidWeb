package com.monday.androidweb.lib;

import android.os.Environment;
import android.util.Log;


import com.google.gson.Gson;
import com.googlecode.openbeans.IntrospectionException;
import com.monday.androidweb.lib.annotations.RequestMapping;
import com.monday.androidweb.lib.annotations.RequestMethod;
import com.monday.androidweb.lib.annotations.ReturnType;
import com.monday.androidweb.lib.template.TemplateEngine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * 自定义的android内置服务器
 * 模仿MVC的框架
 * 此处为WEB服务的总入口
 * <p>
 * GonsinWebServer 会贯穿整个框架，类似android里的Context
 * <p>
 * Created by monday on 2017/8/10.
 */
public class GonsinWebServer extends NanoHTTPD {

    private String rootPath;        // 项目的根目录

    private ControllerContainer controllerContainer;


    /**
     * TODO 注册Controller
     *
     * @param
     */
    public void registerController(List<BaseController> baseControllerList) throws Exception {
        controllerContainer = new ControllerContainer();
        controllerContainer.addController(baseControllerList);//此时controller 的 controllerMap rootUrls 已填充

        Log.d("rootpath", "registerController中");

    }

    public GonsinWebServer(int port, String rootPath) {
        super(port);
        this.rootPath = rootPath;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d("rootpath", "serve 接收HTTP Session");
//        // 判断请求是否是请求下载
//        if (WebUtils.isRequestFile(session)) {
//
//            // 获取后缀
//            String ext = WebUtils.getExt(session.getUri());
//
//            // 获取mimeType
//            String mimeType = mimeTypes().get(ext);
//
//            //获取文件
//            File file = new File(rootPath + session.getUri());
//
//            //完成地址的拼装，返回下载文件
//            return WebUtils.newFixedLengthResponse(mimeType, file);
//        }
        //TODO
        String uri = session.getUri();
        Log.d("rootpath", uri);
        //传来的 map 参数
        Map<String,String> paramsMap = session.getParms();
        //根据 uri 选出对应的 controller
        BaseController controller = controllerContainer.getControllerFromUri(uri);
        //切去前面的公共路径
        uri = cutRootUri(uri);
        Log.d("rootpath", uri);
        //匹配出最合适的方法
        ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
        //获得方法体
        java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
        java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
        //方法名
        String functionName = mostMatchMethod.getMethodName();
        Log.d("rootpath",functionName);
        //获得方法的返回类型 请求方式
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        ReturnType returnType = requestMapping.returnType();
        RequestMethod requestMethod[] = requestMapping.method();
        //查看网页
        if (returnType.equals(ReturnType.VIEW)){
            //解析uri中的参数
            Object[] args = null;
            try {
                args = controllerContainer.parseGetPostArgs(uri, paramsMap, method, session);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            //获得数据对象
            Object bean = null;
            try {
                bean = method.invoke(controller,args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            //构建数据Map
            Map map = new HashMap();
            map.put("user","hjx");
            TemplateEngine templateEngine = new TemplateEngine();
            String result = null;
            try {
                //在手机上找到与匹配方法名同名的网页模板文件
                String rootPath = Environment.getExternalStorageDirectory().getPath();
                String path = rootPath+"/GonsinWebServer/test/views/"+functionName+".tpl";
                Log.d("path",path);
                //数据注入模板
                result = templateEngine.process(path, bean);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(result);
        }
        //文件上传 get post
        if (returnType.equals(ReturnType.JSON)){
            Object[] args = null;
            try {
                //解析参数
                args = controllerContainer.parseGetPostArgs(uri, paramsMap, method, session);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            try {
                //执行
                Object bean = method.invoke(controller, args);

                String json = new Gson().toJson(bean);
                //显示输出
                return newFixedLengthResponse(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        //处理get post 请求
//        if (true) {
//            String uri = session.getUri();  //  /api/bear/test_json
//            Log.d("rootpath", uri);
//            Map map2 = session.getHeaders();
//            String queryParams = session.getQueryParameterString(); //当请求为get的时候，不为空
//            Map paramsMap = session.getParms();
//            BaseController controller = controllerContainer.getControllerFromUri(uri);
//
//            //切去前面的根路径
//            uri = cutRootUri(uri);  // bear/test_json
//            Log.d("rootpath", uri);
//            //找到对应的control的方法结构体
//            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
//            //通过结构体获得方法
//            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
//            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
//            //解析uri中的参数
////            Object[] args = controllerContainer.parseGetPostArgs(uri, queryParams, method);
//            Object[] args = controllerContainer.parseGetPostArgs(uri, paramsMap, method);
//            try {
//                //执行
//                Object bean = method.invoke(controller, args);
//
//                String json = new Gson().toJson(bean);
//                //显示输出
//                return newFixedLengthResponse(json);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        //处理上传文件
//        if (false) {
//            String uri = session.getUri();  //  /api/bear/test_json
//            Map map2 = session.getHeaders();
//            String queryParams = session.getQueryParameterString();
//
//            BaseController controller = controllerContainer.getControllerFromUri(uri);
//
//            //切去前面的根路径
//            uri = cutRootUri(uri);  // bear/test_json
//            Log.d("rootpath", uri);
//
//            //找到对应的control的方法结构体
//            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
//            //通过结构体获得方法
//            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
//            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
//
//            Method methodType = session.getMethod();
//            //解析请求体
//            Map<String, String> files = new HashMap<>(); //<请求体key,文件虚拟路径>
//            if (Method.POST.equals(methodType) || Method.PUT.equals(methodType)) {
//                try {
//                    session.parseBody(files);  //发来的文件存到缓存里，把文件放到Map<请求体的key,文件在缓存中的路径>
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ResponseException e) {
//                    e.printStackTrace();
//                }
//            }
//            Map<String, String> params = session.getParms();   //<请求体key,文件名称>
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                String entryKey = entry.getKey(); // 请求体中name="uploadname"
//                String tmpFilePath = files.get(entryKey); //缓存中的路径
//                String fileName = entry.getValue(); //xxxx.JPG
//                File tmpFile = new File(tmpFilePath);
//
//                try {
//                    //TODO 兼容上传文件的同时解析传来的Map
//                    Object[] args = controllerContainer.parseFileArgs(uri, tmpFile, method);
//                    //执行
//                    Object bean = method.invoke(controller, args);
//
//                    String json = new Gson().toJson(bean);
//
//                    return newFixedLengthResponse(json);
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }

//        //处理显示网页View（未完成）
//        if (false) {
//            String uri = session.getUri();  //  /api/bear/test_json
//            Log.d("rootpath", uri);
//            Map map2 = session.getHeaders();
//            String queryParams = session.getQueryParameterString(); //当请求为get的时候，不为空
//            Map paramsMap = session.getParms();
//
//            BaseController controller = controllerContainer.getControllerFromUri(uri);
//
//            //切去前面的根路径
//            uri = cutRootUri(uri);  // bear/test_json
//            Log.d("rootpath", uri);
//            //找到对应的control的方法结构体
//            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
//            //通过结构体获得方法
//            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
//            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
//            //解析uri中的参数
//            Object[] args = controllerContainer.parseGetPostArgs(uri, paramsMap, method);
//            String functionName = mostMatchMethod.getMethodName();
//            Log.d("rootpath",functionName);
//
//            Map map = new HashMap();
//            map.put("user","hjx");
//            TemplateEngine templateEngine = new TemplateEngine();
//            String result = null;
//            try {
//                String rootPath = Environment.getExternalStorageDirectory().getPath();
//                String path = rootPath+"/GonsinWebServer/test/views/"+functionName+".tpl";
//                Log.d("path",path);
//                result = templateEngine.process(path, map);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
////            try {
////                Configuration configuration = FreeMakerConfiguration.getInstance();
////                FreeMakerBean bean = new FreeMakerBean("hjx");
////                Template template = configuration.getTemplate("testView.ftl");
////                Writer out = new OutputStreamWriter(System.out);
////                template.process(bean,out);
////            } catch (IOException e) {
////                e.printStackTrace();
////            } catch (TemplateException e) {
////                e.printStackTrace();
////            }
//
//            return newFixedLengthResponse(result);
//        }
        return null;
    }


    private String cutRootUri(String uri) {
        // http://192.168.0.101:8083/api/{roomId}/test_json" ->{roomId}/test_json
        //要去掉多少条“/”就 i< 几
        for (int i = 0; i < 2; i++) {
            int head = uri.indexOf("/");
            uri = uri.substring(head + 1);
        }
        return uri;
    }

    public String getRootPath() {
        return rootPath;
    }

}
