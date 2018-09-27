/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.distributed.programming.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
/**
 *
 * @author user
 */
public  class DPUCrypter {
    
    public static int CryptFileUsingAES(boolean encrypt, String key, File inputFile, File outputFile,String checkSum) {
        try {
            if(key.length()<16){                
                key=String.format("%016d", Integer.parseInt(key));
            }
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            if (encrypt) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();
            if(checkSum!=null){
                try{
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String checkSumOfDecryptedFile=DPUCrypter.checksum(outputFile, md);
                    if(!(checkSum.equals(checkSumOfDecryptedFile))){
                        return -2;
                    }
                }catch(Exception err){

                }
            }
            return 0;

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | BadPaddingException
                | IllegalBlockSizeException | IOException e) {

            //e.printStackTrace();
            return -1;
        } catch (InvalidKeyException e) {

            //e.printStackTrace();
            return -2;
        }
    }
    
    public static String CrackFile(long fromIndex, long toIndex, File inputFile, File outputFile,String checkSum) {

        Date startDate=new Date();
        for (long currentIndex = fromIndex; currentIndex <= toIndex; currentIndex++) {            
            System.out.println("Current key: "+currentIndex+" / Pending tries: "+(toIndex- currentIndex)+ " / cracking started at "+startDate.toString());
            int internalReturn = CryptFileUsingAES(false, Long.toString(currentIndex), inputFile, outputFile,checkSum);
            if (internalReturn == 0) {
                Date endDate=new Date();
                System.out.println("The key is: " + currentIndex+" / ended at "+endDate.toString());
                return Long.toString(currentIndex);
            } 
        }
        
        return "NOT FOUND";

    }
    
    public static String checksum(File filepath, MessageDigest md) throws IOException {

        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }
    
}
