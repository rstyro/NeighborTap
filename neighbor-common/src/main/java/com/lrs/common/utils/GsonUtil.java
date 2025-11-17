package com.lrs.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GsonUtil {

    private static final Gson GSON = new Gson();

    // 配置更丰富的Gson实例
    private static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private GsonUtil() {}

    // 获取默认Gson实例
    public static Gson getGson() {
        return GSON;
    }

    // 获取格式化的Gson实例
    public static Gson getPrettyGson() {
        return GSON_PRETTY;
    }

    // 对象转JSON字符串
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    // 对象转格式化的JSON字符串
    public static String toPrettyJson(Object obj) {
        return GSON_PRETTY.toJson(obj);
    }

    // JSON字符串转对象
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    // JSON字符串转对象（支持泛型）
    public static <T> T fromJson(String json, java.lang.reflect.Type type) {
        return GSON.fromJson(json, type);
    }

}
