package com.rocky.dida.common.utils;

import java.util.Random;

/**
 * Created by rocky on 18/2/24.
 */
public class RandomUtil {
    /**
     * 生成一个int随机数
     *
     * @param min
     * @param max
     * @return
     */
    public static int intRandom(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min can not bigger than max");
        if (max == min) return max;
        return new Random().nextInt(max - min + 1) + min;
    }
}
