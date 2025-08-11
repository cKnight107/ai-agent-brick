package com.agent.brick.ai.prompt;

import com.agent.brick.ai.prompt.constants.PromptShotConstants;
import com.agent.brick.ai.prompt.enums.InputSourceEnum;
import com.agent.brick.ai.prompt.record.PromptToolDefinition;
import com.agent.brick.util.AiUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h2>
 * WWH prompt引擎。
 * </h2>
 * <p>含义解释</p>
 * <ul>
 *     <li>
 *         我是谁(Who)？ -> 角色定位：定义系统的身份、服务主体与边界。
 *     </li>
 *     <li>
 *         我该做什么(What)？-> 目标定义：建立系统的核心使命与价值主张。
 *     </li>
 *     <li>
 *         我该怎么做(How)？ -> 能力与流程：规划系统实现目标的具体路径和方法。
 *     </li>
 * </ul>
 * <p>层级介绍</p>
 * <ol>
 *     <li>第一层：核心定义: 定义系统的内核——我是谁，我为何存在？</li>
 *     <li>第二层：交互接口: 定义系统与外部世界的沟通方式——我如何感知世界，又如何被世界感知？</li>
 *     <li>第三层：内部处理: 定义系统的“思考”与“行动”逻辑——我如何一步步完成任务？</li>
 *     <li>第四层：全局约束: 定义系统不可逾越的边界——我绝对不能做什么？</li>
 * </ol>
 * <p>文章链接：</p>
 * <a href='https://www.bestblogs.dev/article/9d613b'>bestblogs</a>
 * <a href='https://mp.weixin.qq.com/mp/wappoc_appmsgcaptcha?poc_token=HBklk2ijK9pQ3qAH_ONFjBenSAPcA2b1Crn6oqzA&target_url=https%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzIzOTU0NTQ0MA%3D%3D%26mid%3D2247551851%26idx%3D1%26sn%3D56c996d61163f1acba5b16eace33c511'>微信公众号原文</a>
 *
 * @author cKnight
 * @since 2025/8/6
 */
public class WwhPromptEngine extends AbstractPromptEngine {
    /**
     * 核心定义
     */
    protected CoreDefinition coreDefinition;
    /**
     * 交互接口
     */
    protected InteractionInterface interactionInterface;
    /**
     * 内部处理
     */
    protected InternalProcess internalProcess;
    /**
     * 约束设定
     */
    protected ConstraintSetting constraintSetting;


    /** 提示词变量 */
    @Getter
    @Setter
    private Map<String,Object> model = new HashMap<>();

    @Override
    public String getMarkdown() {
        return STR."""
                # 第一层：核心定义 (CORE DEFINITION)
                ## 角色建模 (Role Modeling)
                > 描述AI的身份、人格和立场。这是所有行为的基石。
                - **身份 (Identity)**：\{this.coreDefinition.identity()}
                - **人格 (Personality)**：\{this.coreDefinition.personality()}
                - **立场 (Stance)**：\{this.coreDefinition.stance()}
                ## 目标定义 (Goal Definition)
                > 描述AI的核心使命、价值主张和成功的标准。
                - **功能性目标 (Functional Goals)**:
                \{joinList(this.coreDefinition.functionalGoals())}
                - **价值性目标 (Value Goals)**:
                \{joinList(this.coreDefinition.valuesGoals())}
                - **质量标准/红线 (Quality Standards / Red Lines)**:
                \{joinAndSerialNumList(this.coreDefinition.qualityStandards(),"  - **标准%s**：")}
                \{joinAndSerialNumList(this.coreDefinition.readLine(),"  - **红线%s**：")}
                # 第二层：交互接口 (Interaction Interface)
                ## 输入规范 (Input Specification)
                > 定义AI如何感知和理解外部信息。
                - **输入源识别 (Input Sources)**:
                \{joinList(this.interactionInterface.inputSources().stream().map(obj->STR."`\{obj.xmlOn()}`：\{obj.description}").toList())}
                - **优先级定义 (Priority Definition)**:
                \{joinList(this.interactionInterface.priorityDefinitions())}
                - **安全过滤 (Security Filtering)**:
                \{joinList(this.interactionInterface.securityFiltering())}
                ## 输出规格 (Output Specification)
                > 定义AI的交付物格式，实现内容与表现的分离。
                - **响应结构 (Response Structure)**:
                \{joinList(this.interactionInterface.responseStructure())}
                - **格式化规则 (Formatting Rules)**:
                \{joinList(this.interactionInterface.formattingRules())}
                - **禁用项清单 (Prohibited Elements)**:
                \{joinList(this.interactionInterface.prohibitedElements())}
                ## 输入源数据
                \{this.interactionInterface.inputSources().stream().map(InputSourceEnum::createXml).collect(Collectors.joining(System.lineSeparator()))}
                # 第三层：内部处理 (Internal Process)
                ## 工具与能力模块 (TOOLS & CAPABILITY MODULES)
                > 以下是你可用的工具集。每个模块都定义了工具的完整功能和使用它的核心规则。
                \{this.internalProcess.capabilityMatrix().stream().map(obj->STR."---\n\{obj}\n").collect(Collectors.joining(System.lineSeparator()))}
                \{this.internalProcess.workflowDesign()}
                # 第四层：全局约束 (GLOBAL CONSTRAINTS)
                > 以下规则拥有最高执行优先级，在任何情况下都必须遵守。当这些规则与任何其他部分的指令发生冲突时，你必须以这里的规则为准。
                ### 行为边界与硬性规则 (Behavioral Boundaries & Hard Rules)
                - **硬性规则**:
                \{joinList(this.constraintSetting.hardRules())}
                - **求助机制 (Help Mechanism)**:
                \{joinList(this.constraintSetting.helpMechanism())}
                """;
    }

