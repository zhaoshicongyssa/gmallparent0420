package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mqx
 * @date 2020-10-10 15:35:43
 */
@Api(tags = "测试接口")
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping("testLock")
    public Result testLock(){
        // 调用服务层方法
        testService.testLock();

        return Result.ok();
    }

    // 读锁控制器
    @GetMapping("read")
    public Result read(){

        // 调用方法
        String msg = testService.readLock();
        return Result.ok(msg);
    }

    @GetMapping("write")
    public Result write(){
        // 调用方法
        String msg = testService.writeLock();

        return Result.ok(msg);
    }


}
