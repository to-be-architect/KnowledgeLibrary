package com.lib.web.admin.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lib.dto.JSONResult;
import com.lib.entity.MessageInfo;
import com.lib.entity.UserInfo;
import com.lib.service.user.MessageService;
import com.lib.service.user.UserService;
import com.lib.utils.StringValueUtil;

@Controller
@RequestMapping("/admin")
public class UserManageController {
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageService msgService;
	/**
	 * 分页以及查询
	 * @param pageNumber
	 * @param pageSize
	 * @param searcher
	 * @return
	 */
	/**
	 * 删除用户
	 * @param userId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/remove-user", method ={ RequestMethod.POST,RequestMethod.GET})
	public JSONResult removeUser(Long userId){
		JSONResult jsonResult = null;
		boolean flag = userService.deleteUserByUserId(userId);
		if(flag){
			jsonResult = new JSONResult(true, "success");
			jsonResult.setData("success");
		}
		else
			jsonResult = new JSONResult(false, "error");
		return jsonResult;
	}
	@ResponseBody
	@RequestMapping(value = "/remove-userList", method ={ RequestMethod.POST,RequestMethod.GET})
	public JSONResult removeUserList(String userList){
		JSONResult jsonResult = null;
		try{
		String users[] = userList.split("A");
		List<Long>list = new ArrayList<Long>();
		int i = 0;
		if("on".equals(users[0])){
			i = 1;
		}
		for(;i<users.length;i++){
			list.add( Long.parseLong(users[i]));
		}
		for(i = 0;i<list.size();i++){
			userService.deleteUserByUserId(list.get(i));
		}
			jsonResult = new JSONResult(true, "success");
			jsonResult.setData("success");
		}catch (Exception e) {
			jsonResult = new JSONResult(false, "error");
		}
		return jsonResult;
	}
	
	/**
	 * 初始化密码
	 * @param userId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/send-msg", method ={ RequestMethod.POST,RequestMethod.GET})
	public JSONResult sendMessage(Long userId, String msgTitle, String msgContent){
		JSONResult jsonResult = null;
		try{
			MessageInfo msg = new MessageInfo();
			msg.setMsgTitle(msgTitle);
			msg.setMsgContent(msgContent);
			msg.setIsRead(false);
			msg.setUserId(userId);
			msg.setMsgTime(new Date());
			msgService.insertMsg(msg);
			jsonResult = new JSONResult(true, "success");
			jsonResult.setData("success");
		}catch(Exception e){
			jsonResult = new JSONResult(true, "error");
		}
		return jsonResult;
	}
	/**
	 * 初始化密码
	 * @param userId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/refresh-userpwd", method ={ RequestMethod.POST,RequestMethod.GET})
	public JSONResult refreshUserPassword(Long userId){
		JSONResult jsonResult = null;
		try{
			UserInfo user = userService.getUserAllInfo(userId);
			user.setUserPassword(StringValueUtil.getMD5("12345"));
			 userService.updateUserPwd(user);
			jsonResult = new JSONResult(true, "success");
			jsonResult.setData("success");
		}catch(Exception e){
			jsonResult = new JSONResult(false, "error");
		}
		return jsonResult;
	}
	
	@ResponseBody
	@RequestMapping(value = "/change-usertype", method ={ RequestMethod.POST,RequestMethod.GET})
	public JSONResult changeUserType(Long userId, Integer userType){
		JSONResult jsonResult = null;
		try{
			UserInfo user = userService.getUserAllInfo(userId);
			user.setUserType(userType);
			 userService.updateUserType(user);
			jsonResult = new JSONResult(true, "success");
			jsonResult.setData("success");
		}catch(Exception e){
			jsonResult = new JSONResult(false, "error");
		}
		return jsonResult;
	}
}
