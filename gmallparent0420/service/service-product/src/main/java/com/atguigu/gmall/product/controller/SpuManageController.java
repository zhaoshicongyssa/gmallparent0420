package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2020-9-29 11:45:20
 */
@Api(tags = "商品的SPU管理接口")
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;

    // http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
    // 使用spring mvc 对象传值。如果传递的参数与实体类的属性名一致，那么就可以使用对象
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){

        Page<SpuInfo> spuInfoPageParam = new Page<>(page,limit);

        IPage<SpuInfo> spuInfoIPage = manageService.getSpuInfoPage(spuInfoPageParam, spuInfo);

        // 返回！
        return Result.ok(spuInfoIPage);
    }

    // http://api.gmall.com/admin/product/baseSaleAttrList
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){

        // 查询数据 调用服务层
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    // http://api.gmall.com/admin/product/saveSpuInfo
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        // 调用服务层保存方法
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }



}
