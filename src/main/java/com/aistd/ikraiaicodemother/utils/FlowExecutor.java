package com.aistd.ikraiaicodemother.utils;

import java.util.concurrent.*;

/**
 * 流程执行器
 * 根据流程图实现同步和异步两种执行方式
 */
public class FlowExecutor {

    // 取消标志
    private volatile boolean cancelled = false;

    /**
     * 同步执行流程
     */
    public void executeSync() {
        System.out.println("开始同步执行流程");
        
        // 请求流程
        requestFlow();
        
        // 任务A（读操作）
        taskA();
        
        // 并行执行任务B和任务C
        // 同步方式下，这里实际上是串行执行
        taskB();
        taskC();
        
        // 任务C之后执行任务H
        taskH();
        
        // 检查任务B是否返回成功
        if (isTaskBSuccess()) {
            // 并行执行任务X和任务Y
            taskX();
            taskY();
        } else {
            // 执行任务P
            taskP();
        }
        
        // 执行任务W
        taskW();
        
        // 结束
        end();
        
        System.out.println("同步执行流程结束");
    }

    /**
     * 异步执行流程
     */
    public void executeAsync() {
        System.out.println("开始异步执行流程");
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // 请求流程
            requestFlow();
            
            // 任务A（读操作）
            taskA();
            
            // 并行执行任务B和任务C
            Future<?> futureB = executor.submit(this::taskB);
            Future<?> futureC = executor.submit(this::taskC);
            
            // 等待任务C完成，然后执行任务H
            try {
                futureC.get();
                taskH();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            
            // 等待任务B完成，然后判断是否成功
            try {
                futureB.get();
                if (isTaskBSuccess()) {
                    // 并行执行任务X和任务Y
                    Future<?> futureX = executor.submit(this::taskX);
                    Future<?> futureY = executor.submit(this::taskY);
                    
                    // 等待任一完成后执行任务W
                    futureX.get(1, TimeUnit.MINUTES);
                    // 或者 futureY.get(1, TimeUnit.MINUTES);
                } else {
                    // 执行任务P
                    taskP();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            
            // 执行任务W
            taskW();
            
            // 结束
            end();
            
            System.out.println("异步执行流程结束");
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 取消流程
     */
    public void cancel() {
        cancelled = true;
        System.out.println("流程已取消");
    }

    // 以下是各个任务的实现
    private void requestFlow() {
        if (cancelled) return;
        System.out.println("执行请求流程");
        sleep(100);
    }

    private void taskA() {
        if (cancelled) return;
        System.out.println("执行任务A（读操作）");
        sleep(200);
    }

    private void taskB() {
        if (cancelled) return;
        System.out.println("执行任务B（读操作）");
        sleep(300);
    }

    private void taskC() {
        if (cancelled) return;
        System.out.println("执行任务C（读操作）");
        sleep(250);
    }

    private void taskH() {
        if (cancelled) return;
        System.out.println("执行任务H（写操作）");
        sleep(150);
    }

    private boolean isTaskBSuccess() {
        // 模拟任务B是否成功，这里返回true
        return true;
    }

    private void taskX() {
        if (cancelled) return;
        System.out.println("执行任务X（读操作）");
        sleep(350);
    }

    private void taskY() {
        if (cancelled) return;
        System.out.println("执行任务Y（读操作）");
        sleep(400);
    }

    private void taskP() {
        if (cancelled) return;
        System.out.println("执行任务P（读操作）");
        sleep(200);
    }

    private void taskW() {
        if (cancelled) return;
        System.out.println("执行任务W（写操作）");
        sleep(150);
    }

    private void end() {
        if (cancelled) return;
        System.out.println("执行结束");
    }

    // 模拟任务执行时间
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 测试方法
    public static void main(String[] args) {
        FlowExecutor executor = new FlowExecutor();
        
        // 测试同步执行
        System.out.println("=== 测试同步执行 ===");
        executor.executeSync();
        
        System.out.println();
        
        // 测试异步执行
        System.out.println("=== 测试异步执行 ===");
        executor.executeAsync();
        
        // 测试取消功能（可以在另一个线程中调用）
        // new Thread(() -> {
        //     try {
        //         Thread.sleep(500);
        //         executor.cancel();
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        // }).start();
    }
}