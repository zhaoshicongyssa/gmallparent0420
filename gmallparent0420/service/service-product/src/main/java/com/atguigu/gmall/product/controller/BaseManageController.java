package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2020-9-28 14:03:51
 */
// http://api.gmall.com/admin/product/getCategory1
@Api(tags = "商品的后台管理接口")
@RestController //@ResponseBody + @Controller
@RequestMapping("admin/product")
//@CrossOrigin
public class BaseManageController {

    // 调用service 层
    @Autowired
    private ManageService manageService;
    @GetMapping("getCategory1")
    public Result getCategory1(){
        // 获取数据
        List<BaseCategory1> category1List = manageService.getCategory1();
        // 返回数据
        return Result.ok(category1List);
    }

    // http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable(value = "category1Id") Long category1Id){
        // 获取数据
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);

        return Result.ok(category2List);
    }

    // http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        // 获取数据
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);

        return Result.ok(category3List);
    }
    // http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){

        // 获取数据
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);

        return  Result.ok(attrInfoList);
    }

    // http://api.gmall.com/admin/product/saveAttrInfo
    // 获取到前台传递过来的数据，使用@RequestBody ,将Json 对象转化为Java 对象
    // 保存，修改使用的是同一个控制器
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveBaseAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/getAttrValueList/{attrId}
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        // 调用方法
        // baseAttrInfo.getAttrValueList();
        // List<BaseAttrValue> baseAttrValueList = manageService.getAttrValueList(attrId);
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        // 返回数据
        return Result.ok(baseAttrInfo.getAttrValueList());
    }

}
