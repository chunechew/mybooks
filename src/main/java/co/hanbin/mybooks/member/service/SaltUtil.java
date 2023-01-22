package co.hanbin.mybooks.member.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;

@Service
public class SaltUtil {
    private static final String HMAC_SHA512 = "HmacSHA512";

    public String encodePassword(String salt, String password){
        String result = null;

        try {
            Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
            final byte[] byteKey = Utf8.encode(salt);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(Utf8.encode(password));

            result = Base64.getEncoder().encodeToString(macData);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String genSalt(){
        return BCrypt.gensalt();
    }

}
