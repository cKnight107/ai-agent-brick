package com.agent.brick.ai.agent.enums;

import com.agent.brick.ai.agent.*;
import com.agent.brick.constants.AgentConstants;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;

/**
 * agent 池
 * @since 2025/6/21
 *
 * @author cKnight
 */
@AllArgsConstructor
public enum AgentEnum {
    QUESTION_AGENT(AgentConstants.QUESTION_AGENT_NAME, QuestionAgent.class,true),
    OWL_USER_AGENT(AgentConstants.OWL_USER_AGENT, OwlUserAgent.class,false),
    OWL_ASSISTANT_AGENT(AgentConstants.OWL_ASSISTANT_AGENT, OwlAssistantAgent.class,false),
    OWL_TASK_SPECIFIED_AGENT(AgentConstants.OWL_TASK_SPECIFIED_AGENT, OwlTaskSpecifiedAgent.class,false),
    OWL_TASK_PLANNER_AGENT(AgentConstants.OWL_TASK_PLANNER_AGENT, OwlTaskPlannerAgent.class,false),
    TITLE_AGENT(AgentConstants.TITLE_AGENT, TitleAgent.class,false),
    RAG_AGENT(AgentConstants.RAG_AGENT, RagAgent.class,false),

    ;
    public final String name;
    public final Class<? extends AbstractAgent> clazz;
    /** 是否工具使用 */
    public final boolean toolUse;

    public <T extends AbstractAgent> T getAgent(){
        return (T) SpringContextUtils.getBean(this.name, this.clazz);
    }
}
