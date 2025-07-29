package com.agent.brick.ai.transformer;

import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.constants.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 关键词转换器
 * </p>
 *
 * @author cKnight
 * @since 2025/7/2
 */
@Slf4j
public class KeywordTransformer implements DocumentTransformer {
    /** LLM模型 */
    private final ChatModel chatModel;

    /** metadata 数据key*/
    private static final String EXCERPT_KEYWORDS_METADATA_KEY = "excerpt_keywords";

    public KeywordTransformer(ChatModel chatModel) {
        Assert.notNull(chatModel, "ChatModel must not be null");
        this.chatModel = chatModel;
    }

    @Override
    public List<Document> apply(List<Document> documents) {
        return null;
    }


    /**
     * 执行处理
     * @param documents 文档
     * @param count 关键词数量
     * @return
     */
    public List<Document> doApply(List<Document> documents,Integer count){
        for (Document document : documents) {
            try {
                Prompt prompt = PromptConstants.KEYWORD_TRANSFORMER_TEMPLATE.create(
                        Map.of(GlobalConstants.CONTEXT, document.getText(), GlobalConstants.NUM, count)
                );
                String text = this.chatModel.call(prompt).getResult().getOutput().getText();
                document.getMetadata().put(EXCERPT_KEYWORDS_METADATA_KEY,text);
            }catch (Exception e){
                log.error("关键词提取转换器处理,异常:{}",document);
            }
        }
        return documents;
    }

}
