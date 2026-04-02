package com.aistd.ikraiaicodeuser.utils;

import java.util.HashSet;

public class SimpleFindDuplicate {
    
    public static int findDuplicate(int[] nums) {
        HashSet<Integer> set = new HashSet<>();
        
        for (int num : nums) {
            if (!set.add(num)) {
                return num;
            }
        }
        
        return -1; // 理论上不会到达这里
    }
    
    public static void main(String[] args) {
        // 示例数组，包含重复数
        int[] nums = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 5}; // 重复数是5
        
        int duplicate = findDuplicate(nums);
        System.out.println("重复的数是: " + duplicate);
    }
}