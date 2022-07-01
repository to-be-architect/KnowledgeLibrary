package com.lib.service.user;

import com.github.pagehelper.PageInfo;
import com.lib.entity.UserInfo;
import com.lib.exception.user.UserException;
import com.lib.exception.user.UserNullAccountException;
import com.lib.exception.user.UserPasswordWrongException;

public interface UserService {
    /**
     * 邮箱登录验证用户密码是否正确
     *
     * @param user
     * @return
     */
    void checkUserByEmail(UserInfo user) throws UserException, UserPasswordWrongException, UserNullAccountException;

    /**
     * 根据id得到用户
     *
     * @param l
     * @return
     */
    UserInfo getUserById(long l);

    /**
     * 根据 userId 得到用户基本信息
     *
     * @param userId
     * @return
     */
    UserInfo getBasicUserInfo(Long userId);

    /**
     * 根据 email 得到用户基本信息
     *
     * @param userEmail
     * @return
     */
    UserInfo getBasicUserInfoByEmail(String userEmail);

    /**
     * 判断email是否已经被注册
     *
     * @param email
     * @return
     */
    public boolean checkByEmail(String email);

    /**
     * 修改用户信息
     *
     * @param user
     */
    public void updateUser(UserInfo user);

    /**
     * 修改用户密码
     *
     * @param user
     */
    public void updateUserPwd(UserInfo user);

    /**
     * 获取所有用户信息
     *
     * @param userId
     * @return
     */
    public UserInfo getUserAllInfo(Long userId);

    /**
     *
     */
    public PageInfo<UserInfo> queryByPage(Integer pageNo, String string, String searchValue);

    /**
     * 根据id删除用户
     *
     * @param userId
     * @return
     */
    public boolean deleteUserByUserId(Long userId);

    /**
     * 设置用户权限
     *
     * @param user
     */
    public void updateUserType(UserInfo user);


}
