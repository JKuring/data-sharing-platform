import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by linghang.kong on 2017/5/11.
 */
public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        try {
            String[] a = new String[1];
            a[3]="a";
        }catch (Exception e){
//            logger.error("exception: {}",e.toString());
            logger.error("exception: {}",e.getMessage(),e.fillInStackTrace());
        }
    }
}
