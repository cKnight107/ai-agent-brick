# AgentBrick

`AgentBrick` is an open-source framework based on **Spring AI**, designed to help developers easily build intelligent applications through modular design and LEGO-style development. It provides a secondary abstraction over Spring AI, enabling seamless integration with multiple LLMs and offering flexible Agent construction capabilities.

## ✨ Core Features

1. **Universal LLM Adaptation**
    - **Built on a secondary abstraction of Spring AI's OpenAI core code**, allowing effortless integration with various LLM providers such as Qwen, Kimi, MiniMax, ZhiPu, and more.
    - Offers a unified `ChatModel` interface to abstract away underlying implementation differences, reducing development complexity.
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

2. **Modular Agent Construction**
    - Supports modular Agent building, allowing developers to freely combine **Prompts**, **Contexts**, and **Tools**.
    - Flexible component composition:
        - **Layer separation**: Uses Dify's knowledge base retrieval as the底层 RAG capability, while implementing custom business-specific RAG strategies at the upper layer. Layers can be swapped independently.
        - **LLM decoupling**: Different Agents can use different LLMs. Switching to a more suitable LLM does not affect business logic.
        - **Prompt isolation**: Easily test different prompts to find the optimal one for a given Agent.
    - Enables building business-specific Agent architectures.
        - Uses the open-source project [OWL](https://github.com/camel-ai/owl) as an example to rapidly construct Agents.
      ```java
      @Bean(AgentConstants.RAG_AGENT)
      public RagAgent ragAgent(@Qualifier(ChatModelConstants.QWEN_3_PLUS_CHAT_MODEL) QwenChatModel qwenChatModel,
                               RagTools ragTools){
          return RagAgent.builder()
                  .chatModel(qwenChatModel)
                  .prompt(AgentPromptConstants.RAG_AGENT_SYSTEM_PROMPT)
                  .tools(ragTools)
                  .build();
      }
      ```

3. **Efficient Data Query and Processing**
    - **Enhanced wrapper over MyBatis-Plus**, providing cleaner and more efficient database query interfaces.
      ```java
      @Override
      public Map<Long, ChatRecordMsgJsonDto> queryMsg(List<Long> ids) {
          if (CollectionUtils.isEmpty(ids)) {
              return Map.of();
          }
          return where(aiChatRecordMsgMapper)
                  .in(BaseDO::getId, ids)
                  .hasMap(
                      BaseDO::getId,
                      v -> JSONObject.parseObject(v.getMsgDetail().toString(), ChatRecordMsgJsonDto.class)
                  );
      }
      ```

## 🛠️ Technology Stack
JDK 21 + Spring AI 1.0 + Spring Boot 3.4 + PostgreSQL + Redis 7.x

## How to Contribute
Contributions of any kind are welcome, including but not limited to:
- Submitting Issues to report bugs or suggest features.
- Creating Pull Requests to fix issues or add new functionality.
- Participating in documentation improvements, tutorials, and guides.

## 📄 License
This project is open-sourced under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details. Some code is inspired by open-source implementations from **Spring AI** and **OWL** and **Mybatis-Plus**, with original copyrights and license notices preserved.

## 🙏 Special Thanks
We gratefully acknowledge the following excellent open-source projects for their support:
- [Spring AI](https://spring.io/projects/spring-ai) - Spring AI
- [OWL](https://github.com/camel-ai/owl) - OWL
- - [Mybatis-Plus](https://github.com/baomidou/mybatis-plus) - Mybatis-Plus

## 🖊️ Contact
- [Discord](https://discord.gg/jTcXXHPD3e)
- [Gmail](https://chenkai107cn@gmail.com)

---
<div align="center">

**[⭐ Star this project](https://github.com/cKnight107/ai-agent-brick)** • **[Fork and contribute](https://github.com/cKnight107/ai-agent-brick/fork)**

</div>