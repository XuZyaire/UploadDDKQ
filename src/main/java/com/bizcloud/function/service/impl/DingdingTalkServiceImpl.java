package com.bizcloud.function.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bizcloud.function.service.DingdingTalkService;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DingdingTalkServiceImpl implements DingdingTalkService {
    /**
     * 获取access_token
     * @param AppKey
     * @param AppSecret
     * @return
     * @throws ApiException
     */
    @Override
    public String getAccessToken(String AppKey, String AppSecret) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(AppKey);
        request.setAppsecret(AppSecret);
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        //将获取到的结果转json
        JSONObject jsonBody = JSON.parseObject(response.getBody());
        //获取access_token 并 返回值
        return jsonBody.get("access_token").toString();
    }

    /**
     * 获取考勤组
     * @param userid
     * @param access_token
     * @return
     * @throws ApiException
     */
    @Override
    public String getUserGroup(String userid, String access_token) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getusergroup");
        OapiAttendanceGetusergroupRequest request = new OapiAttendanceGetusergroupRequest();
        request.setUserid(userid);
        OapiAttendanceGetusergroupResponse response = client.execute(request,access_token);
        //获取考勤组结果
        String result = JSON.parseObject(response.getBody()).get("result").toString();
        //获取考勤组名称
        String name = JSON.parseObject(result).get("name").toString();
        return name;
    }

    /**
     * 获取userid
     * @param access_token
     * @param mobile
     * @return
     */
    @Override
    public String getUserId(String access_token, String mobile) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getbymobile");
        OapiV2UserUseridGetbymobileRequest request = new OapiV2UserUseridGetbymobileRequest();
        request.setMobile(mobile);
        request.setHttpMethod("POST");
        OapiV2UserUseridGetbymobileResponse response = client.execute(request,access_token);
        JSONObject jsonBody = JSON.parseObject(response.getBody());
        //获取结果
        JSONObject jsonResult = JSONObject.parseObject(jsonBody.get("result").toString());
        //获取userid
        String userid = jsonResult.get("userid").toString();
        return userid;
    }

    /**
     * 获取日统计自定义列
     * @param access_token
     * @return
     * @throws ApiException
     */
    @Override
    public Map<String, String> getAttcolums(String access_token) throws ApiException {
        //创建一个存放name、id的集合
        Map<String,String> column_list = new HashMap<>();

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getattcolumns");
        OapiAttendanceGetattcolumnsRequest request = new OapiAttendanceGetattcolumnsRequest();
        OapiAttendanceGetattcolumnsResponse response = client.execute(request,access_token);
        JSONObject jsonBody = JSONObject.parseObject(response.getBody());
        //获取结果
        JSONObject json_result = JSON.parseObject(jsonBody.get("result").toString());
        //获取列
        List cloumns = JSON.parseArray(json_result.get("columns").toString());
        for (Object cloumn: cloumns) {
            JSONObject json_cloumn = JSON.parseObject(cloumn.toString());
            //根据名称获取id
            String name = json_cloumn.get("name").toString();
            switch(name){
                case "上班1打卡时间":
                case "下班1打卡时间":
                case "上班2打卡时间":
                case "下班2打卡时间":
                case "上班3打卡时间":
                case "下班3打卡时间":
                case "工作时长":
                case "工作日加班":
                case "休息日加班":
                case "节假日加班":
                    column_list.put(json_cloumn.get("id").toString(),name);
                    break;
            }
        }
        return column_list;
    }

    /**
     * 获取自定义列的列值
     * @param attcolums
     * @param access_token
     * @param userid
     * @param lastDKDate
     * @return
     * @throws ParseException
     * @throws ApiException
     */
    @Override
    public String getColumnval(Map<String, String> attcolums, String access_token, String userid, String lastDKDate) throws ParseException, ApiException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //创建column_id_list
        String column_id = "";
        //赋值到column_id_list
        Set<String> strings = attcolums.keySet();
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            column_id = column_id + iterator.next() + ",";
        }
        String column_id_list = column_id.substring(0, column_id.length() - 1);
        //获取今天日期
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 0);
        String todate = sdf.format(now.getTime());
        Date to_Date = sdf.parse(todate);

        //获取今天前30天日期
        Date from_Date = new Date();
        //如果没有上次打卡日期
        if (lastDKDate.equals("")){
            Calendar before = Calendar.getInstance();
            before.add(Calendar.DAY_OF_MONTH, -30);
            String fromdate = sdf.format(before.getTime());
            from_Date = sdf.parse(fromdate);
        }else {
            //获取今天至上次打卡的时间天数
            String lastDKTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(lastDKDate));
            //获取今天日期
            Calendar today = Calendar.getInstance();
            today.add(Calendar.DAY_OF_MONTH, 0);
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            //间隔天数
            int betweenDays = (int)(today.getTime().getTime()-sdf.parse(lastDKTime).getTime()) / (1000 * 3600 * 24);
            if (betweenDays>=31){
                Calendar before = Calendar.getInstance();
                before.add(Calendar.DAY_OF_MONTH, -30);
                String fromdate = sdf.format(before.getTime());
                from_Date = sdf.parse(fromdate);
            }else {
                from_Date = sdf.parse(lastDKDate);
            }
        }

        //查询列值
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getcolumnval");
        OapiAttendanceGetcolumnvalRequest request = new OapiAttendanceGetcolumnvalRequest();
        //设置查询的列id
        request.setColumnIdList(column_id_list);
        //查询的用户id
        request.setUserid(userid);
        //查询的开始时间，30天前
        request.setFromDate(from_Date);
        //查询的截止时间，今天
        request.setToDate(to_Date);
        OapiAttendanceGetcolumnvalResponse response = client.execute(request, access_token);
        JSONObject jsonBody = JSON.parseObject(response.getBody());
        //获取结果
        JSONObject jsonResult = JSON.parseObject(jsonBody.get("result").toString());
        //创建存储的集合
        //上班1打卡时间
        String clockinTimeOne = "";
        //下班1打卡时间
        String clockoutTimeOne = "";
        //上班2打卡时间
        String clockinTimeTwo = "";
        //下班2打卡时间
        String clockoutTimeTwo = "";
        //上班3打卡时间
        String clockinTimeThree = "";
        //下班3打卡时间
        String clockoutTimeThree = "";
        //上班时长
        String workHours = "";
        //工作日加班时长
        String workovertimeHours = "";
        //休息日加班时长
        String dayoffWorkHours = "";
        //节假日加班时长
        String holidayWorkHours = "";
        //获取列值集合
        List list_column_vals = JSON.parseArray(jsonResult.get("column_vals").toString());
        //循环获取
        for (Object column_vals : list_column_vals) {
            String data = "";
            //获取id
            String column_vo = JSON.parseObject(column_vals.toString()).get("column_vo").toString();
            String id = JSON.parseObject(column_vo).get("id").toString();
            //根据id查询出name
            String name = attcolums.get(id);

            //获取value
            List values = JSON.parseArray(JSON.parseObject(column_vals.toString()).get("column_vals").toString());
            data = "{\"id\":\"" + id + "\"," +
                    "\"name\":\"" + name + "\"," +
                    "\"values\":" + values + "" +
                    "}";
            //根据name判断
            switch (name) {
                case "上班1打卡时间":
                    clockinTimeOne = data;
                    break;
                case "下班1打卡时间":
                    clockoutTimeOne = data;
                    break;
                case "上班2打卡时间":
                    clockinTimeTwo = data;
                    break;
                case "下班2打卡时间":
                    clockoutTimeTwo = data;
                    break;
                case "上班3打卡时间":
                    clockinTimeThree = data;
                    break;
                case "下班3打卡时间":
                    clockoutTimeThree = data;
                    break;
                case "工作时长":
                    workHours = data;
                    break;
                case "工作日加班":
                    workovertimeHours = data;
                    break;
                case "休息日加班":
                    dayoffWorkHours = data;
                    break;
                case "节假日加班":
                    holidayWorkHours = data;
                    break;
            }
        }
        //创建一个存储加班时长的列值json集合
        List overTimeHours_list = new ArrayList();
        //分别获取三个加班的，value集合
        List workovertimeHours_list = JSON.parseArray(JSON.parseObject(workovertimeHours).get("values").toString());
        List dayoffWorkHours_list = JSON.parseArray(JSON.parseObject(dayoffWorkHours).get("values").toString());
        List holidayWorkHours_list = JSON.parseArray(JSON.parseObject(holidayWorkHours).get("values").toString());
        for (int i = 0; i < workovertimeHours_list.size(); i++) {
            //创建一个json数据
            String workOverTimeHours_value = "";
            //加班时长
            BigDecimal sumWorkOverHours = new BigDecimal(0);
            //工作日加班时长
            String workovertime_value = JSON.parseObject(workovertimeHours_list.get(i).toString()).get("value").toString();
            BigDecimal workovertime_math = new BigDecimal(workovertime_value);
            //休息日加班时长
            String dayoffWorkHours_value = JSON.parseObject(dayoffWorkHours_list.get(i).toString()).get("value").toString();
            BigDecimal dayoffWorkHours_math = new BigDecimal(dayoffWorkHours_value);
            //节假日加班时长
            String holidayWorkHours_value = JSON.parseObject(holidayWorkHours_list.get(i).toString()).get("value").toString();
            BigDecimal holidayWorkHours_math = new BigDecimal(holidayWorkHours_value);
            //三种类型加班时长相加
            sumWorkOverHours.add(workovertime_math)
                    .add(dayoffWorkHours_math)
                    .add(holidayWorkHours_math);
            workOverTimeHours_value = "{\"date\":\""+JSON.parseObject(workovertimeHours_list.get(i).toString()).get("date").toString()+"\"," +
                    "\"value\":\""+sumWorkOverHours+"\"}";
            //存放到集合中
            overTimeHours_list.add(workOverTimeHours_value);
        }
        //加班时长json数据
        String workOverTimeHours = "{\"id\":\"workOverTimeHours\","+
                "\"name\":\"加班时长\","+
                "\"values\":"+overTimeHours_list+""+
                "}";
        //将各时间段打卡时间、工作时长、加班时长返回
        String reslut = "{\"clockinTimeOne\":"+clockinTimeOne+"," +
                "\"clockoutTimeOne\":"+clockoutTimeOne+"," +
                "\"clockinTimeTwo\":"+clockinTimeTwo+"," +
                "\"clockoutTimeTwo\":"+clockoutTimeTwo+"," +
                "\"clockinTimeThree\":"+clockinTimeThree+"," +
                "\"clockoutTimeThree\":"+clockoutTimeThree+"," +
                "\"workHours\":"+workHours+"," +
                "\"workOverTimeHours\":"+workOverTimeHours+"" +
                "}";
        return reslut;
    }

    /**
     * 获取打卡记录
     * @param userid
     * @param access_token
     * @param checkDateFrom
     * @param checkDateTo
     * @return
     */
    @Override
    public List getRecords(String userid, String access_token, String checkDateFrom, String checkDateTo) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/attendance/listRecord");
        OapiAttendanceListRecordRequest request = new OapiAttendanceListRecordRequest();
        request.setUserIds(Arrays.asList(userid));
        request.setCheckDateFrom(checkDateFrom);
        request.setCheckDateTo(checkDateTo);
        request.setHttpMethod("POST");
        OapiAttendanceListRecordResponse response = client.execute(request,access_token);
        //获取打卡详情
        List recordList = JSON.parseArray(JSON.parseObject(response.getBody()).get("recordresult").toString());
        return recordList;
    }

    /**
     * 根据时间段去获取打卡记录
     * @param userid
     * @param access_token
     * @param lastDKDate
     * @return
     * @throws ParseException
     * @throws ApiException
     */
    @Override
    public List getBetweenTimeRecords(String userid, String access_token, String lastDKDate) throws ParseException, ApiException {
        DingdingTalkService dingdingTalkService = new DingdingTalkServiceImpl();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //存放打卡记录的集合
        List records = new ArrayList();
        //判断lastDKDate是否为空
        if (lastDKDate.equals("")){
            //如果为空，默认查询前31天前的日期
            //设置当前日期
            int day = 0;
            for (int i=0;i<5;i++) {
                int beforeDay = 0;
                if (i != 4) {
                    //设置7天前日期
                    beforeDay = day - 7;
                } else {
                    //设置2天前日期
                    beforeDay = day - 2;
                }
                //获取打卡结束日期
                Calendar today = Calendar.getInstance();
                today.add(Calendar.DAY_OF_MONTH, day);
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                String checkDateTo = sdf.format(today.getTime());
                //获取打卡起始日期
                Calendar before = Calendar.getInstance();
                before.add(Calendar.DAY_OF_MONTH, beforeDay);
                before.set(Calendar.HOUR_OF_DAY, 0);
                before.set(Calendar.MINUTE, 0);
                before.set(Calendar.SECOND, 0);
                String checkDateFrom = sdf.format(before.getTime());
                //下一次循环当前日期为7天前一天
                day = day - 7;
                //打卡记录记录到集合中
                List records_list = dingdingTalkService.getRecords(userid, access_token, checkDateFrom, checkDateTo);
                for (Object record:records_list) {
                    records.add(record);
                }
            }
        }else {
            //上次打卡时间不为空
            //获取今天至上次打卡的时间天数
            String lastDKTime = sdf.format(new SimpleDateFormat("yyyy-MM-dd").parse(lastDKDate));
            //获取今天日期
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            //间隔天数
            int betweenDays = (int)(now.getTime().getTime()-sdf.parse(lastDKTime).getTime()) / (1000 * 3600 * 24);

            //获取今天的打卡日期
            if (betweenDays<=7 && betweenDays>0){
                //间隔日期小于等于7天 并且 大于等于0
                //获取距离今天间隔天数的日期
                Calendar before = Calendar.getInstance();
                before.add(Calendar.DAY_OF_MONTH, betweenDays);
                before.set(Calendar.HOUR_OF_DAY, 0);
                before.set(Calendar.MINUTE, 0);
                before.set(Calendar.SECOND, 0);
                String checkDateTo = sdf.format(before.getTime());
                //获取打卡记录
                records = dingdingTalkService.getRecords(userid, access_token, lastDKTime, checkDateTo);
            }else if (betweenDays>=31){
                //设置当前日期
                int day = 0;
                for (int i=0;i<5;i++) {
                    int beforeDay = 0;
                    if (i != 4) {
                        //设置7天前日期
                        beforeDay = day - 7;
                    } else {
                        //设置2天前日期
                        beforeDay = day - 2;
                    }
                    //获取打卡结束日期
                    Calendar today = Calendar.getInstance();
                    today.add(Calendar.DAY_OF_MONTH, day);
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    String checkDateTo = sdf.format(today.getTime());
                    //获取打卡起始日期
                    Calendar before = Calendar.getInstance();
                    before.add(Calendar.DAY_OF_MONTH, beforeDay);
                    before.set(Calendar.HOUR_OF_DAY, 0);
                    before.set(Calendar.MINUTE, 0);
                    before.set(Calendar.SECOND, 0);
                    String checkDateFrom = sdf.format(before.getTime());
                    //下一次循环当前日期为7天前一天
                    day = day - 7;
                    //打卡记录记录到集合中
                    List records_list = dingdingTalkService.getRecords(userid, access_token, checkDateFrom, checkDateTo);
                    for (Object record:records_list) {
                        records.add(record);
                    }
                }
            }else if (betweenDays>7){
                //间隔日期大于7天
                //设置打卡记录结束日期 今天
                int toDay = 0;
                //获取打卡结束日期
                //获取这段间隔日期内有几个7天
                int count = betweenDays/7;
                for (int i=0;i<count+1;i++){
                    //设置打卡记录起始日期
                    int fromDay = 0;
                    if (i != count){
                        //当i不等于最后一个7天时，每次起始日期-7
                        fromDay = toDay-7;
                    }else {
                        //当i等于最后一个7天时
                        //获取剩下的天数
                        int lastDay = betweenDays-count*7;
                        fromDay = toDay-lastDay;
                    }
                    Calendar to = Calendar.getInstance();
                    to.add(Calendar.DAY_OF_MONTH, toDay);
                    to.set(Calendar.HOUR_OF_DAY, 0);
                    to.set(Calendar.MINUTE, 0);
                    to.set(Calendar.SECOND, 0);
                    String checkDateTo = sdf.format(to.getTime());
                    //获取打卡开始日期
                    Calendar from = Calendar.getInstance();
                    from.add(Calendar.DAY_OF_MONTH, fromDay);
                    from.set(Calendar.HOUR_OF_DAY, 0);
                    from.set(Calendar.MINUTE, 0);
                    from.set(Calendar.SECOND, 0);
                    String checkDateFrom = sdf.format(from.getTime());

                    toDay = toDay -7;
                    //获取打卡记录
                    List record_list = dingdingTalkService.getRecords(userid, access_token, checkDateFrom, checkDateTo);
                    for (Object record: record_list) {
                        records.add(record);
                    }
                }
            }else {
                //等于0时
                return Collections.emptyList();
            }
        }
        return records;
    }
}
