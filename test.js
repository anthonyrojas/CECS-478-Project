const ENC_DEC = require("./Phase3/encryptdecrypt.js");
var privateKeyPath = "private.pem";
var publicKeyPath = "public.pem";
var message = "Hello world! This is a sample message";
var jsonCipher = ENC_DEC.encryption(message, publicKeyPath);
console.log(jsonCipher);
var plainText = END_DEC.decryption(jsonCipher, privateKeyPath);
console.log(plainText);
