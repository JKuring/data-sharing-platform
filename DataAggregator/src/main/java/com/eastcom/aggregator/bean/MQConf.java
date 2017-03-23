package com.eastcom.aggregator.bean;

/**
 * Created by linghang.kong on 2017/3/23.
 */
public class MQConf {
    private String userName = "admin";
    private String password = "admin";
    private String host;
    private String port;
    private String exchange;
    private String routingKey;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String[] getParameters() {
        return new String[]{userName,password,host,port,exchange,routingKey};
    }
}
