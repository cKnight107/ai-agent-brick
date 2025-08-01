package com.agent.brick.compant;

import com.agent.brick.ai.agent.AbstractAgent;
import com.agent.brick.ai.agent.enums.AgentEnum;
import com.agent.brick.util.SpringContextUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * ai组件
 * </p>
 *
 * @author cKnight
 * @since 2025/7/14
 */
@Component
public class AiComponent {

    /**
     * 获取所有工具类的智能体
     * @return List<T>
     * @param <T> agent
     */
    public <T extends AbstractAgent> List<T> getAllToolAgent(){
        return Arrays.stream(AgentEnum.values())
                .map(obj-> {
                    if (obj.toolUse){
                        AbstractAgent bean = SpringContextUtils.getBean(obj.name, obj.clazz);
                        if (Objects.nonNull(bean)) {
                            return (T) bean;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
