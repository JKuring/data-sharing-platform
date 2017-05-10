import com.eastcom.common.service.HttpRequestUtils;
import com.eastcom.dataloader.bean.JobEntityImpl;
import com.eastcom.dataloader.interfaces.dto.JobEntity;
import org.junit.Test;

import java.util.Set;

/**
 * Created by linghang.kong on 2017/3/29.
 */
public class ConfByHttp {


    @Test
    public void httpPost() throws Exception {

        JobEntity jobEntity = HttpRequestUtils.httpGet("http://192.168.1.27:8081/config/getByCiCode?ciCode=xdr_data:ps_gn_http_event_job",JobEntityImpl.class);

        System.out.println(jobEntity.getName());
        System.out.println(jobEntity.getDataPath());
        System.out.println(jobEntity.getGranularity());
        System.out.println(jobEntity.getDelay());

        for (String key: (Set<String>)jobEntity.getPropertiesMap().keySet()
             ) {
            System.out.println(jobEntity.getPropertiesMap().get(key));
        }

        System.out.println(jobEntity.getTableEntity());


    }
}
