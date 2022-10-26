package com.ccx.time.service;

import com.ccx.common.entity.DayTime;
import com.ccx.common.result.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文件名：
 * 版权：Copyright 2017-2022 CMCC ALL Right Reserved.
 * 描述：
 */
@Service
public interface TimeService {

    /**
     * 开始计时
     *
     * @param request request
     * @return ResponseData
     * @throws Exception
     */
    ResponseData startTime(HttpServletRequest request) throws Exception;

    /**
     * 结束计时
     *
     * @param request request
     * @return ResponseData
     * @throws Exception
     */
    ResponseData overTime(HttpServletRequest request) throws Exception;

    /**
     * 查询用户每天时长.
     *
     * @param request request
     * @param dayTime 日期
     * @return ResponseData
     */
    ResponseData<List<DayTime>> queryTimeOfDay(HttpServletRequest request, DayTime dayTime);
}
