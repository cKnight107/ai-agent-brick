package com.agent.brick;

import com.agent.brick.compant.AuthComponent;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.process.strategy.agent.OwlAgentStrategy;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/29
 */
@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class AgentBrickTest {
    @Resource
    private OwlAgentStrategy owlAgentStrategy;

    @Resource
    private AuthComponent authComponent;

    @Test
    public void owlTest(){
        AiReq aiReq = new AiReq();
        Long id = IdWorker.getId();
//        Long id = 1945317363336691714L;
        log.info("agentTest,id:{}",id);
        String token = "znZr8OFhld+M34ICfRbr00ExYS1uxV7lJIH/fRmSxbk5pCygf3GJXsC2lh2lCuAEHweLBX1sZXQJcC06+hYaw9MaoPwqHCHUug3lyAazExs=";
        aiReq.setChatId(id);
//        aiReq.setTaskSpecifiedFlag(true);
        //可自定义用户信息
        aiReq.setSysCacheUserDto(authComponent.getAdminUserInfo(token));
        aiReq.setMessage(AiMessageReq.builder().content("从知识库检索一下七年级历史下册核心知识点，并出一套难度中等的期末试卷，满分一百。").build());
//        aiReq.setMessage(AiMessageReq.builder().content("根据上述的知识点串讲,出一套期末测试试卷,要求难度中等,选择题、填空题、大题,共120分").build());
        owlAgentStrategy.call(aiReq);
    }
}
