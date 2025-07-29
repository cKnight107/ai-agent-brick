package com.agent.brick.service;

import com.agent.brick.controller.request.AiReq;
import com.agent.brick.pojo.vo.JsonResult;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @since 2025/6/1
 *
 * @author cKnight
 */
public interface AiService {
    /**
     * 聊天
     * @param req 入参
     * @return 信息
     */
    Flux<ServerSentEvent<String>> completions(AiReq req);

    /**
     * 对文档进行ETL
     * @param file 文档
     * @return void
     */
    JsonResult<Void> etlDocument(MultipartFile file);
}
