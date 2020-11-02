package com.atguigu.gmall.product.service;

/**
 * @author mqx
 * @date 2020-10-10 15:36:15
 */
public interface TestService {
    /**
     * 测试锁
     */
    void testLock();

    /**
     * 读数据
     * @return
     */
    String readLock();

    /**
     * 写数据
     * @return
     */
    String writeLock();

}
