package com.lib.service.user.impl;

import com.lib.dao.UserInfoDao;
import com.lib.dao.UserRegisterDao;
import com.lib.entity.UserInfo;
import com.lib.entity.UserRegister;
import com.lib.enums.Const;
import com.lib.service.user.UserRegisterService;
import com.lib.utils.MD5Util;
import com.lib.utils.SendEmail;
import com.lib.utils.StringValueUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service
public class UserRegisterServiceImpl implements UserRegisterService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserRegisterDao userRegisterDao;

    public void updateEmail(UserInfo user) {
        userInfoDao.updateUserEmail(user);// 更新email
        UserRegister ur = userRegisterDao.queryById(user.getUserId());
        String validateCode = MD5Util.encode2hex(user.getUserEmail());
        LOG.error("validateCode={}", validateCode);

        ur.setValidateCode(validateCode);
        ur.setRegisterTime(new Date());
        userRegisterDao.updateNoStatus(ur);

        /// 邮件的内容
        StringBuffer sb = new StringBuffer("点击下面链接更换SOKLIB知识库管理系统网站账号，48小时生效，链接只能使用一次</br>");
        sb.append("<a href=\"http://localhost:8080/lib/user/update-userEmail?action=activate&userEmail=");
        sb.append(user.getUserEmail());
        sb.append("&validateCode=");
        sb.append(ur.getValidateCode());
        sb.append("\">http://localhost:8080/lib/user/update-userEmail?action=activate&userEmail=");
        sb.append(user.getUserEmail());
        sb.append("&validateCode=");
        sb.append(ur.getValidateCode());
        sb.append("</a>");

        // 发送邮件
        SendEmail.send(user.getUserEmail(), sb.toString());

    }

    /**
     * 处理注册
     */
    public void processRegister(String userName, String userPassword, String email, String url) {
        UserInfo user = new UserInfo();
        user.setUserName(userName);
        user.setUserPassword(StringValueUtil.getMD5(userPassword));
        user.setUserEmail(email);
        user.setUserType(2);
        userInfoDao.insertUserNoStatus(user);// 保存用户信息
        UserRegister ur = new UserRegister();
        UserInfo userRecord = userInfoDao.queryByEmail(email);
        ur.setUserId(userRecord.getUserId());
        ur.setValidateCode(MD5Util.encode2hex(email));
        ur.setRegisterTime(new Date());
        userRegisterDao.insertNoStatus(ur);
        /// 邮件的内容
        StringBuffer sb = new StringBuffer("用户：" + userName + ",您好！<br>欢迎使用SOKLIB知识库管理系统，请点击下面链接激活您的帐号<br>");
        sb.append("<a href=\"http://" + url + "/lib/register?action=activate&email=");
        sb.append(email);
        sb.append("&validateCode=");
        sb.append(ur.getValidateCode());
        sb.append("\">http://" + url + "/lib/register?action=activate&email=");
        sb.append(email);
        sb.append("&validateCode=");
        sb.append(ur.getValidateCode());
        sb.append("</a>");

        new Thread() {
            @Override
            public void run() {
                // 发送邮件
                try {
                    SendEmail.send(email, sb.toString());
                } catch (Exception e) {
                    LOG.error("发送到邮箱：" + email + " 失败！");
                }
            }
        }.start();

    }

    /**
     * 处理激活
     *
     * @param email        邮箱地址
     * @param validateCode 激活码
     * @throws Exception
     */
    public void processActivate(String email, String validateCode) throws Exception {
        // 数据访问层，通过email获取用户信息
        UserInfo user = userInfoDao.queryByEmail(email);
        UserRegister ur = userRegisterDao.queryById(user.getUserId());
        // 验证用户是否存在
        if (user != null) {
            // 验证用户激活状态
            if (user.getUserType() == 2) {
                /// 没激活
                Date currentTime = new Date();// 获取当前时间
                // 验证链接是否过期
                if (currentTime.before(ur.getLastActivateTime())) {
                    // 验证激活码是否正确
                    if (validateCode.equals(ur.getValidateCode())) {
                        // 激活成功， //并更新用户的激活状态，为已激活
                        user.setUserType(1);// 把状态改为激活
                        // 设置默认头像
                        String photoUuid = StringValueUtil.getUUID();
                        user.setUserPhoto(photoUuid);
                        userInfoDao.updateUserNoStatus(user);
                        userRegisterDao.updateNoStatus(ur);
                    } else {
                        LOG.error("激活码不正确");
                        throw new Exception("激活码不正确");
                    }
                } else {
                    LOG.error("激活码已过期！");
                    throw new Exception("激活码已过期！");
                }
            } else {
                LOG.error("邮箱已激活，请登录！");
                throw new Exception("邮箱已激活，请登录！");
            }
        } else {
            LOG.error("邮箱已激活，请登录！");
            throw new Exception("该邮箱未注册（邮箱地址不存在）！");
        }

    }

}
