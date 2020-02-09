package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessExceotion;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    //通过用户id获取用户对象的方法
   UserModel  getUserById(Integer id);
   void register(UserModel userModel) throws BusinessExceotion;
   /*
   telphone:用户注册手机
   password：用户加密后的密码
    */
   UserModel validataLogin(String telphone,String encrptPassword) throws BusinessExceotion;
}
