package com.agent.brick.ai.agent;

import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.enums.BooleanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

/**
 * <p>
 * RAG 知识检索智能体
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@Slf4j
public class RagAgent extends AbstractAgent{
    public static Builder<RagAgent> builder(){return new Builder<>(RagAgent.class);}

    private final String UNKNOW = "抱歉，暂无法检索到答案";
    /**
     * 执行检索 单次流程
     * @param query 问题
     * @return 答案
     */
    @Tool(description = "检索知识库，若检索到则返回优化后的答案")
    public String doRetrieve(
            @ToolParam(description = "需要检索的问题或者关键字") String query
    ){
        log.info("执行检索,问题:{}",query);
        return doCall(query);
        //初始检索
//        String content = doCall(query);
        //对检索问题进行评分
//        if (gradeResult(content,query)){
//            //返回最终答案
//            log.info("执行检索,直接返回答案,问题:{}",query);
//            return content;
//        }else {
//           //对问题进行重写
//            query = rewrite(query);
//            log.info("执行检索,对问题进行重写,问题:{}",query);
//            //直接改写后的返回情况
//            return doCall(query);
////            if (gradeResult(content,query)){
////                log.info("执行检索,对问题进行重写后返回答案,问题:{}",query);
////                return content;
////            }else {
////                log.info("执行检索,对问题进行重写未检索到答案,问题:{}",query);
////                return UNKNOW;
////            }
//        }
    }

    public String doCall(String query){
        return this.chatClient().user(query).call().content();
    }

    public String rewrite(String query) {
        return ChatClient.create(this.chatModel)
                .prompt(
                        PromptConstants.REWRITE_RETRIEVE_QUESTION_TEMPLATE.create(
                                Map.of(GlobalConstants.QUERY, query)
                        )
                )
                .call()
                .content();
    }

    private String answer(String content, String query) {
        return ChatClient.create(this.chatModel)
                .prompt(
                        PromptConstants.GENERATE_RETRIEVE_ANSWER_TEMPLATE.create(
                                Map.of(GlobalConstants.CONTEXT, content, GlobalConstants.QUERY, query)
                        )
                )
                .call()
                .content();
    }

    public boolean gradeResult(String content, String query) {
        String res = ChatClient.create(this.chatModel)
                .prompt(
                        PromptConstants.GRADE_RETRIEVE_TEMPLATE.create(
                                Map.of(GlobalConstants.CONTEXT, content, GlobalConstants.QUERY, query)
                        )
                )
                .call()
                .content();
        return res.contains(BooleanEnum.YES.name().toLowerCase());
    }
}
