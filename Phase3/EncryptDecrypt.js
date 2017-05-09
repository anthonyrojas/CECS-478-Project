const CRYPTO = require('crypto');
const FS = require('fs');
module.exports = {
    encryption:function(message, key){
        //initialization vector
    	var aesIV = CRYPTO.randomBytes(16);
    	var aesKey = CRYPTO.randomBytes(32);
    	var hmacArray = CRYPTO.randomBytes(32);
    	//read public key contents
    	var publicKey = FS.readFileSync(key, 'utf8');
        //AES 256-bit key with CBC block mode
    	var aes = CRYPTO.createCipheriv('aes-256-cbc', aesKey, aesIV);
        //encrypt plain text with AES key
    	var cipherText = aes.update(new Buffer(message), 'utf8', 'hex') + aes.final('hex')
    	//HMAC 256 bit key with SHA256
        const hmac = CRYPTO.createHmac('sha256', hmacArray);
        //compute integrity tag
    	hmac.update(cipherText);
        //concatenate AES and HMAC keys
    	var concatKeys = Buffer.concat([aesIV, aesKey, hmacArray]);
        //Encrypt concatenated keys with RSA public key
    	var rsaCipherText = CRYPTO.publicEncrypt(publicKey, concatKeys, CRYPTO.constants.RSA_PKCS1_OAEP_PADDING).toString('hex');
    
    	return{
    		rsa : rsaCipherText,
    		aes : cipherText,
    		hmac : hmac.digest('hex')
    	};
    },
    
    decryption:function(jsonObj, key){
        //RSA cipher text
    	var rsaCipherText = jsonObj['rsa'];
        //AES ciphertext
    	var aesCipherText = jsonObj['aes'];
        //HMAC Tag
    	var hmacTag = jsonObj['hmac'];
        //RSA private key
    	var privateKey = FS.readFileSync(key, 'utf8');
        //Decrypt the concatenated keys
    	var rsaCipher = CRYPTO.privateDecrypt({key : privateKey, padding : CRYPTO.constants.RSA_PKCS1_OAEP_PADDING}, new Buffer(rsaCipherText, 'hex'));
    	//0-16 is IV, 16-48 is aes key, 48-80 is hmac key
    	var ivKey = rsaCipher.slice(0, 16);
    	var aesKey = rsaCipher.slice(16, 48);
    	var hmacKey = rsaCipher.slice(48, 80);
    	//hmac authentication
    	var hmac = CRYPTO.createHmac('sha256', hmacKey);
        //compare HMAC tags
    	hmac.update(aesCipherText);
    	if(!Buffer.from(hmacTag, 'hex').equals(hmac.digest()))
    		return "HMAC verification failure! Message not received as expected";
    	//message decrypt
    	var aes = CRYPTO.createDecipheriv('aes-256-cbc', aesKey, ivKey);
    	rsaCipher = aes.update(Buffer.from(jsonObj["aes"],'hex'), null, 'utf8');
    	var message = rsaCipher + aes.final('utf8');
    	return message;
    }
}
