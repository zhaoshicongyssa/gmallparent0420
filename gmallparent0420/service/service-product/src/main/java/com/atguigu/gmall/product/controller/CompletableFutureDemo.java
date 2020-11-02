package com.atguigu.gmall.product.controller;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author mqx
 * @date 2020-10-13 11:27:30
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                50,
                500,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000));

        // 并行化：
//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("hello !");
//        });
        // 第一个线程
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            return "hello";
        },threadPoolExecutor);

        // 第二个线程：futureB
        // Consumer
        CompletableFuture<Void> futureB = futureA.thenAcceptAsync((t) -> {
            // 睡眠1秒中
            delaySec(3);
            // 打印
            System.out.println(t+":\t futureB");
        },threadPoolExecutor);
        // 第二个线程：futureC
        CompletableFuture<Void> futureC = futureA.thenAcceptAsync((t) -> {
            // 睡眠3秒中
            delaySec(1);
            // 打印
            System.out.println(t+":\t futureC");
        },threadPoolExecutor);

        // 获取执行结果！
        futureB.get();
        futureC.get();

        // printCurrTime(s+" 第一个线程");


        // 测试CompletableFuture
        // Supplier
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(new Supplier<Integer>() {
//            @Override
//            public Integer get() {
//                // int i = 1/0;
//                return 1024;
//            }
//        }).thenApply(new Function<Integer, Integer>() {
//            @Override
//            public Integer apply(Integer integer) {
//                System.out.println("thenApply----integer:\t" + integer);
//                return integer*2;
//            }
//        }).whenCompleteAsync(new BiConsumer<Integer, Throwable>() {
//            @Override
//            public void accept(Integer integer, Throwable throwable) {
//                System.out.println("integer:\t"+integer);
//                System.out.println("throwable:\t"+throwable);
//            }
//        }).exceptionally(new Function<Throwable, Integer>() {
//            @Override
//            public Integer apply(Throwable throwable) {
//                System.out.println("throwable:\t"+throwable);
//                return 404;
//            }
//        });
//        // get() 只执行一次！放在最后！
//        System.out.println(integerCompletableFuture.get());

    }
    // 睡眠方法
    private static void delaySec(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
