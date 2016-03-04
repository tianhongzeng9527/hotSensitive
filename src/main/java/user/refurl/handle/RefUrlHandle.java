package user.refurl.handle;

import info.zznet.udf.keywordreport.AnalysisKeyword;
import mmseg4j.analysis.ComplexAnalyzer;
import mmseg4j.analysis.TokenUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import tools.Constants;
import tools.SensitiveWord;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tian on 16-3-3.
 */
public class RefUrlHandle {
    private List<String> selectWord;
    private List<String> sensitiveWord;
    private static Logger logger = Logger.getLogger(RefUrlHandle.class);
    private String url;

    public RefUrlHandle(String url){
        this.url = url;
        selectWord = new ArrayList<>();
        sensitiveWord = new ArrayList<>();
    }

    public void init() throws Exception {
        generateSelectWord();
        generateSensitiveWord();
    }

    private void generateSelectWord() throws Exception {
        AnalysisKeyword analysisKeyword = new AnalysisKeyword();
        String content = analysisKeyword.exec(url);
        selectWord = toWords(content,  new ComplexAnalyzer());
    }

    private void generateSensitiveWord() throws IOException, InterruptedException {
        for(int i = 0; i < selectWord.size(); i++){
            for(Map.Entry<String, String> map: SensitiveWord.sensitiveWordCorrespondId.entrySet()){
                if(similarScore(selectWord.get(i),map.getKey()) > Constants.SIMILAR_SCORE_LINE){
                    sensitiveWord.add(map.getValue());
                }
            }
        }
    }

    private List<String> toWords(String txt, Analyzer analyzer) throws IOException {
        List<String> words = new ArrayList<String>();
        TokenStream ts = null;
        try {
            ts = analyzer.tokenStream("text", new StringReader(txt));
            ts.reset();
            for (PackedTokenAttributeImpl t = new PackedTokenAttributeImpl(); (t = TokenUtils.nextToken(ts, t)) != null; ) {
                words.add(t.toString());
            }
        } catch (IOException e) {
            logger.info(e.toString());
            throw e;
        } finally {
            if (ts != null) {
                try {
                    ts.close();
                } catch (IOException e) {
                    logger.info(e.toString());
                }
            }
        }
        return words;
    }

    public List<String> getSelectWord() throws Exception {
        return selectWord;
    }

    public List<String> getSensitiveWord(){
        return sensitiveWord;
    }

    private double similarScore(String word, String sensitiveWord) throws IOException, InterruptedException {
        double score;
        String address = String.format(Constants.WORDS_DISTANCE_URL, word, sensitiveWord);
        URL url = new URL(address);
        URLConnection urlcon = url.openConnection();
        InputStream is = urlcon.getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        String s = buffer.readLine();
        score = Double.valueOf(s);
        buffer.close();
        return score;
    }

    public static void main(String[] args) throws Exception {
        SensitiveWord sensitiveWord1 = new SensitiveWord("http://192.168.1.106:8080/public_behavior/api/sensitive.do");
        sensitiveWord1.init();
        RefUrlHandle refUrlHandle = new RefUrlHandle("https://www.baidu.com/s?wd=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsv_spt=1&rsv_iqid=0xf2f1eca000006b18&issp=1&f=3&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=0&rsv_t=f18bzUmGnb6KsjEl%2FIjENnZ7SEjLI4V1RAwFFtCD4e2503iS3GrqgnocPNiHORrB1TpA&oq=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsv_pq=b0890ba600009126&prefixsug=%E5%A6%82%E4%BD%95%E5%BC%BA%E5%A5%B8&rsp=1");
        refUrlHandle.init();
    }
}
