# AgentBrick
`AgentBrick` 是一个基于 Spring AI 的开源框架，旨在通过模块化设计和积木式开发，帮助开发者轻松构建智能应用。它在 Spring AI 的基础上进行了二次抽象，支持多种 LLM的无缝集成，并提供了灵活的 Agent 构建能力。
## ✨ 核心亮点
1. **LLM 的通用适配能力**
    - **基于 Spring AI 的 OpenAI 核心代码进行二次抽象**，使得框架能够轻松适配不同厂商的 LLM（如 Qwen、Kimi、MiniMax、ZhiPu 等）。
    - 提供统一的 `ChatModel` 接口，屏蔽底层实现差异，降低开发复杂度。
      ```java
      @Bean(ChatModelConstants.KIMI_K2_CHAT_MODEL)
      public KimiChatModel kimiK2ChatModel(@Qualifier("kimiApi") AiCommonApi kimiApi){
      return (KimiChatModel) LLMEnum.KIMI.genChatModel(
          KimiOptions.builder()
              .model(ChatModelEnum.KIMI_K2)
              .temperature(0.7)
              .maxTokens(8048)
              .build(),
          kimiApi
          );
      }
      ```
2. **积木式 Agent 构建**
    - 支持模块化搭建 Agent，用户可以自由组合 Prompt、Context 和 Tools。
    - 各个环节灵活搭配
      - 上下层分离。项目中使用Dify的知识库检索，作为底层RAG检索能力。上层编写符合业务的RAG策略。各个层次可随时更换搭配。
      - LLM分离。可根据不同的Agent搭配不同的LLM，若出现更加符合Agent能力的LLM可随时更换，不影响业务代码。
      - Prompt分离。测试不同Prompt以寻找最符合Agent要求的Prompt。
    - 编写符合业务逻辑的Agent架构
      - 项目中使用开源项目[OWL](https://github.com/camel-ai/owl)为例，使用Agent快速搭建。
    ```java
      @Bean(AgentConstants.RAG_AGENT)
      public RagAgent ragAgent(@Qualifier(ChatModelConstants.QWEN_3_PLUS_CHAT_MODEL) QwenChatModel qwenChatModel,
      RagTools ragTools){
      return  RagAgent.builder()
        .chatModel(qwenChatModel)
        .prompt(AgentPromptConstants.RAG_AGEMNT_SYSTEM_PROMPT)
        .tools(ragTools)
       .build();
      }
    ```
3. **高效的数据查询与处理**
    - **基于 MyBatis-Plus 进行二次封装**，提供更简洁、高效的数据库查询接口。
    ```java
    @Override
    public Map<Long,ChatRecordMsgJsonDto> queryMsg(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Map.of();
        }
        return where(aiChatRecordMsgMapper)
                .in(BaseDO::getId, ids)
                .hasMap(
                        BaseDO::getId,
                        v-> JSONObject.parseObject(v.getMsgDetail().toString(),ChatRecordMsgJsonDto.class)
                );
        }
    ```
## 🛠️ 技术架构
JDK21+Spring Ai 1.0+Spring Boot 3.4+PostgreSql+Redis7.x
## 如何贡献
欢迎任何形式的贡献，包括但不限于：
- 提交 Issue 报告 Bug 或提出功能建议。
- 提交 Pull Request 修复问题或添加新功能。
- 参与文档编写，帮助完善教程和说明。

## 📄 开源协议
本项目采用 **MIT 开源协议**，详情请查看 [LICENSE](LICENSE) 文件。

## 🙏 特别鸣谢
感谢以下优秀的开源项目为本项目提供支持：
- [Spring AI](https://spring.io/projects/spring-ai) - Spring AI
- [OWL](https://github.com/camel-ai/owl) - OWL

## 🖊️  联系方式
- [Discord](https://discord.gg/jTcXXHPD3e)
- [Gmail](https://chenkai107cn@gmail.com)

---
<div align="center">


**[⭐ 点个Star支持一下](https://github.com/cKnight107/ai-agent-brick)** • **[ Fork 开始贡献](https://github.com/cKnight107/ai-agent-brick/fork)**
</div>