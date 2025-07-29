package com.agent.brick.controller;

import com.agent.brick.service.AiRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class AiRoleController {
    private final AiRoleService aiRoleService;
}
