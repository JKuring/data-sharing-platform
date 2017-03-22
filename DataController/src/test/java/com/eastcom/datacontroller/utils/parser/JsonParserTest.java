package com.eastcom.datacontroller.utils.parser;

import org.junit.Test;

/**
 * Created by linghang.kong on 2017/3/9.
 */
public class JsonParserTest {
    @Test
    public void parseJsonToObject() throws Exception {
        JsonParser jsonParser= new JsonParser();
        String message = "{\n" +
                "  \"name\": \"xdr_data:ps_gn_http_event\",\n" +
                "  \"time\":\"#today#\",\n" +
                "  \"columns\": [\"cf\"],\n" +
                "  \"version\": \"1\",\n" +
                "  \"compressionType\": \"snappy\",\n" +
                "  \"ttl\": \"5184000\",\n" +
                "  \"splitPolicy\": \"org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy\",\n" +
                "  \"spiltKeysFile\": \"classpath:hbase/table/splits.txt\",\n" +
                "  \"coprocessor\": \"org.apache.hadoop.hbase.coprocessor.AggregateImplementation\"\n" +
                "}";

//        HBaseEntityImpl hBaseEntity1 = new HBaseEntityImpl("xdr_data:ps_gn_http_event");
//        hBaseEntity1.setTime("#today#");
//        hBaseEntity1.setColumns(new String[]{"cf"});
//        hBaseEntity1.setVersion(1);
//        hBaseEntity1.setCompressionType("snappy");
//        hBaseEntity1.setTtl(5184000);
//        hBaseEntity1.setSplitPolicy("org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy");
//        hBaseEntity1.setSpiltKeysFile(new File("classpath:hbase/table/splits.txt"));
//        hBaseEntity1.setCoprocessor("org.apache.hadoop.hbase.coprocessor.AggregateImplementation");
//        String message1 = jsonParser.parseObjectToJson(hBaseEntity1);
//        System.out.println(message1);
//        HBaseEntityImpl hBaseEntity = jsonParser.parseJsonToObject(message.getBytes(), HBaseEntityImpl.class);
//        System.out.println(hBaseEntity.getTime());
    }

    @Test
    public void parseObjectToJson() throws Exception {

    }

}