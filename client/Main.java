import java.io.*;
import java.util.*;
import com.eclipsesource.v8.*;
public class Main {
	static String NODE_SCRIPT = "var http = require('http');\n" + ""
			+ "var server = http.createServer(function (request, response) {\n"
			+ " response.writeHead(200, {'Content-Type': 'text/plain'});\n" + " response.end(someJavaMethod());\n"
			+ "});\n" + "" + "server.listen(8000);\n" + "console.log('Server running at http://127.0.0.1:8000/');";

	public static void main(String[] args) throws IOException {
		String script = "var sec = require('../Phase3/EncryptDecrypt.js');" + "\n" + "console.log(sec.encryption('This is my message', 'C:/keys/public.pem'));";
		File jsScriptFile = new File("..\\Phase3\\EncryptDecrypt.js");
		final NodeJS nodeJS = NodeJS.createNodeJS();
		JavaCallback callback = new JavaCallback() {
			public Object invoke(V8Object receiver, V8Array parameters) {
				return "Message Encrypted";
			}
		};

		nodeJS.getRuntime().registerJavaMethod(callback, "someJavaMethod");
		File nodeScript = createTemporaryScriptFile(script, "example");

		nodeJS.exec(nodeScript);

		while (nodeJS.isRunning()) {
			nodeJS.handleMessage();
		}
		nodeJS.release();
	}
	private static File createTemporaryScriptFile(final String script, final String name) throws IOException {
		File tempFile = File.createTempFile(name, ".js");
		PrintWriter writer = new PrintWriter(tempFile, "UTF-8");
		try {
			writer.print(script);
		} finally {
			writer.close();
		}
		return tempFile;	
	}
}
