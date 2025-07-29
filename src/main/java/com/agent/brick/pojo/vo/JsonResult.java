package com.agent.brick.pojo.vo;

import com.agent.brick.enums.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应类
 * @author cKnight
 * @author cKnight
 * @since 2024/6/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonResult<T> {
    /**
     * 状态码 0 表示成功，1表示处理中，-1表示失败
     */
    private Integer code;
    /**
     * 数据
     */
    private T data;
    /**
     * 描述
     */
    private String msg;


    /**
     * 构建一个表示操作成功的JsonResult对象。
     * 该方法返回一个默认的成功响应，其中包含成功状态码，且数据和消息字段为null。
     *
     * @param <T> 泛型类型，表示返回结果中数据的类型
     * @return JsonResult<T> 包含成功状态码的JsonResult对象，数据和消息字段为null
     */
    public static <T> JsonResult<T> buildSuccess() {
        return new JsonResult<>(BizCodeEnum.SUCCESS.getCode(), null, null);
    }

    /**
     * 构建一个成功的JsonResult对象。
     * <p>
     * 该方法用于创建一个表示操作成功的JsonResult实例，其中包含成功状态码、返回的数据以及空错误信息。
     *
     * @param <T> 返回数据的类型
     * @param data 要返回的数据对象
     * @return 返回一个包含成功状态码、数据和空错误信息的JsonResult对象
     */
    public static <T> JsonResult<T> buildSuccess(T data) {
        return new JsonResult<>(BizCodeEnum.SUCCESS.getCode(), data, null);
    }

    /**
     * 构建一个表示错误的JsonResult对象。
     * <p>
     * 该方法用于快速创建一个包含错误信息的JsonResult对象，通常用于业务逻辑处理失败时返回给前端。
     *
     * @param <T> 泛型类型，表示JsonResult中数据的类型。
     * @param msg 错误信息，用于描述失败的原因。
     * @return 返回一个JsonResult对象，其中包含错误码、空数据以及错误信息。
     */
    public static <T> JsonResult<T> buildError(String msg) {
        return new JsonResult<>(BizCodeEnum.FAIL.getCode(), null, msg);
    }


    /**
     * 构建一个包含指定状态码和消息的JsonResult对象。
     * 该函数通常用于返回一个不包含具体数据，但包含状态码和消息的响应结果。
     *
     * @param <T> 泛型类型，表示JsonResult中数据的类型。由于该函数不返回具体数据，因此类型参数在此处未使用。
     * @param code 状态码，用于表示操作的结果状态（如成功、失败等）。
     * @param msg 消息，用于描述操作结果的详细信息或错误信息。
     * @return 返回一个JsonResult对象，包含指定的状态码和消息，数据部分为null。
     */
    public static <T> JsonResult<T> buildCodeAndMsg(int code, String msg) {
        return new JsonResult<>(code, null, msg);
    }

    /**
     * 根据指定的业务代码枚举构建一个JsonResult对象。
     * <p>
     * 该函数通过传入的BizCodeEnum枚举对象，获取其对应的业务代码和消息，
     * 并调用JsonResult的静态方法buildCodeAndMsg来构建并返回一个JsonResult对象。
     *
     * @param <T> JsonResult中数据的泛型类型
     * @param codeEnum 业务代码枚举，包含业务代码和消息
     * @return 返回一个包含指定业务代码和消息的JsonResult对象
     */
    public static <T> JsonResult<T> buildResult(BizCodeEnum codeEnum) {
        return JsonResult.buildCodeAndMsg(codeEnum.getCode(), codeEnum.getMsg());
    }
}
