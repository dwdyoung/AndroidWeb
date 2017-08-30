package com.gonsin.androidweb.samples;

import com.gonsin.androidweb.BaseController;
import com.gonsin.androidweb.annotations.Controller;
import com.gonsin.androidweb.annotations.PathVariable;
import com.gonsin.androidweb.annotations.RequestMapping;
import com.gonsin.androidweb.annotations.RequestMethod;
import com.gonsin.androidweb.annotations.RequestParam;
import com.gonsin.androidweb.annotations.ReturnType;

import java.io.File;
import java.util.Map;

/**
 * 此类为测试类，也是项目的最终效果
 * Created by monday on 2017/8/10.
 */
//根路径为 /api
@Controller("/api")
public class TestController extends BaseController{

    /**
     * 范例 1:
     * 以下方法表示，当我访问    /api/{roomName}/test_json?info=xxx的时候，就会执行下面的方法
     * 此方法会返回TestBean的json字符串
     * 而且此方法只接受GET的访问
     * @param roomId
     * @param info
     * @return
     */
    @RequestMapping(value = "/{roomId}/test_json", method = RequestMethod.GET, returnType = ReturnType.JSON)
    public TestBean testJson1(@RequestParam("info")String info ,@PathVariable("roomId") int roomId){
        return new TestBean(info, roomId);
    }

    // 假设我的连接是  /test/test_json ?info=xxx
    @RequestMapping(value = "/test/test_json", method = RequestMethod.GET, returnType = ReturnType.JSON)
    public TestBean testJson2(@RequestParam("info")String info){
        return new TestBean(info, 1);
    }





    @RequestMapping(value = "/{roomId}/upload/test_file",method = RequestMethod.POST, returnType = ReturnType.VIEW)
    public TestBean testFile(@RequestParam("file")File file,@PathVariable("roomId") int roomId){
        return new TestBean("上传成功", roomId);
    }


    /**
     * 范例 2:
     * 以下方法表示，当我访问    /api/{roomId}/test_view  的时候，就会执行下面的方法
     * 返回  web项目的根目录/Controller名字/views/方法名.html
     * 也就是   {GonsinWebServer.rootPath}/test/views/testView.html
     * 此方法接受的Http方法不限
     *
     * @param roomId
     * @param info
     * @return
     */
    @RequestMapping(value = "/{roomId}/test_view", returnType = ReturnType.VIEW)
    public TestBean testView(@PathVariable("roomId") int roomId, @RequestParam("info")String info){
        return new TestBean(info, roomId);
    }

}
