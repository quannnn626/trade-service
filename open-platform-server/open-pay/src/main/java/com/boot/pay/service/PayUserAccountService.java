package com.boot.pay.service;

import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.domain.PayUserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayUserAccountService extends IService<PayUserAccount> {

    /**
     * 当前登录用户查询自己的账户
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    AccountVO getMyAccount(Long userId);

    /**
     * 按账户编号查询账户（管理后台用）
     *
     * @param accountNo 账户编号
     * @return 账户信息
     */
    AccountVO getByAccountNo(String accountNo);

    /**
     * 设置或修改支付密码（首次设置无需原密码，修改需验证原密码）
     *
     * @param userId 用户ID
     * @param dto    支付密码请求
     */
    void setPayPassword(Long userId, SetPayPasswordDTO dto);
}
