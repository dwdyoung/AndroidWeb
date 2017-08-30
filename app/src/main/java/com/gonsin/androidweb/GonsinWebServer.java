package com.gonsin.androidweb;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

        //处理get post 请求
        if (false) {
            String uri = session.getUri();  //  /api/bear/test_json
            Log.d("rootpath", uri);
            Map map2 = session.getHeaders();
            String queryParams = session.getQueryParameterString(); //当请求为get的时候，不为空

            BaseController controller = controllerContainer.getControllerFromUri(uri);

            //切去前面的根路径
            uri = cutRootUri(uri);  // bear/test_json
            Log.d("rootpath", uri);
            //找到对应的control的方法结构体
            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
            //通过结构体获得方法
            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
            //解析uri中的参数
            Object[] args = controllerContainer.parseGetPostArgs(uri, queryParams, method);
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

        //处理上传文件
        if (false) {
            String uri = session.getUri();  //  /api/bear/test_json
            Map map2 = session.getHeaders();
            String queryParams = session.getQueryParameterString();

            BaseController controller = controllerContainer.getControllerFromUri(uri);

            //切去前面的根路径
            uri = cutRootUri(uri);  // bear/test_json
            Log.d("rootpath", uri);

            //找到对应的control的方法结构体
            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
            //通过结构体获得方法
            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];

            Method methodType = session.getMethod();
            //解析请求体
            Map<String, String> files = new HashMap<>();
            if (Method.POST.equals(methodType) || Method.PUT.equals(methodType)) {
                try {
                    session.parseBody(files);  //发来的文件存到缓存里，把文件放到Map<请求体的key,文件在缓存中的路径>
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ResponseException e) {
                    e.printStackTrace();
                }
            }
            Map<String, String> params = session.getParms();   //<请求体key,文件名称>
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String entryKey = entry.getKey();
                String tmpFilePath = files.get(entryKey); //缓存中的路径
                String fileName = entry.getValue(); //xxxx.JPG
                File tmpFile = new File(tmpFilePath);

                try {
                    Object[] args = controllerContainer.parseFileArgs(uri, tmpFile, method);
                    //执行
                    Object bean = method.invoke(controller, args);

                    String json = new Gson().toJson(bean);

                    return newFixedLengthResponse(json);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

        //处理显示网页View（未完成）
        if (false) {
            String uri = session.getUri();  //  /api/bear/test_json
            Log.d("rootpath", uri);
            Map map2 = session.getHeaders();
            String queryParams = session.getQueryParameterString(); //当请求为get的时候，不为空

            BaseController controller = controllerContainer.getControllerFromUri(uri);

            //切去前面的根路径
            uri = cutRootUri(uri);  // bear/test_json
            Log.d("rootpath", uri);
            //找到对应的control的方法结构体
            ControllerContainer.MethodMatch mostMatchMethod = controllerContainer.getMethodFromUri(controller, uri);  // 此时该controller的从baseController继承来的list会全部被填充,而且获得当前匹配到的methodMatch
            //通过结构体获得方法
            java.lang.reflect.Method[] methods = controller.getClass().getDeclaredMethods();
            java.lang.reflect.Method method = methods[mostMatchMethod.getId()];
            //解析uri中的参数
            Object[] args = controllerContainer.parseGetPostArgs(uri, queryParams, method);
            String functionName = mostMatchMethod.getMethodName();

//            VelocityEngine velocityEngine = new VelocityEngine();
////            velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "/storage/emulated/0/GonsinWebServer/test/views");
////            velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "/storage/emulated/0/GonsinWebServer/test/views");
//            velocityEngine.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,"/storage/emulated/0/GonsinWebServer/test/views");
////            velocityEngine.setProperty("/storage/emulated/0/GonsinWebServer/test/views.resource.loader.class", ClasspathResourceLoader.class.getName());
//            velocityEngine.init();
//            // 获取与方法名同名的模板文件
//            org.apache.velocity.Template template = velocityEngine.getTemplate(functionName+".vm");
//            // 设置变量
//            VelocityContext velocityContext = new VelocityContext();
//            velocityContext.put("user", "hjx");
//            StringWriter stringWriter = new StringWriter();
//            template.merge(velocityContext,stringWriter);

            // TODO 调用 TemplateEngine.process 就可以生成页面
            // TODO 模板语法可以百度 jangod语法

//            try {
//                Configuration configuration = FreeMakerConfiguration.getInstance();
//                FreeMakerBean bean = new FreeMakerBean("hjx");
//                Template template = configuration.getTemplate("testView.ftl");
//                Writer out = new OutputStreamWriter(System.out);
//                template.process(bean,out);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (TemplateException e) {
//                e.printStackTrace();
//            }

            return newFixedLengthResponse("asd");
        }

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
