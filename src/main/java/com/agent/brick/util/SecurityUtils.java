package com.agent.brick.util;

import com.agent.brick.constants.GlobalConstants;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 安全类工具
 * </p>
 *
 * @author cKnight
 * @since 2025/7/4
 */
@Slf4j
public class SecurityUtils {
    /** 秘钥 */
    private static final String SECRET_KEY = "0100a2ac742b4e68bbf85b3b082a9ad3";

    /** 盐值 */
    private static final String SALT = STR."\{GlobalConstants.SECRET}76091a570ec947f085a3195142679776";


    public static String MD5(String data){
        return MD5(data, SALT);
    }

    public static String AESEncryptJSON(JSONObject data,String key){
        return AESEncrypt(data.toJSONString(),key);
    }

    public static JSONObject AESDecryptJSON(String encryptedData ,String key){
        return JSONObject.parseObject(AESDecrypt(encryptedData,key));
    }

    public static String AESEncryptJSON(JSONObject data){
        return AESEncrypt(data.toJSONString());
    }

    public static JSONObject AESDecryptJSON(String encryptedData ){
        return JSONObject.parseObject(AESDecrypt(encryptedData));
    }


    public static String AESEncrypt(String data){
        return AESEncrypt(data,SECRET_KEY);
    }

    public static String AESDecrypt(String encryptedData ){
        return AESDecrypt(encryptedData,SECRET_KEY);
    }


    /**
     * AES解密
     * @param encryptedData 加密数据
     * @param key 秘钥
     * @return 解密后数据
     */
    public static String AESDecrypt(String encryptedData ,String key){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Base64解码
            byte[] decodedBytes = Base64.decodeBase64(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }catch (Exception e){
            log.error("AES解密异常:",e);
            return null;
        }
    }

    /**
     * AES加密
     * @param data 指定数据
     * @param key 秘钥
     * @return 密文
     */
    public static String AESEncrypt(String data,String key){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Base64编码
            return Base64.encodeBase64String(encryptedBytes);
        }catch (Exception e){
            log.error("AES加密失败:",e);
            return null;
        }
    }

    /**
     * MD5 hash
     * @param data 数据
     * @param salt 盐值
     * @return md5
     */
    public static String MD5(String data,String salt){
        return Md5Crypt.md5Crypt(data.getBytes(StandardCharsets.UTF_8),salt);
    }
}
