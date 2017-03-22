package com.eastcom.aggregator.utils.parser;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by linghang.kong on 2017/3/8.
 */
@Component
public class JsonParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);

    private ObjectMapper objectMapper;

    public JsonParser() {
        objectMapper = new ObjectMapper();
    }

    public <T> T parseJsonToObject(byte[] message, Class<T> clasz) {
        try {
            return this.objectMapper.readValue(message, clasz);
        } catch (IOException e) {
            logger.error("Get error to parsed the json: {}, error: {}.", e.getMessage());
        }
        return null;
    }

    public String parseObjectToJson(Object object) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.error("Get error to parsed the object: {}, error: {}.", object.getClass().getName(), e.getMessage());
        }
        return null;
    }
}
