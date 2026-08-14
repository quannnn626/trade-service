package com.boot.pay.auth.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.account.enums.RealNameAuthEnum;
import com.boot.pay.auth.JwtTokenUtil;
import com.boot.pay.auth.dto.LoginDTO;
import com.boot.pay.auth.dto.RefreshDTO;
import com.boot.pay.auth.dto.RegisterDTO;
import com.boot.pay.auth.service.AuthService;
import com.boot.pay.auth.vo.LoginVO;
import com.boot.pay.domain.AuthUser;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.AuthUserMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    private final AuthUserMapper authUserMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final PayUserAccountMapper payUserAccountMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查用户
        AuthUser user = authUserMapper.selectOne(
                new QueryWrapper<AuthUser>().eq("username", dto.getUsername()));
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        // 2. 验密
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        // 3. 生成双 token
        String accessToken = jwtTokenUtil.generateAccessToken(
                user.getId(), user.getUsername(), user.getNickname());
        String refreshToken = jwtTokenUtil.generateRefreshToken();
        // 4. refreshToken 存 Redis
        stringRedisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken, jwtTokenUtil.getRefreshExpiration(), TimeUnit.MILLISECONDS);
        // 5. 返回
        return new LoginVO(accessToken, refreshToken,
                user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 校验用户名是否已存在
        boolean exists = authUserMapper.exists(
                new QueryWrapper<AuthUser>().eq("username", dto.getUsername()));
        if (exists) {
            throw new BusinessException("用户名已被注册");
        }
        // 组装用户对象并入库
        AuthUser user = new AuthUser();
        user.setUserNo("UR" + IdUtil.getSnowflakeNextIdStr());
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        authUserMapper.insert(user);

        // 自动创建钱包账户，与用户注册同一事务
        String accountNo = "UA" + DateUtil.format(new Date(), "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);
        PayUserAccount account = new PayUserAccount();
        account.setUserId(user.getId());
        account.setAccountNo(accountNo);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setStatus(AccountStatusEnum.NORMAL.getCode());
        account.setVersion(0);
        account.setTotalIncome(BigDecimal.ZERO);
        account.setTotalExpense(BigDecimal.ZERO);
        account.setRealNameAuth(RealNameAuthEnum.UNREAL.getCode());
        account.setDailyLimit(AccountConstants.DAILY_LIMIT_UNREAL_NAME);
        account.setDailyUsed(BigDecimal.ZERO);
        payUserAccountMapper.insert(account);

        log.info("用户注册成功并自动开户 userId={} userNo={} accountNo={}",
                user.getId(), user.getUserNo(), accountNo);
    }

    @Override
    public String refresh(RefreshDTO dto) {
        String key = REFRESH_TOKEN_PREFIX + dto.getUserId();
        String savedToken = stringRedisTemplate.opsForValue().get(key);
        if (savedToken == null || !savedToken.equals(dto.getRefreshToken())) {
            throw new BusinessException(401, "refreshToken 无效或已过期，请重新登录");
        }
        AuthUser user = authUserMapper.selectById(dto.getUserId());
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(401, "用户已被禁用");
        }
        String newAccessToken = jwtTokenUtil.generateAccessToken(
                user.getId(), user.getUsername(), user.getNickname());
        // 续期
        stringRedisTemplate.expire(key, jwtTokenUtil.getRefreshExpiration(), TimeUnit.MILLISECONDS);
        return newAccessToken;
    }

    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}
