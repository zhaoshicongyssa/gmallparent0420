package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2020-9-30 11:36:04
 */
@Api(tags = "sku后台数据接口")
@RestController
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;

    // http://api.gmall.com/admin/product/spuImageList/{spuId}
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImagelist(@PathVariable Long spuId){

        // 查询数据
        List<SpuImage> spuImageList = manageService.getSpuImagelist(spuId);
        // 数据返回
        return Result.ok(spuImageList);
    }

    // http://api.gmall.com/admin/product/spuSaleAttrList/{spuId}
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){
        // 调用服务层方法
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        // 返回数据
        return Result.ok(spuSaleAttrList);
    }

    // http://api.gmall.com/admin/product/saveSkuInfo
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        // 保存数据
        manageService.saveSkuInfo(skuInfo);
        // 返回结果
        return Result.ok();
    }

    // http://api.gmall.com/admin/product/list/{page}/{limit}
    @GetMapping("list/{page}/{limit}")
    public Result getSkuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit){

        Page<SkuInfo> skuInfoPageParam = new Page<>(page, limit);
        IPage<SkuInfo> pageSkuInfoList = manageService.getPage(skuInfoPageParam);
        // 返回数据
        return Result.ok(pageSkuInfoList);
    }

    // http://api.gmall.com/admin/product/onSale/{skuId}
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);

        return Result.ok();
    }

    // http://api.gmall.com/admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        manageService.cancelSale(skuId);

        return Result.ok();
    }
}
