package com.eastcom.common.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linghang.kong on 2017/3/21.
 */
public class SparkProperties {
    private String master;

    private String driver_memory;

    private String num_executors;

    private String executor_memory;

    private String executor_cores;

    private String queue;

    private String jars;

    private String clazz;

    private Map<String, String> paropertiesMap;

    private String jarName;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getDriver_memory() {
        return driver_memory;
    }

    public void setDriver_memory(String driver_memory) {
        this.driver_memory = driver_memory;
    }

    public String getNum_executors() {
        return num_executors;
    }

    public void setNum_executors(String num_executors) {
        this.num_executors = num_executors;
    }

    public String getExecutor_memory() {
        return executor_memory;
    }

    public void setExecutor_memory(String executor_memory) {
        this.executor_memory = executor_memory;
    }

    public String getExecutor_cores() {
        return executor_cores;
    }

    public void setExecutor_cores(String executor_cores) {
        this.executor_cores = executor_cores;
    }

    public String getJars() {
        return jars;
    }

    public void setJars(String jars) {
        this.jars = jars;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Map<String, String> getParopertiesMap() {
        return paropertiesMap;
    }

    public void setParopertiesMap(Map<String, String> paropertiesMap) {
        this.paropertiesMap = paropertiesMap;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String[] toParametersArray() {
        List<String> parameters = new ArrayList<>();
        parameters.add("--master");
        parameters.add(master);
        parameters.add("--driver-memory");
        parameters.add(driver_memory);
        parameters.add("--num-executors");
        parameters.add(num_executors);
        parameters.add("--executor-memory");
        parameters.add(executor_memory);
        parameters.add("--executor-cores");
        parameters.add(executor_cores);
        parameters.add("--queue");
        parameters.add(queue);
        parameters.add("--jars");
        parameters.add(jars);
        parameters.add("--class");
        parameters.add(clazz);

        for (String key : paropertiesMap.keySet()
                ) {
            parameters.add("--conf");
            parameters.add(key + "=" + paropertiesMap.get(key));
        }

//        parameters.add("--conf");
//        parameters.add("spark.driver.extraClassPath=/cmss/bch/bc1.3.4/hive/lib/ranger-hive-plugin-shim-0.5.3.jar:/cmss/bch/bc1.3.4/hive/lib/ranger-plugin-classloader-0.5.3.jar:/cmss/bch/bc1.3.4/hive/lib/ranger-hive-plugin-impl/*:/cmss/bch/bc1.3.4/hadoop/share/hadoop/common/lib/guava-11.0.2.jar:/home/ipms/hadoopclent/spark/lib/datanucleus*");
//        parameters.add("--conf");
//        parameters.add("spark.executor.extraClassPath=/cmss/bch/bc1.3.4/hive/lib/ranger-hive-plugin-shim-0.5.3.jar:/cmss/bch/bc1.3.4/hive/lib/ranger-plugin-classloader-0.5.3.jar:/cmss/bch/bc1.3.4/hive/lib/ranger-hive-plugin-impl/*:/cmss/bch/bc1.3.4/hadoop/share/hadoop/common/lib/guava-11.0.2.jar:/home/ipms/hadoopclent/spark/lib/datanucleus*");

        parameters.add(jarName);

        return parameters.toArray(new String[parameters.size()]);
    }
}
