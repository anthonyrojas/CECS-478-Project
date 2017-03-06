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
		
		JSONObject json = Encrypt(input, Public_Key_File);
	}
	
	/**
	 * 
	 * @param text - plain text to be encrypted
	 * @param pubKey - public rsakey
	 * @return ciphertext
	 */
	public static JSONObject Encrypt(String text, String file) {
		
		byte[] rsaCipherText = null;
		byte[] aesCipherText = null;
		byte[] hmacTag = null;
		JSONObject jsonObject = new JSONObject();
		
		//generates the aes and hmac keys
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
			
			//create the aes iv
			byte[] iv = new byte[16];
			SecureRandom rng = new SecureRandom();
			rng.nextBytes(iv);
			IvParameterSpec IV =  new IvParameterSpec(iv);
			
			//create aes cipher object
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, IV);

			
			//encrypt the text with the aes key
			aesCipherText = aesCipher.doFinal(text.getBytes());
			//convert from binary to hex string
			String aesText = Base64.getEncoder().encodeToString(aesCipherText);

			//generate the HMACSha-256bit key
			byte[] hmacArray = new byte[32];
			rng.nextBytes(hmacArray);
			Mac hmackey = Mac.getInstance("HmacSHA256");
			SecretKeySpec hmacsecKey = new SecretKeySpec(hmacArray, "HmacSHA256");
			hmackey.init(hmacsecKey);
			hmacTag = hmackey.doFinal(aesCipherText);
			String hmacFinal = Base64.getEncoder().encodeToString(hmacTag);
			
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
			
			System.out.println("RSA CipherText: " + DatatypeConverter.printHexBinary(rsaCipherText));
			System.out.println("AES CipherText: " + DatatypeConverter.printHexBinary(aesCipherText));
			System.out.println("AES IV: " + DatatypeConverter.printHexBinary(IV.getIV()));
			System.out.println("HMAC Tag: " + DatatypeConverter.printHexBinary(hmacTag));
			
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return jsonObject;
	}
	
	public static String Decrypt(JSONObject jsonObJect, String file) {
		String plainText = null;
		
		try {
			//Load Private Key
			byte[] keyBytes = Files.readAllBytes(new File(file).toPath());
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB//OAEPWithSHA-256MGF1Padding");
			rsaCipher.init(Cipher.DECRYPT_MODE, kf.generatePrivate(spec));
			
			//recover aesCipherText
			//String aesFromJson = JSONObject.getString("AES CipherText");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return plainText;
	}
}

























