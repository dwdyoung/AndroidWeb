package com.monday.androidweb.lib;

import android.util.Log;


import com.monday.androidweb.lib.annotations.Controller;
import com.monday.androidweb.lib.annotations.PathVariable;
import com.monday.androidweb.lib.annotations.RequestMapping;
import com.monday.androidweb.lib.annotations.RequestParam;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Administrator on 2017/8/11.
 */

public class ControllerContainer {

    private List<String> rootUrls = new ArrayList<>();          // 所有Controller的根路径

    private Map<String, BaseController> controllerMap = new HashMap<>();  // 根路径 与 controller对象

    private MethodMatch mostMatchMethod;

    // 所有controller继承baseController,每个controller使用baseController的姓名，但是内容上是各自不同的内容
    public void addController(List<BaseController> baseControllerList) {
        for (int i = 0; i < baseControllerList.size(); i++) {
            // 处理根目录//
            // 获取装入的controller
            BaseController controller = baseControllerList.get(i);

            //getclass get的是自己的类的Annotation
            Controller con = controller.getClass().getAnnotation(Controller.class);
            // 得到根路径 /api
            String rootUrl = con.value();
            // 装入
            controllerMap.put(rootUrl, controller);
            rootUrls.add(rootUrl);

        }
        Log.d("rootpath", "addController完成");

    }

    /**
     * 通过分析uri，获取需要的Controller
     * 必须要addController后才能getControllerFromUri
     *
     * @param uri eg:http://192.168.1.101/api/12/test_json
     * @return
     */
    public BaseController getControllerFromUri(String uri) {
        BaseController baseController = null;
        //逐个逐个把根路径取出来
        for (String rootUrl : rootUrls) {
            //获取根路径在uri中的坐标
            int head = uri.indexOf(rootUrl);
            //如果是-1 则表示uri里面没有这个根路径，进行下一次循环
            if (head == -1) {
                continue;
            }
            //如果不是，则说明uri中有这个根路径,则本次循环中的rootUrl为uri中的根路径，根据此取得controller
            else {
                baseController = controllerMap.get(rootUrl);
                break;
            }
        }
        //返回
        return baseController;
    }


    /**
     *
     * @param controller 对应的controller
     * @param uri 传来的uri
     * @return  适配的方法
     */
    public MethodMatch getMethodFromUri(BaseController controller, String uri) {

        String[] strsUri = uri.split("/");//uri中的子路径拆分集合

        List<List<String>> strsFunctionValueList = controller.getMethodValueStrsList(); //每个方法的value中的的子路径拆分集合  List<数组>
        List<String> valueList = controller.getValueList(); //每个方法中的value 集合
        List<String> methodNameList = controller.getMethodNames(); //该controller实际方法名的集合
        List<List<Class>> functionParamList = controller.getFunctionParamList(); //每个方法的参数类型集合

        strsFunctionValueList.clear();
        valueList.clear();
        methodNameList.clear();
        functionParamList.clear();

        String functionName = null; //需要返回的方法名
        List<MethodMatch> methodMatchList = new ArrayList<>();//通过匹配的 方法名/匹配度对象 集合
        // 装填valueList methodNameList
        Method[] methods = controller.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //获取方法名，装入list
            methodNameList.add(method.getName());
            //获取value，装入list
            String value = method.getAnnotation(RequestMapping.class).value();
            //去掉第一个斜杠
            value = value.substring(1);
            valueList.add(value);
            //获取方法的参数类型
            Class[] methodTypes = method.getParameterTypes();
            List<Class> methodTypesList = Arrays.asList(methodTypes);
            functionParamList.add(methodTypesList);
        }
        //装填strsFunctionValue
        for (int i = 0; i < valueList.size(); i++) {
            String[] s = valueList.get(i).split("/");
            //数组转list
            List<String> stringList = Arrays.asList(s);
            strsFunctionValueList.add(stringList);
        }

