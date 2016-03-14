import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.CombineTextInputFormat;
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Constants;
import tools.SensitiveWord;
import user.refurl.handle.UserInformation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static String TIME_STRING = "time";
    private static String time = "time";
    private static String calculate_word_distance_url = "calculate_word_distance_url";
    private static String CALCULATE_WORD_DISTANCE_URL_STRING = "calculate_word_distance_url";
    private static String category_get_url = "category_get_url";
    private static String CATEGORY_GET_URL = "category_get_url";
    private static String sensitiveWord_get_url = "sensitiveWord_get_url";
    private static String SENSITIVE_WORD_GET_URL = "sensitiveWord_get_url";

    public static class Map extends MapReduceBase implements
            Mapper<LongWritable, Text, Text, IntWritable> {

        private final static IntWritable zero = new IntWritable(0);

        public void configure(JobConf job) {
            time = job.get(TIME_STRING);
            Constants.TIME = time;
            Constants.WORDS_DISTANCE_URL = "http://" + job.get(CALCULATE_WORD_DISTANCE_URL_STRING) + "/zz_nlp/wordsDistance?word1=%s&word2=%s";
//            category_get_url = job.get(CATEGORY_GET_URL);
            sensitiveWord_get_url = "http://" + job.get(SENSITIVE_WORD_GET_URL) + "/public_behavior/api/sensitive.do";
//            Constants.WORDS_DISTANCE_URL = "http://192.168.1.106:8080/zz_nlp/wordsDistance?word1=%s&word2=%s";
//            category_get_url = "http://192.168.1.106:8080/public_behavior/api/behavior.do";
//            sensitiveWord_get_url = "http://192.168.1.106:8080/public_behavior/api/sensitive.do";
        }

        private void handlerRightTimeInformation(UserInformation userInformation, String line, OutputCollector<Text, IntWritable> output) throws IOException {
            if (userInformation.isRightQueryAndSelectUrl()) {
                if (!userInformation.isNormalMessage) {
                    output.collect(new Text(Constants.WRONG_FORMAT + line), zero);
                    return;
                } else {
                    List<String> sensitiveId = userInformation.getSensitiveId();
                    if (sensitiveId != null && sensitiveId.size() > 0) {
                        for (String s : sensitiveId) {
                            output.collect(new Text(userInformation.getWhoWhenUrl() + s), new IntWritable(1));
                        }
                    }
                    List<String> hotWord = userInformation.getSelectWord();
                    if (hotWord != null && hotWord.size() > 0) {
                        for (String s : hotWord) {
                            output.collect(new Text(userInformation.getWhoWhen() + s), new IntWritable(1));
                        }
                    }
                }
            }
        }

        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
            String line = value.toString();
            if (SensitiveWord.sensitiveWordCorrespondId == null) {
                SensitiveWord sensitiveWord = new SensitiveWord(sensitiveWord_get_url);
                sensitiveWord.init();
            }
            if (line.indexOf(time) == 0) {
                UserInformation userInformation;
                try {
                    userInformation = new UserInformation(line);
                } catch (Exception e) {
                    output.collect(new Text(Constants.PROGRAM_EXCEPTION + line), zero);
                    return;
                }
                handlerRightTimeInformation(userInformation, line, output);
            }
        }
    }

    static class MyMultipleOutputFormat extends MultipleTextOutputFormat<Text, IntWritable> {
        @Override
        protected String generateFileNameForKeyValue(Text key, IntWritable value, String name) {
            String[] split = key.toString().trim().split(Constants.SEPARATOR);
            if (split.length == Constants.SENSITIVE_WORD_LENGTH) {
                return "sensitive" + name;
            }
            if (split.length == Constants.HOT_WORD_LENGTH) {
                return "hotWord" + name;
            }
            return "noise" + name;
        }
    }

    public static class Reduce extends MapReduceBase implements
            Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {
            String[] splits = key.toString().trim().split(Constants.SEPARATOR);
            if (splits.length == Constants.HOT_WORD_LENGTH) {
                int sum = 0;
                while (values.hasNext()) {
                    sum += values.next().get();
                    logger.info(key.toString());
                }
                output.collect(key, new IntWritable(sum));
            } else if (splits.length == Constants.SENSITIVE_WORD_LENGTH) {
                while (values.hasNext()) {
                    output.collect(key, values.next());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(Main.class);
        conf.setJobName("zznet");
        conf.setOutputKeyClass(NullWritable.class);
        conf.setOutputValueClass(Text.class);
        conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(IntWritable.class);
        conf.setMapperClass(Map.class);
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);
        conf.setInputFormat(CombineTextInputFormat.class);
//        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(MyMultipleOutputFormat.class);
        conf.set("mapred.textoutputformat.separator", Constants.SEPARATOR);
        conf.set("mapreduce.input.fileinputformat.input.dir.recursive", String.valueOf(true));
        String inputTime = args[0].substring(args[0].lastIndexOf("/") + 1, args[0].length());
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date dt = df.parse(inputTime);
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DAY_OF_MONTH, 1);
        Date dt1 = rightNow.getTime();
        File file = new File(args[0].substring(0, args[0].lastIndexOf("/")) + "/" + df.format(dt1) + "/" + "00");
        if (file.exists())
            FileInputFormat.setInputPaths(conf, new Path(args[0]), new Path(args[0].substring(0, args[0].lastIndexOf("/")) + "/" + df.format(dt1) + "/" + "00"));
        else
            FileInputFormat.setInputPaths(conf, new Path(args[0]));
        String s = df.format(dt);
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));
        conf.set(TIME_STRING, s);
        conf.set(CALCULATE_WORD_DISTANCE_URL_STRING, args[2]);
//        conf.set(CATEGORY_GET_URL, args[3]);
        conf.set(SENSITIVE_WORD_GET_URL, args[3]);
        JobClient.runJob(conf);
    }
}