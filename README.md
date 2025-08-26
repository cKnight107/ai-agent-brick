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
4. **提示词引擎**
   - **WwhPromptEngine**基于[《用系统架构思维，告别“意大利面条式”系统提示词》](https://www.bestblogs.dev/article/9d613b)，进行编码实现做到了 **Prompt As Code**。详情移步[v1.1.0](version/v1.1.0.md)查看。
     - **难以维护：修改一个提示词，可能需要在多个地方同步。** 通过集中式定义，所有提示词逻辑都集中在一个 WwhPromptEngine 实例中通过 Builder 模式构建。确保了整个提示词的定义是集中且唯一的，避免了代码中多处硬编码字符串的问题。维护时只需修改一个地方（Builder 链中的某一步），所有使用该引擎的地方都会自动获得更新，彻底解决了“多处同步”的难题。
     - **无法追踪：不知道线上运行的模型用的是哪个版本的提示词。** 通过**内容指纹** 生成提示词hash值记录日志，运维人员通过查看日志或Trace精准定位“这个请求是用的那个提示词（hash）”，做到全链路可追踪。
     - **无法复现：实验结果无法复现，因为不清楚当时用的提示词具体是什么。** 可通过数据库进行版本快照管理进行追溯，也可以实现动态版本管理。
     - **缺乏协作：团队成员之间无法高效地共享和评审提示词。** 代码即文档通过Builder 链式调用形成一份结构化的、可读性极强的文档。纳入到Git版本管理像提交普通代码一样，进行代码审查。
   - 引用原文，说明主要解决的痛点
     1. **规则打架，行为摇摆不定**
        - 我们通过角色与目标定义，建立清晰的决策框架，让AI在冲突时知道“我是谁，我该听谁的”。
     2. **越改越乱，最终没人敢动**
        - 我们通过模块化与分层，实现高内聚、低耦合，让每次修改都像做外科手术一样精准可控。 
     3. **响应像“开盲盒”，核心价值被稀释**
        - 我们通过流程设计，规划出清晰的行动路径，确保模型的“注意力”被引导至核心任务上，保障产品价值的稳定输出。
## 🛠️ 技术架构
JDK21+Spring Ai 1.0+Spring Boot 3.4+PostgreSql+Redis7.x
## 如何贡献
欢迎任何形式的贡献，包括但不限于：
- 提交 Issue 报告 Bug 或提出功能建议。
- 提交 Pull Request 修复问题或添加新功能。
- 参与文档编写，帮助完善教程和说明。

## 📄 开源协议
本项目基于 Apache License 2.0 开源，详情请查看 [LICENSE](LICENSE) 文件。部分代码参考了 **Spring AI**、**OWL**、**Mybatis-Plus**的开源实现，保留其原始版权和许可声明。

## 🙏 特别鸣谢
感谢以下优秀的开源项目为本项目提供支持：
- [Spring AI](https://spring.io/projects/spring-ai) - Spring AI
- [OWL](https://github.com/camel-ai/owl) - OWL
- [Mybatis-Plus](https://github.com/baomidou/mybatis-plus) - Mybatis-Plus

## 🖊️  联系方式
- [Discord](https://discord.gg/jTcXXHPD3e)
- [Gmail](https://chenkai107cn@gmail.com)

---
<div align="center">


**[⭐ 点个Star支持一下](https://github.com/cKnight107/ai-agent-brick)** • **[ Fork 开始贡献](https://github.com/cKnight107/ai-agent-brick/fork)**
</div>