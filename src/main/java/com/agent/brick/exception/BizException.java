package com.agent.brick.exception;

import com.agent.brick.enums.BizCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 业务异常
 * @author cKnight
 * @since 2024/6/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BizException extends RuntimeException{
    private int code;
    private String msg;

    public BizException(int code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(BizCodeEnum codeEnum){
        super(codeEnum.getMsg());
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
    }

    public BizException(BizException bizException){
        super(bizException.getMsg());
        this.code = bizException.getCode();
        this.msg = bizException.getMsg();
    }
}
