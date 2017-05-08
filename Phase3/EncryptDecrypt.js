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
    		rsaCipher : rsaCipherText,
    		aesCipher : cipherText,
    		hmacTag : hmac.digest('hex')
    	};
    },
    
    decryption:function(jsonObj, key){
        //RSA cipher text
    	var rsaCipherText = jsonObj['rsaCipher'];
        //AES ciphertext
    	var aesCipher = jsonObj['aesCipher'];
        //HMAC Tag
    	var hmacTag = jsonObj['hmacTag'];
        //RSA private key
    	var privateKey = FS.readFileSync(key, 'utf8');
    	var buf = new Buffer(rsaCipherText, 'hex');
        //Decrypt the concatenated keys
    	var message = CRYPTO.privateDecrypt({key : privateKey, padding : CRYPTO.constants.RSA_PKCS1_OAEP_PADDING}, buf);
    	//0-16 is IV, 16-48 is aes key, 48-80 is hmac key
    	var ivKey = message.slice(0, 16);
    	var aesKey = message.slice(16, 48);
    	var hmacKey = message.slice(48, 80);
    	//hmac authentication
    	var hmac = CRYPTO.createHmac('sha256', hmacKey);
        //compare HMAC tags
    	hmac.update(jsonObj["aesCipher"]);
    	if(!Buffer.from(hmacTag, 'hex').equals(hmac.digest()))
    		return "Failure! HMAC tags do not match.";
    	//message decrypt
    	var aes = CRYPTO.createDecipheriv('aes-256-cbc', aesKey, ivKey);
    	message = aes.update(Buffer.from(jsonObj["aesCipher"],'hex'), null, 'utf8');
    	message += aes.final('utf8');
    	return message;
    }
}
