package com.agent.brick.service;

import com.agent.brick.controller.request.AiUserReq;
import com.agent.brick.pojo.vo.AiUserVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-06-15
 */
public interface AiUserService {

    /**
     * 创建用户
     * @param userReq req
     * @return void
     */
    Void createUser(AiUserReq userReq);

    /**
     * 登录
     * @param userReq req
     * @return userVo
     */
    AiUserVO login(AiUserReq userReq);
}