        // 分析入来的请求 strs中的每一项分别与strs2List中的每一项进行比对，若比对成功，返回方法名
        for (int i = 0; i < strsFunctionValueList.size(); i++) {
            int match = 0;
            List<String> strsFunctionValueListItem = strsFunctionValueList.get(i); //取得一个方法value拆分项集合
            List<Class> functionParamListItem = functionParamList.get(i);  //取得一个方法内参数类型集合

            //第一层筛选，拆分出来的数量相等
            if (strsUri.length == strsFunctionValueListItem.size()) {
                int size = strsUri.length;
                Boolean isOK = true; //假设匹配成功
                //第二层筛选，每个拆分项都相等  在某item中，每个拆分项按顺序进行比对   j的意义为正在比对的方法的第几号拆分项
                for (int j = 0; j < size; j++) {
                    String indexInStrUri = strsUri[j];
                    String indexInListItem = strsFunctionValueListItem.get(j);

                    // 先判断 通配符 匹配，如果通配符通过，匹配度 + 1
                    if (indexInListItem.startsWith("{")) {
                        //去掉indexInListItem中{XXX}通配符左右的括号，得到里面的值 如{roomName} -》roomName
                        String tongpei = indexInListItem.substring(1, indexInListItem.length() - 1);
                        //TODO 在方法的形参中找到 该值对应参数的类型名
                        String className = findFunctionParamClassNameByName(methods[i], tongpei, functionParamListItem);

                        switch (className) {
                            case "java.lang.String":
                                if (judgeString(indexInStrUri)) {
                                    match += 1;
                                } else {
                                    isOK = false;
                                }
                                break;
                            case "int":
                                if (judgeInt(indexInStrUri)) {
                                    match += 1;
                                } else {
                                    isOK = false;
                                }
                                break;
                            case "java.lang.Integer":
                                //TODO 同 int 一样
                                break;
                            case "java.lang.Boolean":
                                //TODO 同boolean 一样
                                break;
                            case "boolean":
                                if (judgeboolean(indexInStrUri)) {
                                    match += 1;
                                } else {
                                    isOK = false;
                                }
                                break;
                            case "long":
                                //TODO
                                break;
                            case "double":
                                if (judgedouble(indexInStrUri)) {
                                    match += 1;
                                } else {
                                    isOK = false;
                                }
                                break;
                            case "java.lang.Double":
                                break;
                        }
                    } else if (indexInStrUri.equals(indexInListItem)) {
                        // 判断 连接的拆开项 是否完全相同，匹配度 + 2
                        //纯字符串的情况
                        match += 2;
                    } else {
                        isOK = false;
                    }

                }

                if (isOK) {
                    //拆分项数目相等，各项全相等，匹配成功
                    //根据 i 从list中获取真正方法名
                    functionName = methodNameList.get(i);
                    methodMatchList.add(new MethodMatch(functionName, match, i));
                }
            }
        }
        mostMatchMethod = getTopPriorityMethodMatch(methodMatchList);
        return mostMatchMethod;
    }


    /**
     *
     * @param uri 传来的uri
     * @param paramMap get 方式传来的参数
     * @param method 对应的方法
     * @return
     */
    public Object[] parseGetPostArgs(String uri, Map<String,String> paramMap, Method method, NanoHTTPD.IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {

        int paramsNum = method.getParameterTypes().length;
        //方法的参数集合
        Object[] ArgObjects = new Object[paramsNum];

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        //将value拆分
        String[] strsValueItem = requestMapping.value().substring(1).split("/");
        String[] strsUri = uri.split("/");

        //用这个方法的value 拆分项 与 uri 拆分项 比对， 提取参数
        for (int i = 0; i < strsValueItem.length; i++) {
            //拆分项相同的不是参数，不相同的时候才是参数
            String indexInStrUri = strsUri[i];
            String indexInValueListItem = strsValueItem[i];
            if (indexInStrUri.equals(indexInValueListItem)) {
                continue;
            } else {
                String arg = indexInStrUri;//实参
                Object object = null;
                //判断他是什么型，然后转化成相应的类型，添加到args[]数组里
                if (judgeboolean(arg)) {
                    if (arg == "true") {
                       object = true;
                    }
                    if (arg == "false") {
                        object = false;
                    }
                }
                if (judgedouble(arg)) {
                    object = Double.parseDouble(arg);
                }
                if (judgeInt(arg)) {
                    object = Integer.parseInt(arg);
                }
                if (judgeString(arg)) {
                    object = arg;
                }
                //eg：{roomID}-->roomID
                String StrValue = indexInValueListItem.substring(1,indexInValueListItem.length() - 1);
                //根据通配符的内容 获得方法中参数的位置
                int position = findParamsPosition(StrValue,method);
                ArgObjects[position] = object;
            }
        }
        if (paramMap != null) {
            //遍历找到RequestParam的位置，吧paramByGet放入ArgObjects数组对应位置
            Annotation[][] annotations2 = method.getParameterAnnotations();
            for (int i = 0; i < annotations2.length; i++) //此处 i 代表了当前遍历参数的位置
                for (int j = 0; j < annotations2[i].length; j++) {
                    Annotation annotation = annotations2[i][j];
                    RequestParam requestParam = null;
                    try {
                        //获得当前遍历到的RequestParam标签的value
                        //以value为map中的key,取得数据
                        requestParam = (RequestParam) annotation;
                        String key = requestParam.value();
                        Object value = paramMap.get(key); //如果是文件 则XXX.jpg
                        int position = i;
                        if (key.equals("file")){
//                            Log.d("key",value.toString());
                            Map<String, String> files = new HashMap<>();
                            //解析请求体，将文件存入虚拟内存，并把文件在虚拟内存的路径放入files
                            session.parseBody(files); //必须先解析body后拿paramMap才有文件在paramMap里面
                            paramMap = session.getParms();
                            String tempFileName = paramMap.get(key);
                            String tmpFilePath = files.get(key);
                            File tmpFile = new File(tmpFilePath);
                            ArgObjects[position] = tmpFile;
                            continue;
                        }
                        ArgObjects[position] = value;
                    }catch (ClassCastException e){
                        continue;
                    }
                }
        }
        return ArgObjects;
    }



    public Object[] parseFileArgs(String uri, File file, Method method){

        int paramsNum = method.getParameterTypes().length;
        //方法的参数集合
        Object[] ArgObjects = new Object[paramsNum];

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        //将value拆分
        String[] strsValueItem = requestMapping.value().substring(1).split("/");
        String[] strsUri = uri.split("/");

        //用这个方法的value 拆分项 与 uri 拆分项 比对， 提取参数
        for (int i = 0; i < strsValueItem.length; i++) {
            //拆分项相同的不是参数，不相同的时候才是参数
            String indexInStrUri = strsUri[i];
            String indexInValueListItem = strsValueItem[i];
            if (indexInStrUri.equals(indexInValueListItem)) {
                continue;
            } else {
                String arg = indexInStrUri;//实参
                Object object = null;
                //判断他是什么型，然后转化成相应的类型，添加到args[]数组里
                if (judgeboolean(arg)) {
                    if (arg == "true") {
                        object = true;
                    }
                    if (arg == "false") {
                        object = false;
                    }
                }
                if (judgedouble(arg)) {
                    object = Double.parseDouble(arg);
                }
                if (judgeInt(arg)) {
                    object = Integer.parseInt(arg);
                }
                if (judgeString(arg)) {
                    object = arg;
                }
                //eg：{roomID}-->roomID
                String StrValue = indexInValueListItem.substring(1,indexInValueListItem.length() - 1);
                //根据通配符的内容 获得方法中参数的位置
                int position = findParamsPosition(StrValue,method);
                ArgObjects[position] = object;
            }
        }
        if (file != null) {
            //遍历找到RequestParam的位置，吧file放入ArgObjects数组对应位置
            int position = -1;
            Annotation[][] annotations2 = method.getParameterAnnotations();
            for (int i = 0; i < annotations2.length; i++) //此处 i 代表了当前遍历参数的位置
                for (int j = 0; j < annotations2[i].length; j++) {
                    Annotation annotation = annotations2[i][j];
                    RequestParam requestParam = null;
                    try {
                        requestParam = (RequestParam) annotation;
                        if (requestParam.value().equals("file")){
                            position = i;
                            ArgObjects[position] = file;
                        }

                        break;
                    }catch (ClassCastException e){
                        continue;
                    }
                }
        }
        return ArgObjects;
    }


    /**
     *
     * @param argu 通配符内容
     * @param method 方法
     * @return 通配符内容对应参数的位置
     */
    private int findParamsPosition(String argu,Method method){

        Annotation[][] annotations2 = method.getParameterAnnotations();
        for (int i = 0; i < annotations2.length; i++) //此处 i 代表了当前遍历参数的位置
            for (int j = 0; j < annotations2[i].length; j++) {
                Annotation annotation = annotations2[i][j];
                PathVariable pathVariable = null;
                try {
                    pathVariable = (PathVariable) annotation;
                }catch (ClassCastException e){
                    continue;
                }
                if (pathVariable.value().equals(argu)){
                    return i;
                }
            }

        return -1;
    }

    /**
     * @param method                当前方法
     * @param argu                  {}里面的值
     * @param functionParamListItem 该方法中参数类型的集合
     * @return 通配符内容对应参数的类型
     */
    private String findFunctionParamClassNameByName(Method method, String argu, List<Class> functionParamListItem) {
        //获取所有参数annotation,一个二维数组
        //遍历方法形参中所有PathVariable的值，与argu比较
        Annotation[][] annotations2 = method.getParameterAnnotations();
        for (int i = 0; i < annotations2.length; i++) //此处 i 代表了当前遍历参数的位置
            for (int j = 0; j < annotations2[i].length; j++) {
                Annotation annotation = annotations2[i][j];
                PathVariable pathVariable = null;
                try {
                    pathVariable = (PathVariable) annotation;
                }catch (ClassCastException e){
                    continue;
                }
                Log.d("rootpath value",pathVariable.value());
                if (pathVariable.value().equals(argu)){
                    return functionParamListItem.get(i).getName();
                }
            }
        return null;
    }

    private boolean judgedouble(String str) {
        Boolean isdouble = true;
//        char[] charArrays = str.toCharArray();
//        for (char c : charArrays) {
//            // .  0-9
//            if (c >= 48 && c <= 57) {
//                continue;
//            } else {
//                isdouble = false;
//                break;
//            }
//        }
        if (str.indexOf(".") == -1){ //str中没有 “.”
            isdouble = false;
        }

        return isdouble;
    }

    private boolean judgeboolean(String str) {
        Boolean isboolean = false;
        if (str == "true" || str == "false") {
            isboolean = true;
        }
        return isboolean;
    }

    private boolean judgeInt(String str) {
        char[] charArrays = str.toCharArray();
        Boolean isInt = true;
        for (char c : charArrays) {
            if (c >= 48 && c <= 57) {
                continue;
            } else {
                isInt = false;
                break;
            }
        }
        return isInt;
    }

    private boolean judgeString(String str) {
        char[] charArrays = str.toCharArray();
        Boolean isString = true;
        for (char c : charArrays) {
            //A-Z   a-z  _  .
            if (c >= 65 && c <= 90 || c >= 97 && c <= 122 || c == 95 || c == 46) {
                continue;
            } else {
                isString = false;
                break;
            }
        }
        return isString;
    }

    //获取列表中优先度最高的那个MethodMatch
    private MethodMatch getTopPriorityMethodMatch(List<MethodMatch> methodMatchList) {
        int priority = -1;
        MethodMatch mostMatchMethod = null;
        for (MethodMatch methodMatch : methodMatchList) {
            //如果下一个优先级大于上一个，则保存下来
            if (methodMatch.getProprity() > priority) {
                priority = methodMatch.getProprity();
                mostMatchMethod = methodMatch;
            }
        }
        return mostMatchMethod;
    }

    public MethodMatch getMostMatchMethod(){
        return mostMatchMethod;
    }

    static class MethodMatch {

        private int id;

        private String methodName;

        private int proprity = 0;

        public MethodMatch(String methodName, int proprity, int id) {
            this.methodName = methodName;
            this.proprity = proprity;
            this.id = id;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getProprity() {
            return proprity;
        }

        public int getId() {
            return id;
        }
    }

}
