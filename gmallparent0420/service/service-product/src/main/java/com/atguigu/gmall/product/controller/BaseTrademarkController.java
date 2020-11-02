package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * @author mqx
 * http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
 * @date 2020-9-29 14:04:05
 */
@Api(tags = "品牌管理")
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {

//    @Autowired
//    private ManageService manageService;

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     *
     * @param page 当前页
     * @param limit 每页显示的条数
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result getBaseTrademarkList(@PathVariable Long page,
                                       @PathVariable Long limit){

        Page<BaseTrademark> baseTrademarkPageParam = new Page<>(page, limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.selectPage(baseTrademarkPageParam);
        // 返回数据
        return Result.ok(baseTrademarkIPage);

    }

    // 添加 http://api.gmall.com/admin/product/baseTrademark/save
    @PostMapping("save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        // 调用方法。
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    // 修改 http://api.gmall.com/admin/product/baseTrademark/update
    @PutMapping("update")
    public Result updateBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    // 删除 http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    // @RequestMapping  -- @DeleteMapping
    @DeleteMapping("remove/{id}")
    public Result removeBaseTrademark(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    // 根据Id 回显品牌数据 http://api.gmall.com/admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTrademarkById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    // http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        // 调用服务层获取所有品牌数据
        return Result.ok(baseTrademarkService.list(null));
    }
}
