package com.agent.brick.interceptor;

import com.agent.brick.compant.AuthComponent;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.annotation.Resource;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * mbp公共字段自动赋值
 * @author cKnight
 */
@Component
public class MybatisPlusMetaObjectInterceptor implements MetaObjectHandler {

    @Resource
    private AuthComponent authComponent;

    @Override
    public void insertFill(MetaObject metaObject) {
        SysCacheUserDto adminUserInfo = authComponent.getAdminUserInfo();
        if (Objects.nonNull(adminUserInfo)){
            this.setFieldValByName("createBy", adminUserInfo.getAccountNo(), metaObject);
            this.setFieldValByName("updateBy", adminUserInfo.getAccountNo(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        SysCacheUserDto adminUserInfo = authComponent.getAdminUserInfo();
        if (Objects.nonNull(adminUserInfo)){
            this.setFieldValByName("updateBy", adminUserInfo.getAccountNo(), metaObject);
        }
    }

}
