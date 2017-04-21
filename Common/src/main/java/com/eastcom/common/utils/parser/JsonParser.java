package com.eastcom.common.utils.parser;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by linghang.kong on 2017/3/8.
 */
public class JsonParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseJsonToObject(byte[] message, Class<T> clazz) {
        try {
            return objectMapper.readValue(message, clazz);
        } catch (IOException e) {
            logger.error("Get error to parsed the json: {}, error: {}.", new String(message, 0, message.length), e.getMessage());
        }
        return null;
    }

    public static String parseObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.error("Get error to parsed the object: {}, error: {}.", object.getClass().getName(), e.getMessage());
        }
        return null;
    }
}
