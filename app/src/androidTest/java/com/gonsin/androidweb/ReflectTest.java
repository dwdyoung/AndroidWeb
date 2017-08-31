package com.gonsin.androidweb;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;


import com.gonsin.androidweb.samples.BeanInTestBean;
import com.gonsin.androidweb.samples.TestBean;
import com.gonsin.androidweb.samples.TestController;
import com.googlecode.openbeans.BeanInfo;
import com.googlecode.openbeans.IntrospectionException;
import com.googlecode.openbeans.Introspector;
import com.googlecode.openbeans.PropertyDescriptor;
import com.monday.androidweb.lib.annotations.Controller;
import com.monday.androidweb.lib.annotations.PathVariable;
import com.monday.androidweb.lib.annotations.RequestMapping;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Administrator on 2017/8/11.
 */
@RunWith(AndroidJUnit4.class)
public class ReflectTest {

    @Test
    public void test_0_reflect() {
        Class<TestController> testController = TestController.class;

        Controller con = testController.getAnnotation(Controller.class);
        Log.d("gws:", con.value());
        Field[] fields = testController.getFields();
        for (Field field : fields) {
            Log.i("yyd", "field.getName()--->" + field.getName());
        }

        try {
            Class[] classes = new Class[]{int.class, String.class};
            Method method = testController.getMethod("testJson", classes);//若有参数，则参数类的填写是必须的，不然会找不到
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            Log.d("gws:", requestMapping.value().toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test_10_reflect() {
        Class<TestController> controllerCls = TestController.class;
        // 获取类中@Controller注解
        Controller con = controllerCls.getAnnotation(Controller.class);
        String rootUrl = con.value(); //为 /api
        // TODO 获取这个controllerCls里的方法的
        java.lang.reflect.Method[] methodList = controllerCls.getDeclaredMethods();
        for (java.lang.reflect.Method method : methodList) {
            String Url = "";
            String childUrl = "";
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            childUrl = requestMapping.value();
            Url = rootUrl + childUrl;
            Log.d("wsg", Url);
        }
    }

    @Test
    public void test_20_reflect() {
        List<BaseController> baseControllerList = new ArrayList<>();
        baseControllerList.add(new TestController());

        for (int i = 0; i < baseControllerList.size(); i++) {
            BaseController controller = baseControllerList.get(i);
            //getclass  get的是BaseController 还是 自己的类？
            Controller con = controller.getClass().getAnnotation(Controller.class);
            String rootUrl = con.value();
            Log.d("rootUrl", rootUrl);
        }
    }


    @Test
    public void test_30_reflect() {
        List<BaseController> baseControllerList = new ArrayList<>();
        baseControllerList.add(new TestController());

        for (int i = 0; i < baseControllerList.size(); i++) {
            BaseController controller = baseControllerList.get(i);
            Method[] methods = controller.getClass().getDeclaredMethods();
            for (Method method : methods) {
                String methodName;
                String methodNameInValue;

                methodName = method.getName();
                Log.d("methodName", methodName);

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String value = requestMapping.value();
                int head = value.lastIndexOf("/");
                methodNameInValue = value.substring(head + 1);
                Log.d("methodNameInValue", methodNameInValue);
            }
        }
    }

    @Test
    public void test_40_reflect() {
        TestController controller = new TestController();

        List<String> valueList = new ArrayList<>(); //每个方法中的value 集合
        List<List<String>> strs2List = controller.getMethodValueStrsList(); //每个方法的value中的的子路径拆分集合  List<数组>

        // 在对应Controller中获取方法名与value 装入list
        List<String> methodNameList = controller.getMethodNames();
        Method[] methods = controller.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //获取方法名，装入list
            methodNameList.add(method.getName());
            //获取value，装入list
            String value = method.getAnnotation(RequestMapping.class).value();
            //去掉第一个斜杠
            value = value.substring(1);
            valueList.add(value);
        }
        //controller中的方法的value中的路径分裂成数组 给 strs2List
        for (int i = 0; i < valueList.size(); i++) {
            String[] s = valueList.get(i).split("/");
            List<String> stringList = Arrays.asList(s);
            strs2List.add(stringList);
        }

    }

    //切去前面的根路径
    @Test
    public void test_50_reflect() {
        String uri = "http://192.168.0.101:8083/api/{roomId}/test_json";

        cut(uri);
        Log.d("sad", uri);

    }

    void cut(String uri) {
        for (int i = 0; i < 4; i++) {
            int head = uri.indexOf("/");
            uri = uri.substring(head + 1);
        }
        Log.d("aaa", uri);
    }

    private MethodMatch mostMatchMethod;

    //解析出方法名
    @Test
    public void test_60_reflect() {
        TestController controller = new TestController();
        String uri = "14/test_view"; //模拟经裁剪后的uri

        String[] strsUri = uri.split("/"); //uri中的子路径拆分集合

        List<List<String>> strsFunctionValueList = controller.getMethodValueStrsList(); //每个方法的value中的的子路径拆分集合  List<数组>
        List<String> valueList = controller.getValueList(); //每个方法中的value 集合
        List<String> methodNameList = controller.getMethodNames(); //该controller实际方法名的集合
        List<List<Class>> functionParamList = controller.getFunctionParamList(); //

        strsFunctionValueList.clear();
        valueList.clear();
        methodNameList.clear();
        functionParamList.clear();

        String functionName = null; //需要返回的方法名

        // 装填valueList methodNameList functionParamList
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

        List<MethodMatch> methodMatchList = new ArrayList<>();
        // 分析入来的请求 strs中的每一项分别与strs2List中的每一项进行比对，若比对成功，返回方法名   i的意义为第几号方法开始比对
        for (int i = 0; i < strsFunctionValueList.size(); i++) {
            int match = 0;
            List<String> strsFunctionValueListItem = strsFunctionValueList.get(i);
            List<Class> functionParamListItem = functionParamList.get(i);

            //第一层筛选，拆分出来的数量相等
            if (strsUri.length == strsFunctionValueListItem.size()) {
                int size = strsUri.length;
                Boolean isOK = true; //假设匹配成功
                int signNum = -1; //通配符对应的参数号
                //第二层筛选，每个拆分项都相等  在某item中，每个拆分项按顺序进行比对   j的意义为正在比对的方法的第几号拆分项
                for (int j = 0; j < size; j++) {
                    String indexInStrUri = strsUri[j];
                    String indexInListItem = strsFunctionValueListItem.get(j);

                    // 先判断 通配符 匹配，如果通配符通过，匹配度 + 1
                    if (indexInListItem.startsWith("{")) {
                        signNum = signNum + 1;
                        String className = functionParamListItem.get(signNum).getName();

                        //去掉indexInListItem中{XXX}通配符左右的括号，得到里面的值 如{roomName} -》roomName
                        String tongpei = indexInListItem.substring(1, indexInListItem.length() - 1);
                        //TODO
                        String ClassName = findFunctionParamNameByName(methods[i], tongpei);

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
        Log.d("namename", mostMatchMethod.getMethodName());
    }

    // TODO 找到当前方法形参中包含该值的注释，如@PathVariable("roomName")
    //TODO 找到该注释对应的参数的类型名 className
    private String findFunctionParamNameByName(Method method, String argu) {

        return null;
    }

    /**
     * @param str
     * @return
     */
    private boolean judgedouble(String str) {
        Boolean isdouble = true;
        char[] charArrays = str.toCharArray();
        for (char c : charArrays) {
            // .  0-9
            if (c == 46 || c >= 48 && c <= 57) {
                continue;
            } else {
                isdouble = false;
                break;
            }
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


    @Test
    public void test_70_reflect() {

        Object object1 = true;
        Object object2 = 0;
        Object object3 = "12345a";
        Object object4 = 4.56;
        Object[] objects = new Object[]{object1,object2,object3,object4};
        for (int i = 0 ; i<objects.length ; i++){
            Log.d("name",objects[i].getClass().getName());
        }

    }


    @Test
    public void test_80_reflect() {
        TestController controller = new TestController();
        Method[] methods = controller.getClass().getDeclaredMethods();

        Method method = methods[1];
        String argu = "roomId";
        Class[] methodTypes = method.getParameterTypes();
        List<Class> methodTypesList = Arrays.asList(methodTypes);

        String ss = findFunctionParamClassNameByName(method,argu,methodTypesList);
        Log.d("ss", ss);
        Log.d("ss", ss);
    }

    private String findFunctionParamClassNameByName(Method method, String argu, List<Class> functionParamListItem) {
        //获取所有参数annotation,一个二维数组
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

    @Test
    public void test_90_reflect() {
        TestBean testBean = new TestBean("haha",11);
        testBean.setBeanInTestBean(new BeanInTestBean("hjx",1));
        try {
            Map<String, Object> map = object2Map(testBean);
            map.get("asd");
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> object2Map(Object bindings) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        Map<String, Object> data = new HashMap();
        //内省 获取对象的信息
        BeanInfo beanInfo = Introspector.getBeanInfo(bindings.getClass());
        //获取特性
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        //遍历特性
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors){
            //获取属性名
            String key = propertyDescriptor.getName();
            if (key.equals("class")){
                continue;
            }
            //获取对应属性的get方法
            Method getter = propertyDescriptor.getReadMethod();
            //执行方法
            Object value = getter!=null ? getter.invoke(bindings) : null;
            //判断是否是对象中的对象
            if (judgeIsObjectInObject(value)){
                //如果是，则递归，返回Map
                value = object2Map(value);
            }
            data.put(key,value);
        }

        return data;
    }

    private boolean judgeIsObjectInObject(Object object) {
        boolean IsObjectInObject = false;
        //排除所有的基本类型
        if (object instanceof Integer){
            return IsObjectInObject;
        }
        else if (object instanceof String){
            return IsObjectInObject;
        }
        else if (object instanceof Float){
            return IsObjectInObject;
        }
        else if (object instanceof Double){
            return IsObjectInObject;
        }else {
            IsObjectInObject = true;
        }
        return IsObjectInObject;
    }


    @Test
    public void test_100_reflect() {
        Object object  = new BeanInTestBean("aa",45);
        int a = 1;
        Object object1 = a;
        String name  = object1.getClass().getName();

        if (object instanceof Integer){

        }
        else if (object instanceof String){

        }
        else if (object instanceof Float){

        }
        else if (object instanceof Double){

        }else {
            Log.d("4564", "test_100_reflect: ");
        }

    }

}
