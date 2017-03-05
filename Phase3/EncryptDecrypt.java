import java.io.*;
import java.security.*;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * 
 * @author Corey
 * Encrypt and Decyrpt
 */
public class EncryptDecrypt {
	
	public static void main(String args[]) throws Exception {
		
		/**
		 * File path for private pem key
		 * had to convert it to .der so java can read it
		 */
		String Private_Key_File = "C:/cygwin64/home/Corey/private_key.der";	
		/**
		 * File path for public pem key
		 * had to convert it to .der so java can read it
		 */
		String Public_Key_File = "C:/cygwin64/home/Corey/public_key.der";
		/**
		 * Text to be encrypted
		 */
		String input = "Text to be encrypted";
		
		PrivateKeyReader r = new PrivateKeyReader();
		PublicKeyReader pr = new PublicKeyReader();
		PrivateKey rsaPrivKey;
		PublicKey rsaPubKey = null;
		
		//loads the private and public key
		try {
			rsaPubKey = pr.getPublicKey(Public_Key_File);
			rsaPrivKey = r.getPrivateKey(Private_Key_File);
		} catch (FileNotFoundException fnf){
			System.out.println("File Not Found");
		}
		
		byte[] cipher = Encrypt(input, rsaPubKey);
	}
	
	/**
	 * 
	 * @param text - plain text to be encrypted
	 * @param pubKey - public rsakey
	 * @return ciphertext
	 */
	public static byte[] Encrypt(String text, PublicKey pubKey) {
		
		byte[] rsaCipherText = null;
		byte[] aesCipherText = null;
		
		//generates the aes and hmac keys
		try {
			//Generate the aes key
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey aesKey = keyGen.generateKey();
			//16 is the block size of the key
			byte[] iv = new byte[16];
			SecureRandom rng = new SecureRandom();
			rng.nextBytes(iv);
			
			//create cipher
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//encrypt the text in byte form
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
			byte[] byteText = text.getBytes();
			aesCipherText = aesCipher.doFinal(byteText);
			//convert from binary to hex string
			String aesText = Base64.getEncoder().encodeToString(aesCipherText);
			//print out aes cipher text
			System.out.println(aesText);
			
			//generate hmac 256-bit sha 256 key
			//KeyGenerator keyGen2 = KeyGenerator.getInstance("HmacSHA256");
			//keyGen2.init(256);
			//SecretKey hmacKey = keyGen2.generateKey();
			
			Mac hmackey = Mac.getInstance("HmacSHA256");
			SecretKeySpec secKey = new SecretKeySpec(pubKey.getEncoded(), "HmacSHA256");
			hmackey.init(secKey);
			String hmac = Base64.getEncoder().encodeToString(hmackey.doFinal(aesCipherText));
			System.out.println(hmac);
			
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm not found");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return rsaCipherText;
	}
}
