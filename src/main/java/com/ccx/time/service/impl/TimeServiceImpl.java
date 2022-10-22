package com.ccx.time.service.impl;

import com.ccx.common.entity.DayTime;
import com.ccx.common.entity.DayTimeExample;
import com.ccx.common.result.ResponseData;
import com.ccx.time.mapper.DayTimeMapper;
import com.ccx.time.service.TimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件名：
 * 版权：Copyright 2017-2022 CMCC ALL Right Reserved.
 * 描述：
 */
@Service
public class TimeServiceImpl implements TimeService {

    @Autowired
    private DayTimeMapper dayTimeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public ResponseData<?> startTime(HttpServletRequest request) {
        ResponseData<DayTime> responseData = new ResponseData<>();
        // 1、判断用户的操作是否违规: 没有点击结束，再次点击开始
        if (!isFirstClickStart(request)) {
            responseData.setSuccess(false);
            responseData.setMessage("违规操作");
            return responseData;
        }
        Integer userId = Integer.parseInt(request.getParameter("userId"));
//        if (userId == null) {
//            responseData.setMessage("userId为空");
//        }

        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        DayTime dayTime = new DayTime();
        dayTime.setDayTime(localDate);
        // todo 暂时模拟 userID
        dayTime.setUserId(userId);
        dayTime.setStartTime(localDateTime);

        // 3、插入操作 并将 timeId 存入 redis中
        int i = dayTimeMapper.insertSelective(dayTime);
        String s = Integer.toString(userId);
        redisTemplate.opsForValue().set(s,"1");
        return responseData;
    }


    /**
     * 用户是否是当天第一次点击开始按钮，返回true是.
     *
     * @param request request
     * @return boolean
     */
    private boolean isFirstClickStart(HttpServletRequest request) {
        DayTimeExample example = new DayTimeExample();
        example.createCriteria().andUserIdEqualTo(Integer.valueOf(request.getParameter("userId")));
//        example.createCriteria().andTimeIdEqualTo((Long) request.getAttribute("timeId"));
        List<DayTime> dayTimes = dayTimeMapper.selectByExample(example);
        return CollectionUtils.isEmpty(dayTimes);
    }
}