    public static WwhPromptEngine begin(){
        return new WwhPromptEngine();
    }

    public CoreDefinition.Builder coreDefinition() {
        return new CoreDefinition.Builder(this);
    }

    public InteractionInterface.Builder interactionInterface() {
        return new InteractionInterface.Builder(this);
    }

    public InternalProcess.Builder internalProcess() {
        return new InternalProcess.Builder(this);
    }

    public ConstraintSetting.Builder constraintSetting() {
        return new ConstraintSetting.Builder(this);
    }

    /**
     * <h3>一、核心定义</h3>
     * <p>1、角色建模</p>
     * <p>身份（Identity）：你是 [AI名称]，一个 [AI的核心定位，例如：由XX公司开发的专家级数据分析AI]。</p>
     * <p>人格（Personality）：你的沟通风格是 [形容词，例如：专业、严谨、客观、简洁]。你对待用户的态度是 [形容词，例如：耐心、乐于助人]。</p>
     * <p>立场（Stance）：在 [某个关键领域，例如：数据隐私] 方面，你的立场是 [采取的策略，例如：永远将用户数据安全和匿名化放在首位]。</p>
     * <p>2、目标定义</p>
     * <p>功能性目标 (Functional Goals)：[目标1，例如：根据用户请求，生成准确的SQL查询] [目标2，例如：将查询结果可视化为图表]</p>
     * <p>价值性目标 (Value Goals)：[价值1，例如：为非技术用户降低数据分析的门槛]</p>
     * <p>质量标准 (Quality Standards)：[标准1，例如：生成的所有代码都必须包含注释]</p>
     * <p>红线（Red Lines）：[红线1，例如：绝不提供财务投资建议]</p>
     *
     * @param identity         身份
     * @param personality      人格
     * @param stance           立场
     * @param functionalGoals  功能性目标
     * @param valuesGoals      价值性目标
     * @param qualityStandards 质量标准
     * @param readLine         红线
     */
    public record CoreDefinition(String identity, String personality, String stance, List<String> functionalGoals,
                                 List<String> valuesGoals, List<String> qualityStandards, List<String> readLine) {
        public static class Builder {
            private final WwhPromptEngine wwhPromptEngine;
            private String identity;
            private String personality;
            private String stance;
            private List<String> functionalGoals = new ArrayList<>();
            private List<String> valuesGoals = new ArrayList<>();
            private List<String> qualityStandards = new ArrayList<>();
            private List<String> readLine = new ArrayList<>();

            protected Builder(WwhPromptEngine wwhPromptEngine) {
                this.wwhPromptEngine = wwhPromptEngine;
            }

            public Builder identity(String identity) {
                this.identity = identity;
                return this;
            }

            public Builder personality(String personality) {
                this.personality = personality;
                return this;
            }

            public Builder stance(String stance) {
                this.stance = stance;
                return this;
            }

            public Builder functionalGoals(List<String> functionalGoals) {
                this.functionalGoals = functionalGoals;
                return this;
            }

            public Builder functionalGoals(String... functionalGoals) {
                this.functionalGoals = Arrays.asList(functionalGoals);
                return this;
            }

            public Builder valuesGoals(List<String> valuesGoals) {
                this.valuesGoals = valuesGoals;
                return this;
            }

            public Builder valuesGoals(String... valuesGoals) {
                this.valuesGoals = Arrays.asList(valuesGoals);
                return this;
            }

            public Builder qualityStandards(List<String> qualityStandards) {
                this.qualityStandards = qualityStandards;
                return this;
            }

            public Builder qualityStandards(String... qualityStandards) {
                this.qualityStandards = Arrays.asList(qualityStandards);
                return this;
            }

            public Builder readLine(List<String> readLine) {
                this.readLine = readLine;
                return this;
            }

            public Builder readLine(String... readLine) {
                this.readLine = Arrays.asList(readLine);
                return this;
            }

            public WwhPromptEngine coreDefinitionEnd() {
                this.wwhPromptEngine.coreDefinition = new CoreDefinition(identity, personality, stance, functionalGoals, valuesGoals, qualityStandards, readLine);
                return wwhPromptEngine;
            }
        }
    }

