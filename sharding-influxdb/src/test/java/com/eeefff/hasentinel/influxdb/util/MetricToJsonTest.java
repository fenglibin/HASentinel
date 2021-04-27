package com.eeefff.hasentinel.influxdb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONObject;
import com.eeefff.hasentinel.influxdb.entity.Metric;

public class MetricToJsonTest {

	public static void main(String[] args) throws IOException {
		String content = read(new File("/home/fenglibin/temp/influxdb_metric.json"));
		Metric metric = JSONObject.parseObject(content, Metric.class);
		System.out.println(metric.getResults().get(0).getStatement_id());
	}
    /**
     * get file content by given file
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String read(File file) throws IOException {
        FileReader fr = new FileReader(file);
        return read(fr);
    }
    /**
     * get file content by InputStreamReader
     * 
     * @param fr
     * @return
     * @throws IOException
     */
    public static String read(InputStreamReader fr) throws IOException {
        String result = "";
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null) {
            result += line;
            line = br.readLine();
            if (line != null) {
                result += "\n";
            }
        }
        br.close();
        fr.close();
        return result;
    }
}
