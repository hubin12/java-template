package com.mrbeard.project.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mrbeard.project.entity.User;
import com.mrbeard.project.enums.ResultCodeEnum;
import com.mrbeard.project.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;

/**
 * jwt工具类
 *
 * @author: hubin
 * @date: 2020/11/18 16:14
 */
@Slf4j
public class JwtUtil {


    /**
     * 过期时间为一天
     * TODO 正式上线更换为15分钟
     */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * token私钥
     */
    private static final String TOKEN_SECRET = "yymt@&!18GH";

    /**
     * 生成签名,15分钟后过期
     *
     * @param username
     * @param userId
     * @return
     */
    public static String createToken(String username, Long userId) {
        //过期时间
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        //私钥及加密算法
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        //设置头信息
        HashMap<String, Object> header = new HashMap<>(2);
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        //附带username和userID生成签名
        return JWT.create().withHeader(header).withClaim("userName", username)
                .withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
    }


    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public static User validToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            Claim userName = jwt.getClaim("userName");
            Claim userId = jwt.getClaim("userId");
            User user = new User();
            user.setUserName(userName.asString()).setId(userId.asLong());
            return user;
        } catch (Exception e) {
            log.error("valid token fail!", e);
            throw new CustomException(ResultCodeEnum.UNAUTHORIZED);
        }

    }
}
