package com.boot.pay.account.controller;

import com.boot.common.result.Result;
import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.service.PayUserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 设置/修改支付密码
     */
    @PostMapping("/set-pay-password")
    public Result<Void> setPayPassword(@Valid @RequestBody SetPayPasswordDTO dto,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        payUserAccountService.setPayPassword(userId, dto);
        return Result.success();
    }
}
