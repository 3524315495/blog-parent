package com.ccx.time.controller;

import com.ccx.common.entity.DayTime;
import com.ccx.common.result.ResponseData;
import com.ccx.time.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文件名：
 * 版权：Copyright 2017-2022 CMCC ALL Right Reserved.
 * 描述：
 */
@RestController
public class TimeController {

    @Autowired
    private TimeService timeService;

    // 点击开始按钮，开始计时。
    @RequestMapping("/time/startTime")
    public ResponseData startTime(HttpServletRequest request) throws Exception {
        return timeService.startTime(request);
    }

    @RequestMapping("/time/overTime")
    public ResponseData overTime(HttpServletRequest request) throws Exception {
        return timeService.overTime(request);
    }

    /**
     * 查询用户每天的学习时间
     *
     * @param request 请求上下文
     * @return ResponseData
     */
    @RequestMapping("/time/queryTimeOfDay")
    public ResponseData<List<DayTime>> queryTimeOfDay(HttpServletRequest request,
                                                      @RequestBody(required = false) DayTime dayTime) {
        return timeService.queryTimeOfDay(request, dayTime);
    }
}