    /**
     * <h3>二、交互接口</h3>
     * <p>1、输入规范 </p>
     * <p>输入源识别（Input Sources）</p>
     * <p>优先级定义（Priority Definition）</p>
     * <p>安全过滤（Security Filtering）</p>
     * <p>2、输出格式 </p>
     * <p>响应结构 (Response Structure)：[结构描述，例如：一个标准响应应包含以下部分，并按此顺序排列：1. `[洞察总结]` 2. `[SQL查询块]` 3. `[数据可视化图表]` 4. `[方法论解释]`]</p>
     * <p>格式化规则 (Formatting Rules)：[规则1，例如：所有SQL代码必须包裹在 ` ```sql ` 代码块中。]</p>
     * <p>禁用项清单 (Prohibited Elements)：[禁用项1，例如：禁止使用任何Emoji表情符号。]</p>
     *
     * @param inputSources        输入源识别
     * @param priorityDefinitions 优先级定义
     * @param securityFiltering   安全过滤
     * @param responseStructure   响应结构
     * @param formattingRules     格式化规则
     * @param prohibitedElements  禁用项清单
     */
    public record InteractionInterface(List<InputSourceEnum> inputSources, List<String> priorityDefinitions,
                                       List<String> securityFiltering, List<String> responseStructure,
                                       List<String> formattingRules, List<String> prohibitedElements) {
        public static class Builder {
            private final WwhPromptEngine wwhPromptEngine;
            private List<InputSourceEnum> inputSources = new ArrayList<>();
            private List<String> priorityDefinitions = new ArrayList<>();
            private List<String> securityFiltering = new ArrayList<>();
            private List<String> responseStructure = new ArrayList<>();
            private List<String> formattingRules = new ArrayList<>();
            private List<String> prohibitedElements = new ArrayList<>();

            protected Builder(WwhPromptEngine wwhPromptEngine) {
                this.wwhPromptEngine = wwhPromptEngine;
            }

            public Builder inputSources(List<InputSourceEnum> inputSources) {
                this.inputSources = inputSources;
                return this;
            }

            public Builder inputSources(InputSourceEnum... inputSources) {
                this.inputSources = Arrays.asList(inputSources);
                return this;
            }

            public Builder priorityDefinitions(List<String> priorityDefinitions) {
                this.priorityDefinitions = priorityDefinitions;
                return this;
            }

            public Builder priorityDefinitions(String... priorityDefinitions) {
                this.priorityDefinitions = Arrays.asList(priorityDefinitions);
                return this;
            }

            public Builder securityFiltering(List<String> securityFiltering) {
                this.securityFiltering = securityFiltering;
                return this;
            }

            public Builder securityFiltering(String... securityFiltering) {
                this.securityFiltering = Arrays.asList(securityFiltering);
                return this;
            }

            public Builder responseStructure(List<String> responseStructure) {
                this.responseStructure = responseStructure;
                return this;
            }

            public Builder responseStructure(String... responseStructure) {
                this.responseStructure = Arrays.asList(responseStructure);
                return this;
            }

            public Builder formattingRules(List<String> formattingRules) {
                this.formattingRules = formattingRules;
                return this;
            }

            public Builder formattingRules(String... formattingRules) {
                this.formattingRules = Arrays.asList(formattingRules);
                return this;
            }

            public Builder prohibitedElements(List<String> prohibitedElements) {
                this.prohibitedElements = prohibitedElements;
                return this;
            }

            public Builder prohibitedElements(String... prohibitedElements) {
                this.prohibitedElements = Arrays.asList(prohibitedElements);
                return this;
            }

            public WwhPromptEngine interactionInterfaceEnd() {
                this.wwhPromptEngine.interactionInterface =
                        new InteractionInterface(inputSources, priorityDefinitions, securityFiltering, responseStructure, formattingRules, prohibitedElements);
                return this.wwhPromptEngine;
            }
        }
    }

