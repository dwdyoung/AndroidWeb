package com.monday.androidweb.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller的基类
 * 如果对Controller的概念不熟悉，可以查阅 http://www.cnblogs.com/Eleanore/archive/2012/12/10/2788729.html
 * （随便百度【MVC Controller】可以搜索到）
 *
 *
 * 在本工程里，要遵循以下规则
 * 1、凡是Controller的类，都必须要继承BaseController
 * 2、Controller可以用 @Controller 标签，指派一个根路径，例如  @Controller("/api/meetings")
 * 3、Controller里的方法，可以用 @RequestMapping 指派一个子路径， 例如 @RequestMapping("/add")
 * 4、Controller里的方法的参数，可以用@PathVariable 和 @RequestParam ，分别表示，路径上的参数和传过来的参数
 *
 *
 * 关于该项目实际使用方法，可以查看 TestController
 *
 * Created by monday on 2017/8/10.
 */
public class BaseController {



    private List<String> methodNames = new ArrayList<>(); // 方法名合集   testJson1 testJson2 testView
    // 方法的value的uri，经过/分裂之后得出的数组再拼成数组 {roomName},test_json || test,test_json || {roomId},test_view
    private List<List<String>> methodStrsList = new ArrayList();

    private List<List<Class>> functionParamList = new ArrayList<>(); //装每个方法的参数类型

    private List<String> valueList = new ArrayList<>(); //每个方法中的@RequestMapping 的value 集合


    public List<List<String>> getMethodValueStrsList() {
        return methodStrsList;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public List<List<Class>> getFunctionParamList() {
        return functionParamList;
    }

    public List<String> getValueList() {
        return valueList;
    }
}
