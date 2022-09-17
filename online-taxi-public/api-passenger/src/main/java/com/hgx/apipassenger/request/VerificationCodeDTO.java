package com.hgx.apipassenger.request;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//验证码请求类
@Data
public class VerificationCodeDTO {
    //手机号码
    private String passengerPhone;//此处的值要于测试文档定义的值的大小写保持一致

    private String verificationCode;

}
