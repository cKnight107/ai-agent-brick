package com.agent.brick.ai.tools;

import com.agent.brick.constants.DatePattern;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * ai 通用工具类
 * @since 2025/6/7
 *
 * @author cKnight
 */
@Component
public class AiTools {


    @Tool(description = "获取当前时间，格式:yyyy-MM-dd HH:mm:ss")
    String getCurrentDateTime(){
        return FastDateFormat.getInstance(DatePattern.YMDHMS_1).format(new Date());
    }
}
