package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.account.dto.RealNameAuthDTO;
import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.vo.AccountListVO;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.domain.PayUserAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import java.math.BigDecimal;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayUserAccountService extends IService<PayUserAccount> {

    /**
     * 为新注册用户自动创建钱包账户（注册流程调用，同一事务）
     *
     * @param userId 用户ID
     */
    void createAccountForUser(Long userId);

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
     * 用户账户分页列表（运营后台）
     * <p>
     * 筛选：账户号模糊匹配；用户名/手机号在 auth_user 表，先转 userId 集合再查账户表。
     *
     * @param page      页码
     * @param pageSize  每页条数
     * @param accountNo 账户编号（模糊）
     * @param username  用户名（模糊）
     * @param phone     手机号（模糊）
     * @return 分页列表
     */
    IPage<AccountListVO> listPage(Integer page, Integer pageSize, String accountNo, String username, String phone);

    /**
     * 设置或修改支付密码（首次设置无需原密码，修改需验证原密码）
     *
     * @param userId 用户ID
     * @param dto    支付密码请求
     */
    void setPayPassword(Long userId, SetPayPasswordDTO dto);

    /**
     * 实名认证（校验支付密码，模拟第三方校验，认证后日限额提升至50000）
     *
     * @param userId 用户ID
     * @param dto    实名认证请求
     */
    void realNameAuth(Long userId, RealNameAuthDTO dto);

    /**
     * 冻结账户资金（amount 为空则冻结全部可用余额，冻结后该笔资金不可用于支付）
     * <p>
     * 乐观锁（version）保证并发安全，同一事务内写冻结流水（flow_type=7）。
     *
     * @param userId 用户ID
     * @param amount 冻结金额（为空则冻结全部可用余额）
     * @return 本次冻结金额
     */
    BigDecimal freeze(Long userId, BigDecimal amount);

    /**
     * 解冻账户资金（amount 必填，且不能超过已冻结金额）
     * <p>
     * 乐观锁（version）保证并发安全，同一事务内写解冻流水（flow_type=8）。
     *
     * @param userId 用户ID
     * @param amount 解冻金额
     * @return 本次解冻金额
     */
    BigDecimal unfreeze(Long userId, BigDecimal amount);
}
