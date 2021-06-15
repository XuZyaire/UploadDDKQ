package com.bizcloud.function.service;

import com.taobao.api.ApiException;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface DingdingTalkService {
    //获取access_token
    String getAccessToken(String AppKey,String AppSecret) throws ApiException;
    //获取考勤组
    String getUserGroup(String userid,String access_token) throws ApiException;
    //获取userid
    String getUserId(String access_token,String mobile) throws ApiException;
    //获取日统计自定义列
    Map<String,String> getAttcolums(String access_token) throws ApiException;
    //获取日统计自定义列值
    String getColumnval(Map<String,String> attcolums,String access_token, String userid,String lastDKDate) throws ParseException, ApiException;
    //获取打卡记录
    List getRecords(String userid, String access_token,String checkDateFrom,String checkDateTo) throws ApiException;
    //获取时间间隔中的打卡记录
    List getBetweenTimeRecords(String userid, String access_token, String lastDKDate) throws ParseException, ApiException;
}
