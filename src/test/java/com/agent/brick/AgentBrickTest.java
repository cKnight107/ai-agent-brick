package com.agent.brick;

import com.agent.brick.ai.tools.request.DifyDatasetReq;
import com.agent.brick.api.DifyHttpClient;
import com.agent.brick.compant.AiComponent;
import com.agent.brick.compant.AuthComponent;
import com.agent.brick.config.DifyConfig;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.process.strategy.agent.OwlAgentStrategy;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Resource
    private DifyHttpClient difyHttpClient;

    @Resource
    private DifyConfig difyConfig;

    @Resource
    private Executor virtualThreadExecutor;

    @Resource
    private AiComponent aiComponent;

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

    @Test
    public void difyTest(){
        DifyDatasetReq difyDatasetReq = new DifyDatasetReq("12", 3, List.of());
//        List<String> strings = difyHttpClient.datasetDocuments(difyConfig.getDatasetId());
        JSONObject res = difyHttpClient.datasetRetrieve(difyConfig.getDatasetId(), difyDatasetReq);
        log.info("agentTest,strings:{}",res);
    }

    @Test
    public void virtualTest(){
        //由于@Async 由AOP代理，必须外部调用，自调用不会走代理即无法使用@Async
//        List<String> list = IntStream.range(0, 10)
//                .mapToObj(_ -> aiComponent.getTest())
//                .toList()
//                .stream()
//                .map(CompletableFuture::join)
//                .toList();

        IntStream.range(0,10)
                .mapToObj(_ -> CompletableFuture.runAsync(this::getTestV2,virtualThreadExecutor))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();
    }


    @Async(GlobalConstants.ASYNC_VIRTUAL_THREAD)
    public CompletableFuture<String> getTest(){
        String threadName = Thread.currentThread().getName();
        try {
            int sleepSeconds = ThreadLocalRandom.current().nextInt(1, 5);
            Thread.sleep(Duration.ofSeconds(sleepSeconds));

            String result = "睡眠了 " + sleepSeconds + " 秒 [线程: " + threadName + "]";
            log.info("✅ @Async 方法完成: {}", result);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("❌ @Async 方法异常", e);
            throw new RuntimeException("任务执行失败", e);
        }
    }

    public String getTestV2(){
        String threadName = Thread.currentThread().getName();
        try {
            int sleepSeconds = ThreadLocalRandom.current().nextInt(1, 5);
            Thread.sleep(Duration.ofSeconds(sleepSeconds));

            String result = "睡眠了 " + sleepSeconds + " 秒 [线程: " + threadName + "]";
            log.info("✅ @Async 方法完成: {}", result);
            return result;
        } catch (Exception e) {
            log.error("❌ @Async 方法异常", e);
            throw new RuntimeException("任务执行失败", e);
        }
    }


}
