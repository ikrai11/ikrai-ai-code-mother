package com.aistd.ikraiaicodemother.utils;

import java.util.HashSet;

/**
 * 查找1000个整数中唯一的重复数
 */
public class FindDuplicateNumber {

    public static void main(String[] args) {
        // 示例数组，包含1000个整数，范围[0,999]，有且只有2个相同的数
        int[] nums = generateTestArray();
        
        // 查找重复数
        int duplicate = findDuplicate(nums);
        
        // 输出结果
        System.out.println("重复的数是: " + duplicate);
    }

    /**
     * 生成测试数组
     * @return 包含1000个整数的数组，范围[0,999]，有且只有2个相同的数
     */
    private static int[] generateTestArray() {
        int[] nums = new int[1000];
        
        // 填充0-999的数
        for (int i = 0; i < 999; i++) {
            nums[i] = i;
        }
        
        // 最后一个位置重复一个随机数
        nums[999] = (int) (Math.random() * 1000);
        
        return nums;
    }

    /**
     * 查找重复数
     * @param nums 包含1000个整数的数组
     * @return 重复的数
     */
    public static int findDuplicate(int[] nums) {
        HashSet<Integer> set = new HashSet<>();
        
        for (int num : nums) {
            // 如果添加失败，说明该数已存在，即为重复数
            if (!set.add(num)) {
                return num;
            }
        }
        
        // 理论上不会执行到这里，因为题目保证有且只有2个相同的数
        return -1;
    }
}