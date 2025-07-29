package com.agent.brick.util;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 随机类 方法
 * @author cKnight
 * @since 2024/6/9
 */
public class RandomUtils {
    private static final String ALL_CHAR_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final String NUM = "0123456789";

    /**
     * 生成uuid
     *
     * @return
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }


    /**
     * 获取随机长度的串
     *
     * @param length
     * @return
     */
    public static String getStringNumRandom(int length) {
        //生成随机数字和字母,
        Random random = new Random();
        StringBuilder saltString = new StringBuilder(length);
        for (int i = 1; i <= length; ++i) {
            saltString.append(ALL_CHAR_NUM.charAt(random.nextInt(ALL_CHAR_NUM.length())));
        }
        return saltString.toString();
    }

    public static String getNumRandom(int length) {
        //生成随机数字和字母,
        Random random = new Random();
        StringBuilder saltString = new StringBuilder(length);
        for (int i = 1; i <= length; ++i) {
            saltString.append(NUM.charAt(random.nextInt(NUM.length())));
        }
        return saltString.toString();
    }

    /**
     * 随机获取指定数量的元素
     * @param list
     * @param num
     * @return
     * @param <E>
     */
    public static <E> List<E> getRandomElement(List<E> list,int num){
        Random random = new Random();
        List<E> randomList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int index = random.nextInt(list.size());
            E remove = list.remove(index);
            randomList.add(remove);
            if (CollectionUtils.isEmpty(list)){
                break;
            }
        }
        return randomList;
    }
}
