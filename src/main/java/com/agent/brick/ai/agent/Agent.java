package com.agent.brick.ai.agent;

import com.agent.brick.controller.request.AiReq;
import org.springframework.ai.chat.client.ChatClient;

/**
 * agent顶层接口
 * @since 2025/6/20
 * @author cKnight
 */
public interface Agent {
    ChatClient.ChatClientRequestSpec chatClient();

    ChatClient.CallResponseSpec call();

    ChatClient.StreamResponseSpec steam();

    ChatClient.ChatClientRequestSpec chatClient(AiReq req);

    ChatClient.CallResponseSpec call(AiReq req);

    ChatClient.StreamResponseSpec steam(AiReq req);
}
