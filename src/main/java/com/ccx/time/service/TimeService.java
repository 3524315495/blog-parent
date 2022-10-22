package com.ccx.time.service;

import com.ccx.common.result.ResponseData;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 文件名：
 * 版权：Copyright 2017-2022 CMCC ALL Right Reserved.
 * 描述：
 */
@Service
public interface TimeService {

    ResponseData<?> startTime(HttpServletRequest request);
}
