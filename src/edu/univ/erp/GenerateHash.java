package edu.univ.erp;

import edu.univ.erp.auth.PasswordUtil;

public class GenerateHash {
    public static void main(String[] args) {
        String hash = PasswordUtil.hashPassword("India@123");
        System.out.println("HASH: " + hash);
    }
}