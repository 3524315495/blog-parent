package com.ccx.time.service.impl;

import com.ccx.common.entity.DayTime;
import com.ccx.common.entity.DayTimeExample;
import com.ccx.common.entity.TotalTime;
import com.ccx.common.result.ResponseData;
import com.ccx.common.result.constant.RedisConstant;
import com.ccx.time.mapper.DayTimeMapper;
import com.ccx.time.mapper.TotalTimeMapper;
import com.ccx.time.service.TimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private TotalTimeMapper totalTimeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<?> startTime(HttpServletRequest request) throws Exception {
        ResponseData<DayTime> responseData = new ResponseData<>();
        // 1、判断用户是否违规
        if (isViolations(request)) {
            responseData.setSuccess(false);
            responseData.setMessage("您已经处于一个任务中，请先结束！");
            return responseData;
        }
        Integer userId = Integer.parseInt(request.getParameter("userId"));
        DayTime dayTime = new DayTime();
        dayTime.setDay(new Date());
        dayTime.setUserId(userId);
        dayTime.setStartTime(new Date());
        // 3、插入操作 并将 timeId 存入 redis中
        dayTimeMapper.insertSelectivePrimary(dayTime);
        Long timeId = dayTime.getTimeId();
        // user->timeId
        redisTemplate.opsForHash().put(RedisConstant.USER_ID_TIME_ID, userId.toString(), timeId.toString());

        return responseData;
    }

    // 点击结束按钮
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<?> overTime(HttpServletRequest request) throws Exception {
        // todo 如果redis意外关闭，则需要考虑持久化 或者 搭建集群
        ResponseData<TotalTime> responseData = new ResponseData<>();
        Date overTime = new Date();
        // 1.更新day_time表中的over_time为当前时间
        String userId = request.getParameter("userId");
        // 1.1 根据 userId 获取到 time_id
        Long timeId = Long.valueOf(redisTemplate.opsForHash().get(RedisConstant.USER_ID_TIME_ID, userId) + "");
        DayTime dayTime = new DayTime();
        dayTime.setOverTime(new Date());
        dayTime.setTimeId(timeId);
        dayTimeMapper.updateByPrimaryKeySelective(dayTime);
        // 2.如果该用户是第一次使用，则向total_time表插入一条数据；否则，更新操作
        // 2.1 查询total_time中是否存在 该userId，不存在则插入，存在则更新
        // 获取开始时间
        DayTime dayTimeExist = dayTimeMapper.selectByPrimaryKey(timeId);
        Date startTime = dayTimeExist.getStartTime();
        String hours = String.format("%.2f", (overTime.getTime() - startTime.getTime()) / (1000.0 * 60.0 * 60.0));
        TotalTime totalTime = new TotalTime();
        totalTime.setUserId(Integer.valueOf(userId));

        TotalTime totalTimes = totalTimeMapper.selectByPrimaryKey(Integer.valueOf(userId));
        if (totalTimes == null) {
            // 为空，插入数据
            totalTime.setStartDate(startTime);
            totalTime.setTotalTime(hours);
            totalTimeMapper.insertSelective(totalTime);
        } else {
            // 不为空，更新数据
            totalTime.setTotalTime(String.format("%.2f",
                    Double.parseDouble(hours) + Double.parseDouble(totalTimes.getTotalTime())));
            totalTimeMapper.updateByPrimaryKeySelective(totalTime);
        }
        // 将 redis 中当前用户的 timeId 重置为 -1
        redisTemplate.opsForHash().put(RedisConstant.USER_ID_TIME_ID, userId, "-1");
        ArrayList<TotalTime> list = new ArrayList<>();
        list.add(totalTime);
        responseData.setRows(list);
        return responseData;
    }

    @Override
    public ResponseData<List<DayTime>> queryTimeOfDay(HttpServletRequest request, DayTime dayTime) {
        String userId = request.getParameter("userId");
        ResponseData<List<DayTime>> data = new ResponseData<>();
        data.setRows(dayTimeMapper.queryTimeOfDay(userId, dayTime));
        return data;
    }


    /**
     * 用户是否是违规操作
     *
     * @param request request
     * @return boolean
     */
    private boolean isViolations(HttpServletRequest request) {
        DayTimeExample example = new DayTimeExample();
        // 获取timeId
        Object timeId = redisTemplate.opsForHash()
                .get(RedisConstant.USER_ID_TIME_ID, request.getParameter("userId"));
        if (timeId == null) {
            return false;
        }
        Long tId = Long.valueOf(timeId.toString());
        example.createCriteria().andUserIdEqualTo(Integer.valueOf(request.getParameter("userId")))
                .andTimeIdEqualTo(tId);
        List<DayTime> dayTimes = dayTimeMapper.selectByExample(example);
        return CollectionUtils.isNotEmpty(dayTimes);
    }
}
