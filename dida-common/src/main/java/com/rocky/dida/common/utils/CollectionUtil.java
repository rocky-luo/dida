package com.rocky.dida.common.utils;

import java.util.Collection;

/**
 * Created by rocky on 18/2/24.
 */
public class CollectionUtil {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.size() == 0;
    }
}
