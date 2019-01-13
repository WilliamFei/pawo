package com.gm.auth;/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (c) 2012-2019. haiyi Inc.
 * pawo-power All rights reserved.
 */



import com.gm.po.UserPo;
import com.gm.request.UserRequest;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;


/**
 * <p> 登录接口 </p>
 *
 * <pre> Created: 2019-01-02 13:53  </pre>
 * <pre> Project: pawo-power  </pre>
 *
 * @author gmzhao
 * @version 1.0
 * @since JDK 1.7
 */
@Controller
@RequestMapping("pawo/user")
public class AuthController {


    private  HashOperations<String,Object,Object> hashOperations;

    private final static String USER_KEY = "pawo:auth:user";

    @Autowired
    public AuthController(RedisTemplate<String,Object> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }


    /**
     * 登录认证接口
     *
     * @param username 用户名
     * @param userPassword 用户密码
     * @return 返回信息
     */
    @GetMapping("login")
    public @ResponseBody ResponseEntity auth(@RequestParam("username")String username,
                                             @RequestParam("password")String userPassword){
        UserRequest user = (UserRequest)hashOperations.get(USER_KEY,username);
        String password = user.getPassword();
        boolean valid = BCrypt.checkpw(password,userPassword);
        if(valid){
            return ResponseEntity.ok("登录成功");
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录失败");
        }
    }


    /**
     * 创建用户
     *
     * @param userRequest 用户信息
     * @return 创建结果
     */
    @PostMapping("create")
    public ResponseEntity create(@RequestBody @Valid UserRequest userRequest){
        String userName = userRequest.getUsername();
        Object object = hashOperations.get(USER_KEY,userName);
        if(object!=null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("用户已存在");
        }
        String password = userRequest.getPassword();
        String hashPassWord = BCrypt.hashpw(password,BCrypt.gensalt());
//        Date createDate = userRequest.getDate();
//        String date = DateUtil.date(createDate).toString(DatePattern.PURE_DATE_PATTERN);
        UserPo userPo = new UserPo();
        userPo.setUsername(userRequest.getUsername());
        userPo.setPassword(hashPassWord);
        userPo.setType(userRequest.getType());
        userPo.setCreateTime("20190102");
        hashOperations.put(USER_KEY,userName,userPo);
        return ResponseEntity.ok("创建成功");
    }


    /**
     * 查询用户列表信息
     *
     * @return 用户列表信息
     */
    @GetMapping("list")
    public @ResponseBody ResponseEntity list(){
        List<Object> userPoList = hashOperations.values(USER_KEY);
        return ResponseEntity.ok(userPoList);
    }


}
