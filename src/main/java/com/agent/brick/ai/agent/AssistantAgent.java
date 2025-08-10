package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.MsgRecordAdvisor;
import com.agent.brick.ai.advisor.RequestChatMemoryAdvisor;
import com.agent.brick.ai.prompt.WwhPromptEngine;
import com.agent.brick.ai.prompt.constants.PromptShotConstants;
import com.agent.brick.ai.prompt.enums.InputSourceEnum;
import com.agent.brick.ai.tools.AiTools;
import com.agent.brick.compant.AiComponent;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 助手 智能体
 * @since 2025/6/20
 *
 * @author cKnight
 */
public class AssistantAgent extends AbstractAgent {

    public static Builder<AssistantAgent> builder() {
        return new Builder<>(AssistantAgent.class);
    }

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(this.chatModel).prompt(getPrompt(req));
        this.tools.forEach(chatClientRequestSpec::tools);
        chatClientRequestSpec.messages(AiUtil.genUserPrompt(req.getMessage()).getUserMessage());
        chatClientRequestSpec.advisors(
//                new RequestChatMemoryAdvisor(req),
                new MsgRecordAdvisor(req)
        );
        return chatClientRequestSpec;
    }

    @Override
    public Prompt getPrompt(AiReq req) {
        WwhPromptEngine wwhPromptEngine = WwhPromptEngine.begin()
                //第一层 核心定义
                .coreDefinition()
                .identity("你是Bob，一个精通各个学科的助教智能助手。")
                .personality("你的沟通风格是专业、严谨、高效而又风趣的。你致力于通过专业、可靠的行动来帮助教师更好执行教学任务。")
                .stance("在所有教学任务中，你的核心立场是：永远将知识严谨性放在首位。")
                .functionalGoals(
                        "通过逐步、迭代地使用工具，系统性地完成用户指定的教学任务",
                        "在必要时，通过提问澄清模糊不清的需求，以确保任务的准确执行。"
                )
                .valuesGoals(
                        "为教师提供专业、严谨、可靠的教学方案，将复杂的教学任务分解为清晰、可管理、可验证的步骤。"
                )
                .readLine(
                        STR."例如：\{PromptShotConstants.MUST_NEVER}使用“在我看来”、“我认为”等主观性强的短语"
                )
                .coreDefinitionEnd()
                //第二层 交互接口
                .interactionInterface()
                .inputSources(
                        InputSourceEnum.USER_QUERY,
                        InputSourceEnum.USER_INFO,
                        InputSourceEnum.REQUEST_INFO,
                        InputSourceEnum.CHAT_HISTORY

                )
                .priorityDefinitions(
                        STR."**全局目标**：`\{InputSourceEnum.USER_QUERY.xmlTagContent}`定义了整个任务的最总目标"
                )
                .interactionInterfaceEnd()
                //第三层内部处理
                .internalProcess()
                .capabilityMatrixMethod(this.tools)
//                .workflowDesign(
//                        STR."""
//                                ### 核心任务示例
//                                >你必须学习并模仿以下示例来执行任务
//                                #### 示例1：[执行一个需要工具的成功任务]
//                                - **用户输入**：
//                                    \{InputSourceEnum.USER_QUERY.createXml("帮我生成历史八年级上册的期末试卷，难度中等满分一百，题型自由发挥。")}
//                                - **你的行动(Your Action)**:
//                                    1. 用户需要历史八年级上册的期末试卷。
//                                    2. 我需要检索历史八年级上册全部章节的内容。
//                                    3. 根据全部章节内容，生成知识点。
//                                    4. 根据用户要求设计试卷架构。
//                                    5. 把试卷架构和知识点给到出题工具。
//                                    6. 拿到试卷内容，对试卷进行初筛，修改错误，整理并返回给用户。
//                                #### 示例2：[处理一个超出能力范围的请求]
//                                - **用户输入**：
//                                    \{InputSourceEnum.USER_QUERY.createXml("帮我查询一下明天的天气。")}
//                                - **你的行动(Your Action)**:
//                                    1. 用户想查询天气。
//                                    2. 我的能力模块中没有查询天气的工具。
//                                    3. 这超出了我的能力范围，我必须触发“求助机制”。
//                                """
//                )
                .internalProcessEnd()
                .constraintSetting()
                .hardRules(
                        STR."\{PromptShotConstants.MUST_NEVER}不要将自己的思考步骤输出给用户，直接解决用户的请求或任务。",
                        STR."\{PromptShotConstants.MUST_NEVER}不要将工具调用过程输出给用户。",
                        STR."\{PromptShotConstants.MUST_NEVER}捏造事实或提供未经证实的信息。如果你不知道答案，就明确说“我不知道”。",
                        STR."\{PromptShotConstants.MUST_NEVER}违反你在 `核心定义` 中设定的角色和立场。当规则冲突时，以你的核心身份作为最终决策依据。"
                )
                .helpMechanism(
                        "**触发条件**: 当你无法理解用户请求，或请求超出你的能力范围时。",
                        STR."**固定话术**: 你 \{PromptShotConstants.MUST} 回应：“我无法完成您的请求，因为[简明原因]。我的核心能力是[能力1]和[能力2]。您可以尝试这样问我：‘...’”"
                )
                .constraintSettingEnd();
        wwhPromptEngine.setModel(Map.of(
                InputSourceEnum.USER_QUERY.name(),
                req.getMessage().getContent(),
                InputSourceEnum.USER_INFO.name(),
                req.getSysCacheUserDto().toString(),
                InputSourceEnum.REQUEST_INFO.name(),
                req.toString(),
                InputSourceEnum.CHAT_HISTORY.name(),
                req.getMessages().stream().map(m -> STR."\{m.getRole()}:\{m.getContent()}").collect(Collectors.joining(System.lineSeparator()))
        ));
        return wwhPromptEngine.toPrompt();
    }
}