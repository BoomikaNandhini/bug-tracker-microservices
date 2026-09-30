package com.bugtrackerpro.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, new OtpData(otp, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);
        if (data != null && data.otp.equals(otp) && System.currentTimeMillis() < data.expiry) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }

    private static class OtpData {
        String otp;
        long expiry;

        OtpData(String otp, long expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
