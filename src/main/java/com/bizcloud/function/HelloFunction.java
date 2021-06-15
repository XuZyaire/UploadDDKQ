package com.bizcloud.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.bizcloud.function.service.DingdingTalkService;
import com.bizcloud.function.service.impl.DingdingTalkServiceImpl;
import com.bizcloud.function.utils.DateTime;

import com.bizcloud.ipaas.tb12db7b374924f7a800ee3bc0099cb2c.guojmrww.auth.extension.AuthConfig;
import com.bizcloud.ipaas.tb12db7b374924f7a800ee3bc0099cb2c.guojmrww.codegen.*;
import com.bizcloud.ipaas.tb12db7b374924f7a800ee3bc0099cb2c.guojmrww.model.*;
import com.taobao.api.ApiException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class HelloFunction {
    public String handle(Object param, Map<String, String> variables) throws ApiException, ParseException {
        System.out.println(param);
//        String InClass = JSONObject.toJSONString(param);
        String InClass = param.toString();
        JSONObject jsonParam = JSONObject.parseObject(InClass);

        //获取传入参数
        String jsonGRXX =JSONObject.toJSONString(jsonParam.get("GRXX"));
        //获取所属项目
        String project_id = "";
        if (JSON.parseObject(jsonGRXX).get("DQSZXMu")!=null){
            project_id= JSON.parseObject(jsonGRXX).get("DQSZXMu").toString();
        }
        //所属班组
        String team_id = "";
        if (JSON.parseObject(jsonGRXX).get("DQSSBZu")!=null){
            team_id = JSON.parseObject(jsonGRXX).get("DQSSBZu").toString();
        }
        //获取身份证
        String idCard = JSON.parseObject(jsonGRXX).get("IDCard").toString();
        //获取创建人id
        String modifyUserId = JSON.parseObject(jsonGRXX).get("modifyUserId").toString();
        //获取工人信息id
        String grxxid = JSON.parseObject(jsonGRXX).get("id").toString();
        //获取工人姓名
        String grxxname = JSON.parseObject(jsonGRXX).get("name").toString();
        //获取电话号码 json类型
        String lxdh = JSON.parseObject(jsonGRXX).get("LXDH").toString();
        JSONObject jsonLXDH = JSON.parseObject(JSONObject.toJSONString(JSON.parseObject(jsonGRXX).get("LXDH")));
        //json获取具体的电话号码
        String mobile = jsonLXDH.get("number").toString();
        //获取最近打卡日期
        String lastDKDate = "";
        if (JSON.parseObject(jsonGRXX).get("LastDKDate")!=null){
            lastDKDate = JSON.parseObject(jsonGRXX).get("LastDKDate").toString();
        }
        System.out.println(lastDKDate);

        /* 获取ACCESS 权限 */
        AuthConfig authConfig = new AuthConfig(variables.get("APAAS_ACCESS_KEY"), variables.get("APAAS_ACCESS_SECRET"));
        authConfig.setUserId(modifyUserId);
        DingdingTalkService dingdingTalkService = new DingdingTalkServiceImpl();
        DateTime dateTime = new DateTime();
        //转年月
        SimpleDateFormat sdf_yearmonth = new SimpleDateFormat("yyyy-MM");
        //转日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //转时间
        SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //api
        //班组月考勤api
        YkqhzApi bzykqApi = new YkqhzApi();
        //考勤单api
        GrkqdApi grkqdApi = new GrkqdApi();
        //工人月考勤api
        YkqhzrApi grykqApi = new YkqhzrApi();
        //工人考勤明细api
        GrkqmxApi grkqmxApi = new GrkqmxApi();
        //打卡记录api
        GrdkjluApi grdkApi = new GrdkjluApi();


        //获取钉钉考勤
        DdkqApi ddkqApi = new DdkqApi();
        DDKQDTOPageInfo pageInfo = new DDKQDTOPageInfo();
        //默认第一页
        pageInfo.set$PageNum(1);
        //容量为一
        pageInfo.set$PageSize(1);
        DataResponseDDKQDTOPageInfo ddkqPost = ddkqApi.findPageInfoDDKQUsingPOST(pageInfo);
        JSONObject jsonDDKQ = JSON.parseObject(JSONObject.toJSONString(ddkqPost.getData()));
        //获取集合数据
        List dataList = JSON.parseArray(jsonDDKQ.get("dataList").toString());
        //默认获取获取第一条数据
        //转json数据
        JSONObject jsonData = JSON.parseObject(JSONObject.toJSONString(dataList.get(0)));
        //获取AppKey
        String AppKey = jsonData.get("appKey").toString();
        //获取AppSecret
        String AppSecret = jsonData.get("appSecret").toString();
        //获取满勤工时
        String maxWorkHours = jsonData.get("mqGShi").toString();

        //获取access_token
        String access_token = dingdingTalkService.getAccessToken(AppKey, AppSecret);
        System.out.println(access_token);

        //获取钉钉用户userid
        String userid = dingdingTalkService.getUserId(access_token, mobile);
        System.out.println("钉钉用户userid："+userid);

        //根据userid获取所属考勤组名称
        String userGroup = dingdingTalkService.getUserGroup(userid, access_token);

        //获取30天打卡统计数据
        Map<String,String> attcolums = dingdingTalkService.getAttcolums(access_token);
        String columnval_result = dingdingTalkService.getColumnval(attcolums,access_token, userid,lastDKDate);

        //获取上班1打卡时间及列值
        String clockinTimeOne = JSON.parseObject(columnval_result).get("clockinTimeOne").toString();
        List clockinTimeOne_values = JSON.parseArray(JSON.parseObject(clockinTimeOne).get("values").toString());
        //获取下班1打卡时间及列值
        String clockoutTimeOne = JSON.parseObject(columnval_result).get("clockoutTimeOne").toString();
        List clockoutTimeOne_values = JSON.parseArray(JSON.parseObject(clockoutTimeOne).get("values").toString());
        //获取下班1打卡时间及列值
        String clockinTimeTwo = JSON.parseObject(columnval_result).get("clockinTimeTwo").toString();
        List clockinTimeTwo_values = JSON.parseArray(JSON.parseObject(clockinTimeTwo).get("values").toString());
        //获取下班1打卡时间及列值
        String clockoutTimeTwo = JSON.parseObject(columnval_result).get("clockoutTimeTwo").toString();
        List clockoutTimeTwo_values = JSON.parseArray(JSON.parseObject(clockoutTimeTwo).get("values").toString());
        //获取下班1打卡时间及列值
        String clockinTimeThree = JSON.parseObject(columnval_result).get("clockinTimeThree").toString();
        List clockinTimeThree_values = JSON.parseArray(JSON.parseObject(clockinTimeThree).get("values").toString());
        //获取下班1打卡时间及列值
        String clockoutTimeThree = JSON.parseObject(columnval_result).get("clockoutTimeThree").toString();
        List clockoutTimeThree_values = JSON.parseArray(JSON.parseObject(clockoutTimeThree).get("values").toString());

        //工作时长及列值
        String workHours = JSON.parseObject(columnval_result).get("workHours").toString();
        List workHours_values = JSON.parseArray(JSON.parseObject(workHours).get("values").toString());
        //加班时长及列值
        String workOverTimeHours = JSON.parseObject(columnval_result).get("workOverTimeHours").toString();
        List workOverTimeHours_values = JSON.parseArray(JSON.parseObject(workOverTimeHours).get("values").toString());


        //获取打卡记录       31天之内
        List records = dingdingTalkService.getBetweenTimeRecords(userid, access_token,lastDKDate);
        System.out.println("打卡记录条数"+records.size());

        //日统计时间
        String dayDateTime = "";
        //日期
        String dayDate = "";
        //获取年月
        String yearMonth = "";
        //获取日统计工作时长
        BigDecimal workHour = new BigDecimal(0);
        //获取日统计加班时长
        BigDecimal workOverHour = new BigDecimal(0);
        //获取上班1打卡时间
        String clockinTimeOne_dateTime = "";
        //获取下班1打卡时间
        String clockoutTimeOne_dateTime = "";
        //获取上班2打卡时间
        String clockinTimeTwo_dateTime = "";
        //获取下班2打卡时间
        String clockoutTimeTwo_dateTime = "";
        //获取上班3打卡时间
        String clockinTimeThree_dateTime = "";
        //获取下班3打卡时间
        String clockoutTimeThree_dateTime = "";
        //批量更新/新建班组月考勤
        List<SaveOrUpdateBatchYKQHZDTO> bzykqResponse_list = new ArrayList<>();
        //批量更新/新建考勤单
        List<SaveOrUpdateBatchGRKQDDTO> grkqdResponse_list = new ArrayList<>();
        //批量更新/新建工人月考勤
        List<SaveOrUpdateBatchYKQHZRDTO> grykqResponse_list = new ArrayList<>();
        //批量更新/新建考勤明细
        List<SaveOrUpdateBatchGRKQMXDTO> grkqmxResponse_list = new ArrayList<>();
        //批量更新/新建打卡记录
        List<SaveOrUpdateBatchGRDKJLuDTO> grdkResponse_list = new ArrayList<>();


        //判断是否存在打卡记录
        if(records.size()>0){
            //循环添加  30天的每日统计
            for(int i = 0;i<clockinTimeOne_values.size();i++){
                //获取日期时间
                dayDateTime = JSON.parseObject(clockinTimeOne_values.get(i).toString()).get("date").toString();
                //获取日期
                dayDate = sdf.format(sdf.parse(dayDateTime));
                //获取年月
                yearMonth = sdf_yearmonth.format(sdf_yearmonth.parse(dayDate));
                //获取工作时长(小时)
                workHour = new BigDecimal(JSON.parseObject(workHours_values.get(i).toString()).get("value").toString()).divide(new BigDecimal(60),2,BigDecimal.ROUND_UP);
                //获取加班时长(小时)
                workOverHour = new BigDecimal(JSON.parseObject(workOverTimeHours_values.get(i).toString()).get("value").toString()).divide(new BigDecimal(60),2,BigDecimal.ROUND_UP);
                //获取上班1打卡时间
                clockinTimeOne_dateTime = dateTime.getDateTime(clockinTimeOne_values, i);
                //获取下班1打卡时间
                clockoutTimeOne_dateTime = dateTime.getDateTime(clockoutTimeOne_values, i);
                //获取上班2打卡时间
                clockinTimeTwo_dateTime = dateTime.getDateTime(clockinTimeTwo_values, i);
                //获取下班2打卡时间
                clockoutTimeTwo_dateTime = dateTime.getDateTime(clockoutTimeTwo_values, i);
                //获取上班3打卡时间
                clockinTimeThree_dateTime = dateTime.getDateTime(clockinTimeThree_values, i);
                //获取下班3打卡时间
                clockoutTimeThree_dateTime = dateTime.getDateTime(clockoutTimeThree_values, i);

                //判断上班1打卡时间或者下班1打卡时间不为空或者上班2打卡时间或者下班2打卡时间或者上班3打卡时间或者下班3打卡时间不为空
                if (clockinTimeOne_dateTime!="" || clockoutTimeOne_dateTime!="" || clockinTimeTwo_dateTime!="" || clockoutTimeTwo_dateTime!="" || clockinTimeThree_dateTime!="" || clockoutTimeThree_dateTime!=""){
                    //循环添加打卡记录
                    for(int j=0;j<records.size();j++){
                        //打卡记录
                        JSONObject record = JSON.parseObject(records.get(j).toString());
                        System.out.println(record);
                        //获取打卡记录id
                        String dk_id = record.get("id").toString();
                        //获取打卡类型
                        String checkType = "";
                        if (record.get("checkType")!=null){
                            checkType = record.get("checkType").toString();
                            if (checkType.equals("OnDuty")){
                                checkType = "上班";
                            }else {
                                checkType = "下班";
                            }
                        }else {
                            checkType = "其他";
                        }
                        //获取打卡日期
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        String workDate = "";
                        if (record.get("workDate")!=null){
                            String work_date = record.get("workDate").toString();
                            //时间戳转String
                            workDate = sdf.format(new Date(new Long(work_date)));
                        }

                        //获取打卡时间
                        sdf_time.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                        String checkTime = "";
                        if (record.get("userCheckTime")!=null){
                            String check_time = record.get("userCheckTime").toString();
                            //时间戳转String
                            checkTime = sdf_time.format(new Date(new Long(check_time)));
                        }

                        //获取打卡地址
                        String userAddress = "";
                        if (record.get("userAddress")!=null){
                            userAddress = record.get("userAddress").toString();
                        }
                        //获取打卡方式
                        String sourceType = record.get("sourceType").toString();
                        if (sourceType.equals("ATM")){
                            sourceType = "考勤机";
                        }else {
                            sourceType = "手机钉钉";
                        }
                        //获取打卡结果
                        //获取结果不为null
                        String timeResult = "";
                        if (record.get("timeResult")!=null){
                            timeResult= record.get("timeResult").toString();
                            if (timeResult.equals("Normal")){
                                timeResult = "正常";
                            }else if (timeResult.equals("Early")){
                                timeResult = "早退";
                            }else if (timeResult.equals("Late")){
                                timeResult = "迟到";
                            }
                        }else {
                            timeResult="其他";
                        }
                        //获取其他描述
                        String otherDescribe = "";
                        if (timeResult.equals("其他")){
                            otherDescribe = record.get("invalidRecordMsg").toString();
                        }
                        //判断是否存打卡记录，存在则创建(新建)一系列信息
                        if (dayDate.equals(workDate)) {
                            //更新(修改)班组月考勤
                            YKQHZDTOQueryBatch bzykqQuery = new YKQHZDTOQueryBatch();
                            //设置条件
                            //设置月份
                            bzykqQuery.setYF(yearMonth);
                            //设置项目
                            bzykqQuery.setPROJECT(project_id);
                            //设置班组
                            bzykqQuery.setSGBZ(team_id);
                            //根据条件进行修改(新建)
                            YKQHZDTOUpdateBatch bzykqUpdate = new YKQHZDTOUpdateBatch();
                            SaveOrUpdateBatchYKQHZDTO saveOrUpdateYKQHZDTO = new SaveOrUpdateBatchYKQHZDTO();
                            //设置项目
                            bzykqUpdate.setPROJECT(project_id);
                            //设置施工班组
                            bzykqUpdate.setSGBZ(team_id);
                            //设置月份
                            bzykqUpdate.setYF(yearMonth);
                            saveOrUpdateYKQHZDTO.setQuery(bzykqQuery);
                            saveOrUpdateYKQHZDTO.setUpdate(bzykqUpdate);
                            //存放到班组月考勤集合中
                            bzykqResponse_list.add(saveOrUpdateYKQHZDTO);


                            //更新(新建)考勤单
                            //设置要修改的数据条件
                            GRKQDDTOQueryBatch grkqddtoQuery = new GRKQDDTOQueryBatch();
                            //设置日期
                            grkqddtoQuery.setCQRQ(dayDate);
                            //设置所属项目
                            grkqddtoQuery.setXMMC(project_id);
                            //设置班组
                            grkqddtoQuery.setSGBZ(team_id);
                            //进行修改
                            GRKQDDTOUpdateBatch grkqddtoUpdate = new GRKQDDTOUpdateBatch();
                            SaveOrUpdateBatchGRKQDDTO saveOrUpdateGRKQDDTO = new SaveOrUpdateBatchGRKQDDTO();
                            //如果上方条件成立修改，不成立新建
                            //设置考勤单状态默认为已确认
                            grkqddtoUpdate.setZT("已确认");
                            //设置日期
                            grkqddtoUpdate.setCQRQ(dayDate);
                            //设置所属项目
                            grkqddtoUpdate.setXMMC(project_id);
                            //设置班组
                            grkqddtoUpdate.setSGBZ(team_id);
                            saveOrUpdateGRKQDDTO.setQuery(grkqddtoQuery);
                            saveOrUpdateGRKQDDTO.setUpdate(grkqddtoUpdate);
                            //存放到考勤单集合中
                            grkqdResponse_list.add(saveOrUpdateGRKQDDTO);


                            //创建工人月考勤
                            YKQHZRDTOQueryBatch grykqQuery = new YKQHZRDTOQueryBatch();
                            //设置月份
                            grykqQuery.setYF(yearMonth);
                            //设置项目
                            grykqQuery.setXM(project_id);
                            //设置班组
                            grykqQuery.setSGBZ(team_id);
                            //设置工人
                            grykqQuery.setGLGR(grxxid);
                            //根据条件去新建(修改)
                            YKQHZRDTOUpdateBatch grykqUpdate = new YKQHZRDTOUpdateBatch();
                            SaveOrUpdateBatchYKQHZRDTO saveOrUpdateYKQHZRDTO = new SaveOrUpdateBatchYKQHZRDTO();
                            //设置项目
                            grykqUpdate.setXM(project_id);
                            //设置施工班组
                            grykqUpdate.setSGBZ(team_id);
                            //设置工人
                            grykqUpdate.setGLGR(grxxid);
                            //设置年月
                            grykqUpdate.setYF(yearMonth);
                            //设置身份证
                            grykqUpdate.setIdcard(idCard);
                            saveOrUpdateYKQHZRDTO.setQuery(grykqQuery);
                            saveOrUpdateYKQHZRDTO.setUpdate(grykqUpdate);
                            //存放到工人月考勤明细集合中
                            grykqResponse_list.add(saveOrUpdateYKQHZRDTO);


                            //更新或新建考勤明细
                            GRKQMXDTOQueryBatch grkqmxdtoQuery = new GRKQMXDTOQueryBatch();
                            //设置条件 查询到的更新数据,不新建，反之新建
                            //工人id
                            grkqmxdtoQuery.setGRID(grxxid);
                            //用户考勤日期
                            grkqmxdtoQuery.setCQRQ(dayDate);
                            //更新信息
                            GRKQMXDTOUpdateBatch grkqmxdtoUpdate = new GRKQMXDTOUpdateBatch();
                            SaveOrUpdateBatchGRKQMXDTO saveOrUpdateGRKQMXDTO = new SaveOrUpdateBatchGRKQMXDTO();
                            //设置姓名
                            grkqmxdtoUpdate.setName(grxxname);
                            //设置联系电话
                            grkqmxdtoUpdate.setLXDH(lxdh);
                            //设置身份证号码
                            grkqmxdtoUpdate.setSFZHM(idCard);
                            //设置工人id
                            grkqmxdtoUpdate.setGRID(grxxid);
                            //设置出勤情况
                            if (workHour.compareTo(new BigDecimal(maxWorkHours))>-1){
                                //工作时长大于系统设置时长
                                grkqmxdtoUpdate.setCQQK("全天");
                            } else {
                                //工作时长小于系统时长且不等于0
                                grkqmxdtoUpdate.setCQQK("半天");
                            }
                            //设置出勤日期
                            grkqmxdtoUpdate.setCQRQ(dayDate);
                            //设置施工班组
                            grkqmxdtoUpdate.setSGBZ(team_id);
                            //设置项目
                            grkqmxdtoUpdate.setXMMC(project_id);
                            //设置钉钉用户id
                            grkqmxdtoUpdate.setDdUserId(userid);
                            //设置出勤工时
                            grkqmxdtoUpdate.setCqGShi(workHour);
                            //设置加班时长
                            grkqmxdtoUpdate.setJbGShi(workOverHour);
                            //设置考勤组
                            grkqmxdtoUpdate.setKqZu(userGroup);
                            //设置上班1打卡时间
                            grkqmxdtoUpdate.setSbDKTime1(clockinTimeOne_dateTime);
                            //设置下班1打卡时间
                            grkqmxdtoUpdate.setXbDKTime1(clockoutTimeOne_dateTime);
                            saveOrUpdateGRKQMXDTO.setQuery(grkqmxdtoQuery);
                            saveOrUpdateGRKQMXDTO.setUpdate(grkqmxdtoUpdate);
                            //存放到工人考勤明细集合中
                            grkqmxResponse_list.add(saveOrUpdateGRKQMXDTO);


                            //更新或新建工人打卡记录
                            GRDKJLuDTOQueryBatch grdkQuery = new GRDKJLuDTOQueryBatch();
                            //设置条件
                            //设置工人id
                            grdkQuery.setGrName(grxxid);
                            //设置项目
                            grdkQuery.setDkXMu(project_id);
                            //设置班组
                            grdkQuery.setDkBZu(team_id);
                            //设置数据id
                            grdkQuery.setDdid(dk_id);
                            //更新信息
                            GRDKJLuDTOUpdateBatch grdkUpdate = new GRDKJLuDTOUpdateBatch();
                            SaveOrUpdateBatchGRDKJLuDTO saveOrUpdateGRDKJLuDTO = new SaveOrUpdateBatchGRDKJLuDTO();
                            //设置修改(新建)信息
                            //设置姓名
                            grdkUpdate.setName(grxxname);
                            //设置钉钉用户id
                            grdkUpdate.setDdUserId(userid);
                            //设置打卡数据id
                            grdkUpdate.setDdid(dk_id);
                            //设置工人名称 关联
                            grdkUpdate.setGrName(grxxid);
                            //设置项目 关联
                            grdkUpdate.setDkXMu(project_id);
                            //设置班组  关联
                            grdkUpdate.setDkBZu(team_id);
                            //设置身份证号码
                            grdkUpdate.setSfZHMa(idCard);
                            //设置手机号
                            grdkUpdate.setPhoneNumber(lxdh);
                            //设置考勤组
                            grdkUpdate.setKqZu(userGroup);
                            //设置打卡类型
                            grdkUpdate.setCheckType(checkType);
                            //设置考勤日期
                            grdkUpdate.setKqDate(workDate);
                            //设置打卡时间
                            grdkUpdate.setDkTime(checkTime);
                            //设置打卡地址
                            grdkUpdate.setDkAddress(userAddress);
                            //设置打卡方式
                            grdkUpdate.setDkType(sourceType);
                            //设置打卡结果
                            grdkUpdate.setDkResult(timeResult);
                            //设置其他描述
                            grdkUpdate.setQtMShu(otherDescribe);
                            saveOrUpdateGRDKJLuDTO.setQuery(grdkQuery);
                            saveOrUpdateGRDKJLuDTO.setUpdate(grdkUpdate);
                            //存放到工人打卡记录集合中
                            grdkResponse_list.add(saveOrUpdateGRDKJLuDTO);
                            records.remove(j);
                            j--;
                        }
                    }
                }
            }
            //获取今天日期
            Calendar today = Calendar.getInstance();
            today.add(Calendar.DAY_OF_MONTH, 0);
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            String todayDate = sdf.format(today.getTime());
            System.out.println(todayDate);
            //修改工人花名册userid
            GrxxApi grxxApi = new GrxxApi();
            GRXXDTOQuery grxxdtoQuery = new GRXXDTOQuery();
            //设置查询条件
            //工人id
            grxxdtoQuery.setId(grxxid);
            GRXXDTOUpdate grxxdtoUpdate = new GRXXDTOUpdate();
            SaveOrUpdateGRXXDTO updateGRXXDTO = new SaveOrUpdateGRXXDTO();
            //设置修改条件
            //修改用户钉钉号
            grxxdtoUpdate.setDdUserId(userid);
            //修改上次打卡日期为今天
            grxxdtoUpdate.setLastDKDate(todayDate);
            //查询修改
            updateGRXXDTO.setQuery(grxxdtoQuery);
            updateGRXXDTO.setUpdate(grxxdtoUpdate);
            GRXXSaveOrUpdateDataResponseObject grxxResponse = grxxApi.saveOrUpdateGRXXUsingPOST(updateGRXXDTO);
            System.out.println("工人花名册："+grxxResponse.getMessage());

            //更新/新建班组月考勤
            YKQHZSaveOrUpdateDataBatchResponseObject bzykqResponse = bzykqApi.saveOrUpdateBatchYKQHZUsingPOST(bzykqResponse_list);
            System.out.println("班组月考勤："+bzykqResponse.getMessage());
            //更新/新建考勤单
            GRKQDSaveOrUpdateDataBatchResponseObject grkqdResponse = grkqdApi.saveOrUpdateBatchGRKQDUsingPOST(grkqdResponse_list);
            System.out.println("考勤单："+grkqdResponse.getMessage());
            //更新/新建工人月考勤
            YKQHZRSaveOrUpdateDataBatchResponseObject grykqResponse = grykqApi.saveOrUpdateBatchYKQHZRUsingPOST(grykqResponse_list);
            System.out.println("工人月考勤："+grykqResponse.getMessage());
            //更新/新建考勤明细
            GRKQMXSaveOrUpdateDataBatchResponseObject grkqmxResponse = grkqmxApi.saveOrUpdateBatchGRKQMXUsingPOST(grkqmxResponse_list);
            System.out.println("考勤明细："+grkqmxResponse.getMessage());
            //更新/新建打卡记录
            GRDKJLuSaveOrUpdateDataBatchResponseObject grdkResponse = grdkApi.saveOrUpdateBatchGRDKJLuUsingPOST(grdkResponse_list);
            System.out.println("打卡记录："+grdkResponse.getMessage());
        }else {
            System.out.println("不存在打卡记录");
        }
        return null;
    }
}
