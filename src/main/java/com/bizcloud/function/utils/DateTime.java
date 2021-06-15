package com.bizcloud.function.utils;

import com.alibaba.fastjson.JSON;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 根据values获取 打卡时间
 * 工具类
 */
public class DateTime {

    public String getDateTime(List values, int i) throws ParseException {
        SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf_dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String date = JSON.parseObject(values.get(i).toString()).get("date").toString();
        String time = JSON.parseObject(values.get(i).toString()).get("value").toString();

        String dateTime = "";
        //判断是否打卡
        if (!time.equals("")){
            //打卡时间
            dateTime = sdf_date.format(sdf_date.parse(date))+" "+time+":00";
            return sdf_dateTime.format(sdf_dateTime.parse(dateTime));
        }
        return dateTime;
    }
}
