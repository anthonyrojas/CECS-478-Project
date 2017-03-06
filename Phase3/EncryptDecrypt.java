import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.bind.DatatypeConverter;
import org.json.*;

/**
 * 
 * @author Corey and Anthony
 * Encrypt and Decrypt
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
		
		JSONObject json = Encrypt(input, Public_Key_File);
		String plainText = Decrypt(json, Private_Key_File);
		
		System.out.println("Decrypted Text: " + plainText);
	}
	
	/**
	 * 
	 * @param text - plain text to be encrypted
	 * @param file - file path of the public rsakey
	 * @return JSONObject - contains aesCipherText, rsaCipherText, HMAC Tag, and IV
	 */
	public static JSONObject Encrypt(String text, String file) {
		
		//rsa ciphertext - aes key and hmac key concatenated and then rsa encrypted
		byte[] rsaCipherText = null;
		//encrypted plain text
		byte[] aesCipherText = null;
		byte[] hmacTag = null;
		JSONObject jsonObject = new JSONObject();
		
		try {
			
			//get public key from file and create rsa object
			byte[] keyBytes = Files.readAllBytes(new File(file).toPath());
			X509EncodedKeySpec rsaSpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, kf.generatePublic(rsaSpec));
			
			//Generate the aes key
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey aesKey = keyGen.generateKey();
			
			//creates the aes iv
			byte[] iv = new byte[16];
			//random bytes for the IV
			SecureRandom rng = new SecureRandom();
			rng.nextBytes(iv);
			IvParameterSpec IV =  new IvParameterSpec(iv);
			
			//create aes cipher object
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, IV);

			
			//encrypt the text with the aes key
			aesCipherText = aesCipher.doFinal(text.getBytes());

			//generate the HMACSha-256bit key
			byte[] hmacArray = new byte[32];
			rng.nextBytes(hmacArray);
			Mac hmackey = Mac.getInstance("HmacSHA256");
			SecretKeySpec hmacsecKey = new SecretKeySpec(hmacArray, "HmacSHA256");
			hmackey.init(hmacsecKey);
			hmacTag = hmackey.doFinal(aesCipherText);
			
			//concatenate the aes and hmac keys
			byte[] aesKeyByte = aesKey.getEncoded();
			byte[] hmacKeyByte = hmacsecKey.getEncoded();
			byte[] aeshmacKey = new byte[aesKeyByte.length + hmacKeyByte.length];
			System.arraycopy(aesKeyByte, 0, aeshmacKey, 0, aesKeyByte.length);
			System.arraycopy(hmacKeyByte, 0, aeshmacKey, aesKeyByte.length, aesKeyByte.length);
			
			//Encrypt concatenated keys
			rsaCipherText = rsaCipher.doFinal(aeshmacKey);
			
			jsonObject.put("RSA CipherText", DatatypeConverter.printHexBinary(rsaCipherText));
			jsonObject.put("AES CipherText", DatatypeConverter.printHexBinary(aesCipherText));
			jsonObject.put("AES IV", DatatypeConverter.printHexBinary(IV.getIV()));
			jsonObject.put("HMAC Tag", DatatypeConverter.printHexBinary(hmacTag));
			
			//print out jsonObject
			System.out.println(jsonObject.toString());
			System.out.println("AES Cipher Text: " + DatatypeConverter.printHexBinary(rsaCipherText));
			System.out.println("AES IV: " + DatatypeConverter.printHexBinary(IV.getIV()));
			System.out.println("RSA Cipher Text: " + DatatypeConverter.printHexBinary(rsaCipherText));
			System.out.println("HMAC Tag: " + DatatypeConverter.printHexBinary(hmacTag));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * 
	 * @param jsonObject - contains aesCipherText, rsaCipherText, HMAC Tag, and IV from encryption
	 * @param file - path location of the private rsa key
	 * @return - decrypted plaintext
	 */
	public static String Decrypt(JSONObject jsonObject, String file) {
		
		String plainText = null;
		
		try {
			//Load Private Key
			byte[] keyBytes = Files.readAllBytes(new File(file).toPath());
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB//OAEPWithSHA-256andMGF1Padding");
			rsaCipher.init(Cipher.DECRYPT_MODE, kf.generatePrivate(spec));
			
			//recover rsa cipher text
			String rsa = jsonObject.getString("RSA CipherText");
			byte[] rsaCipherText = DatatypeConverter.parseHexBinary(rsa);
			
			//Decrypt concatenated keys
			byte[] aesHMACKeys = rsaCipher.doFinal(rsaCipherText);
			
			//get aes key and hmackey from concatenated key
			byte[] aesKey = new byte[aesHMACKeys.length / 2];
			byte[] hmacKey = new byte[aesHMACKeys.length / 2];
			System.arraycopy(aesHMACKeys, 0, aesKey, 0, aesKey.length);
			System.arraycopy(aesHMACKeys, aesKey.length, hmacKey, 0, hmacKey.length);
			
			//recover aesCipherText and IV
			String aesFromJson = jsonObject.getString("AES CipherText");
			byte[] aesCipherText = DatatypeConverter.parseHexBinary(aesFromJson);
			String iv = jsonObject.get("AES IV").toString();
			byte[] IV = DatatypeConverter.parseHexBinary(iv);
			
			//recover HMACTag
			String HMAC = jsonObject.getString("HMAC Tag");
			byte[] hmacTag1 = DatatypeConverter.parseHexBinary(HMAC);
			
			//Generate an HMACTag
			Mac hmackey = Mac.getInstance("HmacSHA256");
			SecretKeySpec hmacsecKey = new SecretKeySpec(hmacKey, "HmacSHA256");
			hmackey.init(hmacsecKey);
			byte[] hmacTag2 = hmackey.doFinal(aesCipherText);
			
			//compare hmac tags
			if (java.util.Arrays.equals(hmacTag1, hmacTag2)) {
				
				//create aes cipher object and decrypt message
				Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(IV));
				plainText = new String (aesCipher.doFinal(aesCipherText), "US-ASCII");
		
			} else {
				System.out.println("HMACTags did not match, Decryption  Failure");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plainText;
	}
}