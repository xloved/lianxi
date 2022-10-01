package com.hgx.apipassenger.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.hgx.internalcomm.dto.ResponseResult;
import com.hgx.internalcomm.dto.TokenResult;
import com.hgx.internalcomm.utils.JwtUtils;
import com.hgx.internalcomm.utils.RedisPrefixUtils;
import javafx.print.Printer;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.jnlp.SingleInstanceListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * jwt拦截器
 */
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean result = true;
        String resultString = "";

         String token = request.getHeader("Authorization");

         //解析token
        TokenResult tokenResult = null;
         try {
             tokenResult = JwtUtils.parseToken(token);//获取token
         }catch (SignatureVerificationException e){
             resultString = "token sign error";
             result = false;
         }catch(TokenExpiredException e){
              resultString = "token time out";
              result = false;

         }catch(AlgorithmMismatchException e){
              resultString = "token AlgorithmMismatchException";
              result = false;
         }catch(Exception e){
             resultString = "token invalid";
             result = false;
         }

         if(tokenResult == null){
             resultString = "token invalid";
             result = false;
         }else {
             //把token从redis中取出
             String passengerPhone = tokenResult.getPassengerPhone();
             String identity = tokenResult.getIdentity();

             String tokenKey = RedisPrefixUtils.getByToken(passengerPhone, identity);
             //从redis中取出token
             String tokenRedis = stringRedisTemplate.opsForValue().get(tokenKey);
             if(StringUtils.isBlank(tokenRedis)){
                  resultString = "token invalid";
                  result = false;
             }else{
                  if(!token.trim().equals(tokenRedis.trim())){
                      resultString = "token invalid";
                      result = false;
                  }
             }
         }

        // 传入的token与从redis中取出的token进行比较是否相等
         if(!result){//如果result为false，就把错误写出去
             PrintWriter out = response.getWriter();
             out.print(JSONObject.fromObject(ResponseResult.fail(resultString)).toString());
         }

        return result;
    }
}
