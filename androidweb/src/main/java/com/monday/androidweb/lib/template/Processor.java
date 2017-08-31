package com.monday.androidweb.lib.template;

import com.googlecode.openbeans.BeanInfo;
import com.googlecode.openbeans.IntrospectionException;
import com.googlecode.openbeans.Introspector;
import com.googlecode.openbeans.PropertyDescriptor;

import net.asfun.jangod.base.Application;
import net.asfun.jangod.base.Configuration;
import net.asfun.jangod.base.Context;
import net.asfun.jangod.base.ResourceManager;
import net.asfun.jangod.interpret.JangodInterpreter;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by monday on 2017/8/30.
 */

public class Processor {

    protected Context context;
    protected Application application;
    JangodInterpreter interpreter;

    public Processor(Application application) {
        this.application = application;
        context = new Context(application);
        interpreter = new JangodInterpreter(context);
    }

    public Configuration getConfiguration() {
        return context.getConfiguration();
    }

    public void setCommonBindings(Map<String, Object> bindings) {
        if (bindings == null) {
            application.getGlobalBindings().clear();
        } else {
            application.setGlobalBindings(bindings);
        }
    }

    public String render(String templateFile, Map<String, Object> bindings) throws IOException {
        return render(templateFile, bindings, context.getConfiguration().getEncoding());
    }

    public String render(String templateFile, Object bindings) throws IOException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        Map<String, Object> bindingsMap = object2Map(bindings);
        return render(templateFile, bindingsMap, context.getConfiguration().getEncoding());
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


    public String render(String templateFile, Map<String, Object> bindings, String encoding)
            throws IOException {
        if (bindings == null) {
            context.reset(Context.SCOPE_SESSION);
        } else {
            context.initBindings(bindings, Context.SCOPE_SESSION);
        }

        String fullName = ResourceManager.getFullName(templateFile, application.getConfiguration()
                .getWorkspace());
//        String fullName = ResourceManager.getFullName(templateFile, "/storage/emulated/0/GonsinWebServer/test/views/");
//        interpreter.setFile(fullName);
        try {
            interpreter.init();
            return interpreter.render(application.getParseResult(fullName, encoding));
        } catch (Exception e) {
            /* Modified by join */
            throw new IOException(e.getMessage());
            // throw new IOException(e.getMessage(), e.getCause());
        }
    }

    public void render(String templateFile, Map<String, Object> bindings, Writer out,
                       String encoding) throws IOException {
        out.write(render(templateFile, bindings, encoding));
    }

    public void render(String templateFile, Map<String, Object> bindings, Writer out)
            throws IOException {
        out.write(render(templateFile, bindings));
    }

    public void render(String templateFile, Object bindings, Writer out)
            throws IOException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        out.write(render(templateFile, bindings));
    }

}
