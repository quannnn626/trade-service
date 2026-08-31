package com.boot.pay.account.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.account.dto.FreezeAccountDTO;
import com.boot.pay.account.dto.RealNameAuthDTO;
import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.dto.UnfreezeAccountDTO;
import com.boot.pay.account.vo.AccountListVO;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.service.PayUserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* @author quannnn
* @description 用户账户接口
* @createDate 2026-08-13
*/
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final PayUserAccountService payUserAccountService;

    /**
     * 当前登录用户查询自己的账户
     */
    @GetMapping("/my")
    public Result<AccountVO> my(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(payUserAccountService.getMyAccount(userId));
    }

    /**
     * 管理后台按账户编号查询账户
     */
    @GetMapping("/{accountNo}")
    public Result<AccountVO> detail(@PathVariable String accountNo) {
        return Result.success(payUserAccountService.getByAccountNo(accountNo));
    }

    /**
     * 用户账户分页列表（运营后台）
     * 筛选：账户号（模糊）、用户名/手机号（模糊，查 auth_user 后转 userId）
     */
    @GetMapping("/list")
    public Result<IPage<AccountListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String accountNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone) {
        return Result.success(payUserAccountService.listPage(page, pageSize, accountNo, username, phone));
    }

    /**
     * 设置/修改支付密码
     */
    @PostMapping("/set-pay-password")
    public Result<Void> setPayPassword(@Valid @RequestBody SetPayPasswordDTO dto,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        payUserAccountService.setPayPassword(userId, dto);
        return Result.success();
    }

    /**
     * 实名认证
     */
    @PostMapping("/real-name-auth")
    public Result<Void> realNameAuth(@Valid @RequestBody RealNameAuthDTO dto,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        payUserAccountService.realNameAuth(userId, dto);
        return Result.success();
    }

    /**
     * 冻结账户资金（amount 为空则冻结全部可用余额）
     */
    @PostMapping("/freeze")
    public Result<BigDecimal> freeze(@RequestBody(required = false) FreezeAccountDTO dto,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal amount = dto == null ? null : dto.getAmount();
        return Result.success(payUserAccountService.freeze(userId, amount));
    }

    /**
     * 解冻账户资金（amount 必填，且不能超过已冻结金额）
     */
    @PostMapping("/unfreeze")
    public Result<BigDecimal> unfreeze(@Valid @RequestBody UnfreezeAccountDTO dto,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(payUserAccountService.unfreeze(userId, dto.getAmount()));
    }

    /**
     * 启用账户（运营后台，账户状态置为正常）
     */
    @PutMapping("/{accountNo}/enable")
    public Result<Void> enable(@PathVariable String accountNo) {
        payUserAccountService.enable(accountNo);
        return Result.success();
    }

    /**
     * 禁用账户（运营后台，账户状态置为冻结，禁用后支付/充值被拒）
     */
    @PutMapping("/{accountNo}/disable")
    public Result<Void> disable(@PathVariable String accountNo) {
        payUserAccountService.disable(accountNo);
        return Result.success();
    }
}