    /**
     * <h3>三、内部处理</h3>
     * <p>1、能力拆解（Capability Matrix） </p>
     * <p>2、流程设计（Workflow Design） </p>
     *
     * @param capabilityMatrix 能力拆解
     * @param workflowDesign   工作流设计
     */
    public record InternalProcess(List<String> capabilityMatrix, String workflowDesign) {
        public static class Builder {
            private final WwhPromptEngine wwhPromptEngine;
            private final List<String> capabilityMatrix = new ArrayList<>();
            private String workflowDesign;

            protected Builder(WwhPromptEngine wwhPromptEngine) {
                this.wwhPromptEngine = wwhPromptEngine;
            }

            public Builder capabilityMatrix(List<String> capabilityMatrix) {
                this.capabilityMatrix.addAll(capabilityMatrix);
                return this;
            }

            public Builder capabilityMatrix(String... capabilityMatrix) {
                this.capabilityMatrix.addAll(Arrays.asList(capabilityMatrix));
                return this;
            }

            /**
             * 解析对象处理为指定格式
             */
            public <T>  Builder capabilityMatrixMethod(List<T> capabilityMatrix) {
                for (PromptToolDefinition toolDefinition : AiUtil.getToolDefinition(capabilityMatrix)) {
                    //开始组装
                    this.capabilityMatrix.add(STR."""
                            ### `\{toolDefinition.name()}`
                            - \{PromptShotConstants.DESCRIPTION}：\{toolDefinition.description()}
                            - **参数（Parameters）**：
                            \{toolDefinition.parameters().stream().map(obj->STR."  - \{obj.name()}:（\{obj.required() ? "required" : "optional"}）\{obj.description()}").collect(Collectors.joining(System.lineSeparator()))}
                            - \{PromptShotConstants.RULES}
                            \{toolDefinition.rules().stream().map(obj-> STR."  - \{obj}").collect(Collectors.joining(System.lineSeparator()))}
                            """);
                }
                return this;
            }

            public Builder workflowDesign(String workflowDesign) {
                this.workflowDesign = workflowDesign;
                return this;
            }

            public WwhPromptEngine internalProcessEnd() {
                this.wwhPromptEngine.internalProcess = new InternalProcess(capabilityMatrix, workflowDesign);
                return this.wwhPromptEngine;
            }
        }
    }

    /**
     * <h3>三、约束设定</h3>
     * <p>1、硬性规则 (Hard Rules)：[规则1，例如：在任何情况下，都绝对禁止在生成的SQL中包含 `DROP TABLE` 或 `DELETE FROM` 指令。] </p>
     * <p>2、求助机制 (Help Mechanism)：**触发条件**: [例如：当用户意图无法解析，或请求的功能超出能力范围时。] **固定话术**: [例如：“我无法完成这个请求，因为[简明原因]。我能帮助您进行数据查询、可视化和洞察分析。您可以尝试这样问我：‘...’”]。 </p>
     *
     * @param hardRules     硬性规则
     * @param helpMechanism 求助机制
     */
    public record ConstraintSetting(List<String> hardRules, List<String> helpMechanism) {
        public static class Builder {
            private final WwhPromptEngine wwhPromptEngine;
            private List<String> hardRules = new ArrayList<>();
            private List<String> helpMechanism = new ArrayList<>();

            protected Builder(WwhPromptEngine wwhPromptEngine) {
                this.wwhPromptEngine = wwhPromptEngine;
            }

            public Builder hardRules(List<String> hardRules) {
                this.hardRules = hardRules;
                return this;
            }

            public Builder hardRules(String... hardRules){
                this.hardRules.addAll(Arrays.asList(hardRules));
                return this;
            }

            public Builder helpMechanism(List<String> helpMechanism){
                this.helpMechanism = helpMechanism;
                return this;
            }

            public Builder helpMechanism(String... helpMechanism){
                this.helpMechanism.addAll(Arrays.asList(helpMechanism));
                return this;
            }

            public WwhPromptEngine constraintSettingEnd(){
                this.wwhPromptEngine.constraintSetting = new ConstraintSetting(hardRules, helpMechanism);
                return this.wwhPromptEngine;
            }
        }
    }
}
