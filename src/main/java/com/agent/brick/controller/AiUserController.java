package com.agent.brick.controller;

import com.agent.brick.controller.request.AiUserReq;
import com.agent.brick.pojo.vo.AiUserVO;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.service.AiUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author autoCode
 * @since 2025-06-15
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class AiUserController {
    private final AiUserService aiUserService;

    @PostMapping("/createUser")
    public JsonResult<Void> createUser(@RequestBody AiUserReq userReq){
        return JsonResult.buildSuccess(aiUserService.createUser(userReq));
    }

    @PostMapping("/login")
    public JsonResult<AiUserVO> login(@RequestBody AiUserReq userReq){
        return JsonResult.buildSuccess(aiUserService.login(userReq));
    }
}
