package com.agent.brick.ai.prompt;

import com.agent.brick.util.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * prompt引擎抽象父类
 * </p>
 *
 * @author cKnight
 * @since 2025/8/6
 */
public abstract class AbstractPromptEngine implements PromptEngine{

    /** 提示词变量 */
    @Getter
    @Setter
    private Map<String,Object> model = new HashMap<>();

    /** 提示词文本 本地缓存 */
    @Getter
    private String template;

    /** 提示词指纹 */
    @Getter
    private String promptFingerprint;

    @Override
    public Prompt toPrompt() {
        return toPrompt(this.model);
    }

    /**
     * 显式调用，刷新缓存与指纹。
     * @return md string
     */
    @Override
    public String toMarkdown() {
        String markdown = getMarkdown();
        this.promptFingerprint = SecurityUtils.SHA256(markdown);
        this.template = markdown;
        return this.template;
    }

    @Override
    public Prompt toPrompt(Map<String, Object> model) {
        return MyPromptTemplate.start()
                .messageType(MessageType.SYSTEM)
                .template(this.template)
                .end()
                .create(model);
    }

    /** 子类实现 */
    protected abstract String getMarkdown();

    protected String joinAndSerialNumList(List<String> list){
        return joinAndSerialNumList(list,"- %s",System.lineSeparator());
    }

    protected String joinAndSerialNumList(List<String> list,String prefix){
        return joinAndSerialNumList(list,prefix,System.lineSeparator());
    }

    protected String joinAndSerialNumList(List<String> list,String prefix,String join){
        List<String> tempList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            tempList.add(prefix.formatted(i+1)+list.get(i));
        }
        return String.join(join, tempList);
    }

    protected String joinList(List<String> list){
        return joinList(list,"  - ");
    }

    protected String joinList(List<String> list,String prefix){
        return joinList(list,prefix,System.lineSeparator());
    }

    protected String joinList(List<String> list,String prefix,String join){
        return list.stream().map(obj->STR."\{prefix}\{obj}").collect(Collectors.joining(join));
    }
}
