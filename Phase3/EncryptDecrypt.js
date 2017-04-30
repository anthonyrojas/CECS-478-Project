const CRYPTO = require('crypto');
const PATH = require('path');
const FS = require('fs');
var privateKeyPath = "private.pem";
var publicKeyPath = "public.pem";
module.exports = {
	encryption : function(message, key){
		var aesIV = CRYPTO.randomBytes(16);
		var aesKey = CRYPTO.randomBytes(32);
		var hmacArray = CRYPTO.randomBytes(32);
		//read public key contents
		var publicKey = FS.readFileSync(key, 'utf8');
		var aes = CRYPTO.createCipheriv('aes-256-cbc', aesKey, aesIV);
		var cipherText = aes.update(new Buffer(message), 'utf8', 'hex') + aes.final('hex')
		const hmac = CRYPTO.createHmac('sha256', hmacArray);
		hmac.update(cipherText);
		var concatKeys = Buffer.concat([aesIV, aesKey, hmacArray]);
		var rsaCipherText = CRYPTO.publicEncrypt(publicKey, concatKeys, CRYPTO.constants.RSA_PKCS1_OAEP_PADDING).toString('hex');

		return{
			rsaCipher : rsaCipherText,
			aesCipher : cipherText,
			hmacTag : hmac.digest('hex')
		};
	}

	decryption : function(jsonObj, key){
		var rsaCipherText = jsonObj['rsaCipher'];
		var aesCipher = jsonObj['aesCipher'];
		var hmacTag = json['hmacTag'];
		var privateKey = FS.readFileSync(key, 'utf8');
		var buf = new Buffer(rsaCipherText, 'hex');
		var message = CRYPTO.privateDecrypt({key : privateKey, padding : CRYPTO.constants.RSA_PKCS1_OAEP_PADDING}, buf);
		//0-16 is IV, 16-48 is aes key, 48-80 is hmac key
		var ivKey = message.slice(0, 16);
		var aesKey = message.slice(16, 48);
		var hmacKey = message.slice(48, 80);
		//hmac authentication
		var hmac = CRYPTO.createHmac('sha256', hmacKey);
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
