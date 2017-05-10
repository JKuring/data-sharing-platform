package com.eastcom;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by linghang.kong on 2017/3/14.
 */
@SpringBootApplication
public class Loader {

    public static ApplicationContext applicationContext;

    public static void main(String[] args) throws InterruptedException {
        applicationContext = new ClassPathXmlApplicationContext("classpath:beans.xml");
        Thread.currentThread().join();
    }
}
