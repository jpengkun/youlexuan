package com.offcn.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbUser;
import com.offcn.user.service.UserService;
import com.offcn.util.PhoneFormatCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * usercontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		try {
			//检验手机号格式
			boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(user.getPhone());

			if (!phoneLegal) {
				return new Result(false,"手机号格式不正确");
			}

			//校验验证码
			String oldCode = (String) redisTemplate.boundHashOps("phoneCode").get(user.getPhone());

			if (!oldCode.equals(code)){
				return new Result(false,"验证码错误，请重新输入");
			}
			userService.add(user);
			//注册成功后该手机号验证码清空
			redisTemplate.boundHashOps("phoneCode").delete(user.getPhone());
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		try {
			//检验手机号格式
			boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);

			if (!phoneLegal) {
				return new Result(false,"手机号格式不正确");
			}
			userService.sendCode(phone);
			return new Result(true, "验证码发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "验证码发送失败");
		}
	}
	
}
