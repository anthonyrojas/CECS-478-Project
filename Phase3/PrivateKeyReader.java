import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateKeyReader {
	
	public PrivateKey getPrivateKey(String filename) throws Exception {
		
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		
		return kf.generatePrivate(spec);		
	}
}
