package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessExceotion;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials ="true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;
    @Autowired
    private  HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method ={RequestMethod.POST},consumes ={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                  @RequestParam(name="password")String password) throws BusinessExceotion, UnsupportedEncodingException, NoSuchAlgorithmException {

        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                StringUtils.isEmpty(password)){
            throw  new BusinessExceotion(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录服务,用来校验用户登录是否合法
       UserModel userModel= userService.validataLogin(telphone,this.EncodeByMd5(password));
        //将登录凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);

    }



    //用户注册接口
    @RequestMapping(value = "/register",method ={RequestMethod.POST},consumes ={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessExceotion, UnsupportedEncodingException, NoSuchAlgorithmException {
           //验证手机号和对应的otpcode相符合
        String inSessionOtpCode=(String)this.httpServletRequest.getSession().getAttribute(telphone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw  new BusinessExceotion(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }




        //用户的注册流程
        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // 确定计算方法

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        // 加密字符串
        String  newStr= Arrays.toString(Base64Utils.encode(md5.digest(str.getBytes("utf-8"))));
        System.out.println(newStr);
        return newStr;


    }




    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method ={RequestMethod.POST},consumes ={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphon){
     //需要按照一定的规则生成OTp验证码
        Random random=new Random();
       int randomInt= random.nextInt(99999);
       randomInt+=10000;
       String otpCode=String.valueOf(randomInt);

        // 将OTP验证码同对应的用户的手机号关联,使用httpsession的方式绑定他的手机号与COTPCODE
        httpServletRequest.getSession().setAttribute(telphon,otpCode);

        //将OTP验证码通过短信通道发送给用户
        System.out.println("telphon ="+telphon + "& otpCode ="+ otpCode);
        return CommonReturnType.create(null);
    }






    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessExceotion {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel=userService.getUserById(id);

        //若获取的对应用户信息不存在
        if(userModel==null){
            //userModel.setEncrptPassword("123");
            throw new BusinessExceotion((EmBusinessError.USER_NOT_EXIST));
        }


        //将核心领域模型用户对象转化为课供ui使用的view object
        UserVO userVO= convertFromModel(userModel);
        //返回通用对象
        return CommonReturnType.create(userVO);
    }
    private  UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        } else {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userModel, userVO);
            return userVO;
        }
    }






}
