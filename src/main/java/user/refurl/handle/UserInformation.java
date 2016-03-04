package user.refurl.handle;

import info.zznet.udf.keywordreport.AnalysisKeyword;
import tools.Constants;
import tools.SensitiveWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by tian on 16-3-3.
 */
public class UserInformation {

    private String reqTime;
    private String reqHour;
    private String reqURL;
    private String referURL;
    public boolean isNormalMessage;
    private String uid;
    private String uname;
    private List<String> sensitiveId;
    private boolean isRightTime;
    private RefUrlHandle refUrlHandle;
    private List<String> selectWord;

    public UserInformation(String userDataInformation) throws Exception {
        String[] splits = userDataInformation.trim().split(Constants.DECOLLATOR_FOR_HDFS_MESSAGE);
        isRightTime = true;
        isNormalMessage = true;
        initUserInformation(splits);
        if (isRightTime()) {
            refUrlHandle = new RefUrlHandle(referURL);
            refUrlHandle.init();
            selectWord = refUrlHandle.getSelectWord();
            sensitiveId = refUrlHandle.getSensitiveWord();
        }
    }

    public List<String> getSensitiveId(){
        return sensitiveId;
    }

    public List<String> getSelectWord(){
        return selectWord;
    }


    public boolean isRightTime() {
        return isRightTime;
    }

    private void initUserInformation(String[] splits) throws ParseException {
        isRightTime = true;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = df.parse(splits[0].substring(0, 8));
        reqTime = df2.format(dt);
        reqHour = splits[0].substring(8, 10);
        reqURL = splits[5];
        referURL = splits[8];
        int length = splits.length;
        if (length > 10)
            uid = splits[10];
        if (length > 11)
            if (length > 11)
                uname = splits[11];
        if (length <= 10) {
            uid = "110";
            uname = "uname";
        }
    }

    public String getWhoWhenUrl(){
        return uid+Constants.SEPARATOR+uname+Constants.SEPARATOR+reqURL+Constants.SEPARATOR;
    }

    public String getWhoWhen(){
        return uid+Constants.SEPARATOR+uname+Constants.SEPARATOR;
    }

    public static void main(String[] args) throws Exception {
        SensitiveWord sensitiveWord = new SensitiveWord("http://192.168.1.106:8080/public_behavior/api/sensitive.do");
        sensitiveWord.init();
        UserInformation userInformation = new UserInformation("20160405114421\t192.168.1.104\t55368\t123.125.115.99\t电视剧\thttp://kan.sogou.com/dianshiju/\tGET\tJava/1.7.0_79\thttps://www.baidu.com/s?wd=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsv_spt=1&rsv_iqid=0xf2f1eca000006b18&issp=1&f=3&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=0&rsv_t=f18bzUmGnb6KsjEl%2FIjENnZ7SEjLI4V1RAwFFtCD4e2503iS3GrqgnocPNiHORrB1TpA&oq=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsv_pq=b0890ba600009126&prefixsug=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsp=1");
        System.out.println(userInformation.getSelectWord());
        System.out.println(userInformation.getSensitiveId());
    }
}
