package com.agent.brick.ai.prompt;


import com.agent.brick.constants.GlobalConstants;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.template.st.StTemplateRenderer;

/**
 * 提示词常量
 * @since 2025/6/3
 * @author cKnight
 */
public interface PromptConstants {

    /** 尖括号字符串模版 */
    StTemplateRenderer STRING_TEMPLATE = StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build();

    /** 默认的用户提示词字符串 query+思维链 */
    String DEFAULT_USER_PROMPT_STR = STR."{query}\n\{PromptConstants.COT}";

    /** CoT 思维链进阶 减少推理长度 */
    String COT = "Think step by step, but only keep a minimum draft for each thinking step.";

    /** 上下文 */
    String CONTEXT = """
            Context information is below.
            ---------------------
            {context}
            ---------------------
            """;

    Prompt SIMPLE_SYSTEM_PROMPT = MyPromptTemplate.start()
            .messageType(MessageType.SYSTEM)
            .template("""
                    你是一个高级智能助手，专注于帮助用户解决问题。
                    """)
            .end()
            .create();

    /** 默认的系统提示模版 */
    MyPromptTemplate DEFAULT_TEMPLATE_SYSTEM = MyPromptTemplate
            .start()
            .messageType(MessageType.SYSTEM)
            .template(STR."""
                    主要任务
                    - 帮助用户解决问题或者处理任务
                    工作要求
                    - 在执行任务前，要确认明确的目标以及用户期望的结果，若无法确认可主动向用户询问。
                    - 获取到结果时，要根据目标与期望结果，对结果进行思考，无法满足则继续执行，已满足则将结果汇总输出给用户。
                    注意事项
                    - 最后输出时，要再次回看问题，思考结果是否满足。
                    用户信息
                    {\{GlobalConstants.USER}}
                    自我介绍
                    {\{GlobalConstants.ONESELF}}
                    请求信息
                    {\{GlobalConstants.QUERY}}
                    """)
            .end();


    /** 思维链用户模版  */
    MyPromptTemplate COT_TEMPLATE_SYSTEM = MyPromptTemplate
            .start()
            .messageType(MessageType.USER)
            .template(DEFAULT_USER_PROMPT_STR)
            .end();

    /** 默认的记忆模版 */
    MyPromptTemplate DEFAULT_CHAT_MEMORY_TEMPLATE = MyPromptTemplate
            .start()
            .template(STR."""
                {\{GlobalConstants.INSTRUCTIONS}}
                
                Use the conversation memory from the MEMORY section to provide accurate answers.
                ---------------------
                MEMORY:
                {\{GlobalConstants.MEMORY}}
                ---------------------
                """)
            .end();

    /** 关键词提取模版 */
    MyPromptTemplate KEYWORD_TRANSFORMER_TEMPLATE = MyPromptTemplate
            .start()
            .messageType(MessageType.USER)
            .template(STR."""
                    {\{GlobalConstants.CONTEXT}}
                    Give <num> unique keywords for this document.
                    Format as comma separated. Keywords:
                    使用中文回答
                    """)
            .end();

    /** 知识检索评价模版 */
    MyPromptTemplate GRADE_RETRIEVE_TEMPLATE = MyPromptTemplate
            .start()
            .messageType(MessageType.SYSTEM)
            .template(STR."""
                    您是评估检索到的文档与用户问题的相关性的评分者。
                    这是检索的文档:
                    如果文档包含与用户问题相关的关键字或语义，请将其评为相关。
                    给出二元分数yes或no分数，以指示文档是否与问题相关。
                    注意
                    - 只能回答yes 或 no，不可回答其他答案。
                    {\{GlobalConstants.CONTEXT}}
                    这是用户的问题:
                    {\{GlobalConstants.QUERY}}
                    """)
            .end();

    /** 重写检索问题模版 */
    MyPromptTemplate REWRITE_RETRIEVE_QUESTION_TEMPLATE = MyPromptTemplate
            .start()
            .messageType(MessageType.SYSTEM)
            .template(STR."""
                    查看输入并尝试推理潜在的语义意图/含义。
                    这是最初的问题:
                    {\{GlobalConstants.QUERY}}
                    提出一个改进的问题：
                    """)
            .end();

    /** 检索答案最终回答模版 */
    MyPromptTemplate GENERATE_RETRIEVE_ANSWER_TEMPLATE = MyPromptTemplate
            .start()
            .messageType(MessageType.SYSTEM)
            .template(STR."""
                    你是问答任务的助手。
                    使用以下检索到的上下文片段来回答问题。
                    如果你不知道答案，就说你不知道。
                    问题:
                    {\{GlobalConstants.QUERY}}
                    上下文:
                    {\{GlobalConstants.CONTEXT}}
                    """)
            .end();
}
