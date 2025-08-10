package com.agent.brick.ai.prompt;

import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Prompt toPrompt() {
        return null;
    }

    @Override
    public String toMarkdown() {
        return null;
    }

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
