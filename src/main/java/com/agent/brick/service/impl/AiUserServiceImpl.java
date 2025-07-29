package com.agent.brick.service.impl;

import com.agent.brick.base.BaseService;
import com.agent.brick.compant.RedisCacheComponent;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.constants.TimeConstant;
import com.agent.brick.controller.request.AiUserReq;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.enums.CacheKeyEnum;
import com.agent.brick.enums.RoleCodeEnum;
import com.agent.brick.exception.BizException;
import com.agent.brick.mapper.AiUserMapper;
import com.agent.brick.model.AiRole;
import com.agent.brick.model.AiUser;
import com.agent.brick.model.AiUserRoleRel;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.pojo.vo.AiUserVO;
import com.agent.brick.service.AiRoleService;
import com.agent.brick.service.AiUserRoleRelService;
import com.agent.brick.service.AiUserService;
import com.agent.brick.util.*;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author autoCode
 * @since 2025-06-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiUserServiceImpl extends BaseService implements AiUserService {

    private final AiUserMapper aiUserMapper;

    private final AiUserRoleRelService aiUserRoleRelService;

    private final RedisCacheComponent redisCacheComponent;

    private final AiRoleService aiRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void createUser(AiUserReq userReq) {
        CommonUtils.checkArgs(userReq.getAccountNo(),userReq.getName(),userReq.getPassword(),userReq.getRoleIds());
        //用户账户校验
        checkUnique(
                where(aiUserMapper).eq(AiUser::getAccountNo,userReq.getAccountNo()),
                BizCodeEnum.SYS_USER_EXIST_ERROR
        );
        String salt = GlobalConstants.SECRET + RandomUtils.generateUUID();
        String pwd = SecurityUtils.MD5(userReq.getPassword(),salt);
        AiUser user = AiUser.builder()
                .name(userReq.getName())
                .password(pwd)
                .accountNo(userReq.getAccountNo())
                .salt(salt).build();
        //入库
        aiUserMapper.insert(user);
        //角色关联入库
        aiUserRoleRelService.createUserRole(user.getId(),userReq.getRoleIds());
        return null;
    }

    @Override
    public AiUserVO login(AiUserReq userReq) {
        CommonUtils.checkArgs(userReq.getAccountNo(),userReq.getPassword());
        AiUser user = where(aiUserMapper).eq(AiUser::getAccountNo, userReq.getAccountNo()).one();
        //校验登录
        checkLogin(user,userReq);
        AiUserVO result = ConvertUtils.beanProcess(user, AiUserVO.class);
        //处理用户角色
        processUserRole(user,result);
        //生成token
        String token = genToken(user);
        result.setToken(token);
        //放入redis
        String key = CacheKeyEnum.format(CacheKeyEnum.ADMIN_LOGIN_KEY,user.getId(),token);
        SysCacheUserDto sysCacheUserDto = ConvertUtils.beanProcess(result, SysCacheUserDto.class);
        //删除原始token
        removeToken(user.getId());
        redisCacheComponent.set(key,JSONObject.toJSONString(sysCacheUserDto), TimeConstant.ONE_DAY * 12);
        return result;
    }

    private void removeToken(Long userId){
        //模糊删除
        String key = CacheKeyEnum.format(CacheKeyEnum.ADMIN_LOGIN_KEY, userId, "*");
        redisCacheComponent.removeLike(key);
    }

    private String genToken(AiUser user) {
        JSONObject data = JSONUtils.builder().put("id", user.getId()).put("uuid", RandomUtils.generateUUID()).build();
        return SecurityUtils.AESEncryptJSON(data);
    }

    private void processUserRole(AiUser user, AiUserVO result) {
        List<AiUserRoleRel> aiUserRoleRels = aiUserRoleRelService.queryListByUserId(user.getId());
        if (CollectionUtils.isNotEmpty(aiUserRoleRels)) {
            List<AiRole> aiRoles = aiRoleService.queryListByIds(ConvertUtils.toFieldList(aiUserRoleRels, AiUserRoleRel::getRoleId));
            result.setRoleList(RoleCodeEnum.valueList(ConvertUtils.toFieldList(aiRoles,AiRole::getCode)));
        }
    }

    private void checkLogin(AiUser user, AiUserReq userReq) {
        if (Objects.isNull(user)) {
            throw new BizException(BizCodeEnum.SYS_USER_LOGIN_ERROR);
        }
        //校验状态
        if (!user.getStatus()){
            throw new BizException(BizCodeEnum.SYS_USER_STATUS_OFF_ERROR);
        }
        //校验密码
        if (!SecurityUtils.MD5(userReq.getPassword(),user.getSalt()).equals(user.getPassword())) {
            throw new BizException(BizCodeEnum.SYS_USER_LOGIN_ERROR);
        }
    }
}
