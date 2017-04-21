package com.eastcom.common.service;


import com.eastcom.common.utils.parser.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by linghang.kong on 2017/3/29.
 * 使用jackson 可以把url的结果转换为自己想要的json对象
 * 其实如果没有映射对象应该转为JSONObject的，不过这个net.sf.json.JSONObject，maven repository中没有
 */

public class HttpRequestUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);

    /**
     * @param url
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T httpPost(String url, Class<T> clazz) {
        //post请求返回结果
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost method = new HttpPost(url);
        T object = null;
        try {
            if (url != null) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity("", "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    /**读取服务器返回过来的json字符串数据**/
                    str = EntityUtils.toString(result.getEntity());
                    /**把json字符串转换成json对象**/
                    if (!clazz.isInstance("")) {
                        object = JsonParser.parseJsonToObject(str.getBytes(), clazz);
                    }else {
                        return (T) str;
                    }
                } catch (Exception e) {
                    logger.error("post请求提交失败:" + url, e);
                }
            }
        } catch (IOException e) {
            logger.error("post请求提交失败:" + url, e);
        }
        return object;
    }


    /**
     * @param url
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T httpGet(String url, Class<T> clazz) {
        T object = null;
        //get请求返回结果
        try {
//            HttpPost method = new HttpPost(url);
            DefaultHttpClient client = new DefaultHttpClient();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

//            request.setHeader("content-type","application/json;charset=UTF-8");

//            StringEntity entity = new StringEntity("", "utf-8");
//            entity.setContentEncoding("UTF-8");
//            entity.setContentType("application/json");
//            method.setEntity(entity);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());
                /**把json字符串转换成json对象**/
                if (!clazz.isInstance("")) {
                    object = JsonParser.parseJsonToObject(strResult.getBytes(), clazz);
                } else {
                    return (T) strResult;
                }
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        }
        return object;
    }
}
