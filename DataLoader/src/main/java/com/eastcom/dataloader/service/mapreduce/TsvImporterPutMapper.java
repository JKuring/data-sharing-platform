package com.eastcom.dataloader.service.mapreduce;

import com.eastcom.common.utils.filter.FilterImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@InterfaceAudience.Public
@InterfaceStability.Evolving
public class TsvImporterPutMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

    public static final String CF_DEFAULT = "cf";
    public static final String SEP_DEFAULT = "|";
    public static final boolean WRITE_TO_WAL_DEFAULT = false;

    private String columnFamily = CF_DEFAULT;
    private String charset;
    private String keyIndexs = "0";
    private String keyStrategies = "0";
    private String keyEncrypts = "0";
    private String valueEncrypts = "0";

    private boolean isWriteToWAL;

    private String separator = SEP_DEFAULT;
    private String rowKeySeparator = SEP_DEFAULT;
    private boolean skipBadLines;
    private Counter badLineCount;

    // filter
    public final static String EASTCOM_FILTER_PARAMS = "importtsv.filter.params";
    public final static String EASTCOM_FILTER_DEFINE = "importtsv.filter.define.class";
    private final static String EASTCOM_FILTER_SEPARATOR = ",";
    private final static String EASTCOM_FILTER_SEPARATOR_PARAMS = "\\|";
    private Map<Integer, FilterImpl<String>> filters = null;
    private String filterColumnNum;

    /**
     * 如果不添加参数不会实例化该过滤器，可以对多个列进行过滤，如果有自定义函数，请按照行对应过滤函数类，
     * 过滤函数需要实现{@link com.eastcom.common.utils.filter.Filter}接口的 doFilter function.
     *
     * @param context
     */
    private void initFilter(Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context context) {
        try {
            Configuration conf = context.getConfiguration();

            String filterContext = conf.get(EASTCOM_FILTER_PARAMS);
            if (filterContext.length() != 0) {
                filters = new HashMap<>();
                String defineFilterClass = conf.get(EASTCOM_FILTER_DEFINE);
                String[] tmpContext = filterContext.split(EASTCOM_FILTER_SEPARATOR);
                String[] tmpClass = defineFilterClass.split(EASTCOM_FILTER_SEPARATOR);
                int nClass = tmpClass.length;
                int n = 0;
                for (String string : tmpContext
                        ) {
                    String[] filterParams = string.split(EASTCOM_FILTER_SEPARATOR_PARAMS);
                    FilterImpl filter = new FilterImpl<String>(filterParams[1], filterParams[2]);
                    if (n < nClass && tmpClass[n].length() > 0) {
                        filter.setDefine_filter_class(tmpClass[n]);
                        n++;
                    }
                    this.filters.put(Integer.valueOf(filterParams[0]), filter);
                }
            }
        } catch (Exception e) {

        }
    }

    public boolean getSkipBadLines() {
        return this.skipBadLines;
    }

    public Counter getBadLineCount() {
        return this.badLineCount;
    }

    public void incrementBadLineCount(int count) {
        this.badLineCount.increment(count);
    }

    protected void setup(Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context context) {
        doSetup(context);
        initFilter(context);
    }

    protected void doSetup(Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context context) {
        Configuration conf = context.getConfiguration();

        this.separator = conf.get("importtsv.separator");
        if (this.separator == null)
            this.separator = "\t";
        else {
            this.separator = new String(Base64.decode(this.separator));
        }

        this.charset = conf.get("importtsv.charset", "gbk");

        this.columnFamily = conf.get("importtsv.columnFamily", CF_DEFAULT);

        /**
         * <pre>
         * ts: 当前时间
         * raw|00: 自定义字段值插入
         * 1: 指定原始记录对应位置字段值插入
         * </pre>
         */
        this.keyIndexs = conf.get("importtsv.rowkey.indexs", "0,ts");

        // 3,2,6,5
        // r,o,ss|yyyy-MM-dd HH:mm:ss|yyyyMMddHHmmss,ip2l
        /**
         * <pre>
         * r: 反转
         * o:
         * ss:时间字符串转化
         * ip2l: ip地址转long型
         * ip2lr: ip地址转long型反转
         * </pre>
         */
        this.keyStrategies = conf.get("importtsv.rowkey.strategies", "r,");

        // keynum,chars
        this.keyEncrypts = conf.get("importtsv.rowkey.encrypts", "keynum,chars");

        // chars
        this.valueEncrypts = conf.get("importtsv.value.encrypts", "chars");

        this.rowKeySeparator = conf.get("importtsv.rowkey.separator", SEP_DEFAULT);

        this.isWriteToWAL = conf.getBoolean("importtsv.wal.writetowal", WRITE_TO_WAL_DEFAULT);

        this.skipBadLines = context.getConfiguration().getBoolean("importtsv.skip.bad.lines", true);
        this.badLineCount = context.getCounter("ImportTsv", "Bad Lines");

        // 初始化
        columnK = keyIndexs.split(",");
        strategyK = keyStrategies.split(",");

        encryptKeys = keyEncrypts.split(",");
        encryptValues = valueEncrypts.split(",");

    }

    private String[] columnK;
    private String[] strategyK;

    private String[] encryptKeys;
    private String[] encryptValues;

    @SuppressWarnings("deprecation")
    public void map(LongWritable offset, Text value,
                    Mapper<LongWritable, Text, ImmutableBytesWritable, Put>.Context context) throws IOException {
        try {
            String val = value + "";

            // execute filter
            boolean filterStatus = false;
            if (this.filters != null && this.filters.size() > 0) {
                String[] tmp = val.split(separator);
                try {
                    for (Integer column : this.filters.keySet()
                        ) {
                        filterStatus |= this.filters.get(column).filter(tmp[column]);
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    filterStatus =true;
                    System.err.println("Bad line at offset: " + offset.get() + ":\n ArrayIndexOutOfBoundsException: " + e.getMessage());
                }
            }
            if (!filterStatus) {

                String row = buildRowkey(val);
                if (row == null)
                    throw new IllegalArgumentException("rowkey不能为空.");

                String nval = buildVal(val);

                ImmutableBytesWritable rowKey = new ImmutableBytesWritable(Bytes.toBytes(row));

                Put p = new Put(rowKey.copyBytes());
                //p.setWriteToWAL(isWriteToWAL);
                p.add(Bytes.toBytes(columnFamily), null, nval.getBytes());

                context.write(rowKey, p);
            }else {
                incrementBadLineCount(1);
            }
        } catch (IllegalArgumentException e) {
            if (this.skipBadLines) {
                System.err.println("Bad line at offset: " + offset.get() + ":\n" + e.getMessage());
                incrementBadLineCount(1);
                return;
            }
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            if (this.skipBadLines) {
                System.err.println("Bad line at offset: " + offset.get() + ":\n" + e.getMessage());
                incrementBadLineCount(1);
                return;
            }
            throw new IOException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private String buildVal(String val) {
        if (valueEncrypts == null || valueEncrypts.trim().length() == 0)
            return val;
        try {
            if (valueEncrypts.equalsIgnoreCase("chars"))
                return encryptChars(val, charset);

        } catch (Exception e) {
        }
        return val;
    }

    private String buildRowkey(String val) throws NoSuchAlgorithmException {

        String[] values = val.split(separator);
        if (values == null || values.length == 0)
            return null;

        if (columnK == null)
            return val;

        int clen = columnK.length;

        StringBuilder row = new StringBuilder(200);
        for (int i = 0; i < clen; i++) {
            String part = columnK[i];

            if (strategyK != null && strategyK.length > i && strategyK[i].equalsIgnoreCase("v")) {
                // part = part;
            } else {
                if (part.startsWith("raw")) {
                    String[] ps = part.split("\\|");
                    if (ps.length > 1)
                        part = ps[1];
                    else
                        part = "-";
                } else if (part.equalsIgnoreCase("ts")) {
                    part = System.currentTimeMillis() + "";
                } else {
                    Integer index = Integer.parseInt(part);
                    if (index >= values.length)
                        throw new IllegalArgumentException(
                                "指定列数[" + keyIndexs + "]大于值[" + val + "]列数,在分隔符[" + separator + "]");
                    part = values[index];
                }
            }

            if (part == null || part.length() == 0)
                return null;

            part = keyStrategyChange(part, i);
            part = keyEncryptChange(part, i);

            if (part == null)
                return null;

            row.append(part).append(rowKeySeparator);
        }
        row.append(md5Digest(val));
        return row.toString();

    }

    private String keyEncryptChange(String part, int i) {

        if (part == null)
            return "";

        if (i >= encryptKeys.length)
            return part;

        String sk = encryptKeys[i];

        String res = part;
        if (sk.trim().length() == 0) {
            //
        } else if (sk.equalsIgnoreCase("keynum")) {
            res = encryptKeyNums(part);
        } else if (sk.equalsIgnoreCase("chars")) {
            res = encryptChars(part, charset);
        } else {
            //
        }
        return res;
    }

    private String keyStrategyChange(String part, int i) {

        if (part == null)
            return null;

        if (i >= strategyK.length)
            return part;

        String strategy = strategyK[i];
        String res = part;

        if (part.trim().length() == 0) {
            //
        } else if (strategy.equalsIgnoreCase("r")) {
            res = new StringBuilder(part).reverse().toString();
        } else if (strategy.equalsIgnoreCase("o")) {
            //
        } else if (strategy.startsWith("ls") || strategy.startsWith("sl") || strategy.startsWith("ss")) {
            res = buildDateChange(strategy, part);
        } else if (strategy.equalsIgnoreCase("ip2l")) {
            res = ip2long(part);
        } else if (strategy.equalsIgnoreCase("ip2lr")) {
            res = ip2long(part);
            res = new StringBuilder(res).reverse().toString();
        } else {
            //
        }

        return res;
    }

    public String ip2long(String part) {
        if (part == null || part.trim().length() == 0)
            throw new IllegalArgumentException("IP地址不能为空.");

        long[] ip = new long[4];
        try {
            String[] ips = part.split("[.]");
            if (ips.length < 4)
                return "";
            ip[0] = Long.parseLong(ips[0]);
            ip[1] = Long.parseLong(ips[1]);
            ip[2] = Long.parseLong(ips[2]);
            ip[3] = Long.parseLong(ips[3]);
        } catch (Exception e) {
            return "";
        }
        return "" + ((ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]);
    }

    private String md5Digest(String str) throws NoSuchAlgorithmException {

        String temp;
        MessageDigest alg = MessageDigest.getInstance("MD5");
        alg.update(str.getBytes());
        byte[] digest = alg.digest();
        temp = byte2hex(digest);
        return temp;

    }

    private String byte2hex(byte[] bytes) {

        String hs = "";
        String stmp = "";
        for (int i = 0; i < bytes.length; i++) {
            stmp = (Integer.toHexString(bytes[i] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase();

    }

    private String buildDateChange(String strategy, String part) {

        String[] ss = strategy.split("\\|");
        if (ss.length < 2)
            return null;

        String type = ss[0];
        try {
            if (type.equalsIgnoreCase("sl")) {//
                String format = ss[1];

                SimpleDateFormat fromSdf = new SimpleDateFormat(format);
                Date date = fromSdf.parse(part);
                return date.getTime() + "";

            } else if (type.equalsIgnoreCase("ls")) {// 返回秒数
                String format = ss[1];

                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Long time = Long.parseLong(part);
                Date date = new Date(time);
                return sdf.format(date);

            } else if (type.equalsIgnoreCase("ss")) {// 返回时间字符串
                String from = ss[1];
                String to = ss[2];

                SimpleDateFormat fromSdf = new SimpleDateFormat(from);
                SimpleDateFormat toSdf = new SimpleDateFormat(to);

                Date date = fromSdf.parse(part);
                return toSdf.format(date);
            } else if (type.equalsIgnoreCase("lls")) {// ms返回时间字符串
                String format = ss[1];

                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Long time = Long.parseLong(part) / 1000;
                Date date = new Date(time);
                return sdf.format(date);
            }
        } catch (NumberFormatException e) {
        } catch (ParseException e) {
        }
        return part;
    }

    private char[] KEYNUM_ENCRYPT_MAP = "8374012596".toCharArray();
    private char[] KEYNUM_DECRYPT_MAP = "4561379208".toCharArray();

    public String encryptChars(String text, String charset) {
        try {
            byte[] bytes = null;
            if (charset == null || charset.trim().length() == 0)
                bytes = text.getBytes();
            else
                bytes = text.getBytes(charset);
            byte[] nbytes = encryptBytes(bytes);
            return new String(nbytes, charset);
        } catch (Exception e) {
        }
        return text;
    }

    public String decryptChars(String text, String charset) throws UnsupportedEncodingException {
        byte[] bytes = null;
        if (charset == null || charset.trim().length() == 0)
            bytes = text.getBytes();
        else
            bytes = text.getBytes(charset);
        byte[] nbytes = decryptBytes(bytes);
        return new String(nbytes, charset);
    }

    public String encryptKeyNums(String text) {

        if (text == null)
            return null;

        if (text.trim().length() == 0)
            return "";

        char[] cs = text.toCharArray();
        int len = cs.length;
        char[] ncs = new char[len];
        for (int i = 0; i < len; i++) {
            try {
                int t = Integer.parseInt("" + cs[i]);
                if (t < 0 || t >= 10)
                    ncs[i] = 0;
                else
                    ncs[i] = KEYNUM_ENCRYPT_MAP[t];
            } catch (NumberFormatException e) {
                ncs[i] = 0;
            }
        }
        return new String(ncs);
    }

    public String decryptKeyNums(String text) {
        if (text == null)
            return null;

        if (text.trim().length() == 0)
            return "";

        char[] cs = text.toCharArray();
        int len = cs.length;
        char[] ncs = new char[len];
        for (int i = 0; i < len; i++) {
            try {
                int t = Integer.parseInt("" + cs[i]);
                if (t < 0 || t >= 10)
                    ncs[i] = 0;
                else
                    ncs[i] = KEYNUM_DECRYPT_MAP[t];
            } catch (NumberFormatException e) {
                ncs[i] = 0;
            }
        }
        return new String(ncs);
    }

    public byte[] encryptBytes(byte[] bytes) {
        if (bytes == null)
            return null;
        int len = bytes.length;
        byte[] bs = new byte[len];
        for (int i = 0; i < len; i++) {
            bs[i] = (byte) (bytes[i] + 3);
        }
        return bs;
    }

    public byte[] decryptBytes(byte[] bytes) {
        if (bytes == null)
            return null;
        int len = bytes.length;
        byte[] bs = new byte[len];
        for (int i = 0; i < len; i++) {
            bs[i] = (byte) (bytes[i] - 3);
        }
        return bs;
    }

    //
    public boolean filter() {
        boolean result = false;


        return result;
    }

}
