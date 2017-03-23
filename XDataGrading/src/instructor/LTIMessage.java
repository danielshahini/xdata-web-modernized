package instructor;
// Reference: https://code.google.com/p/chemvantage/
// This LTIMessage class is a slightly modified version of the LTIMessage class in chemVantage

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.google.gdata.util.common.util.Base64;



public class LTIMessage {  // utility for sending LTI-compliant "POX" or "REST+JSON" messages to a Tool Consumer (LMS)
	private static Logger logger = Logger.getLogger(LTIMessage.class.getName());
	String messageType="text/html";
	String acceptType = "application/xml";
	String messageText="";
	String httpMethod="POST";
	String oauth_consumer_key;
	String oauth_shared_secret;
	String destinationURL;
	
	LTIMessage() {}
    
    LTIMessage(String msgType,String msgText,String destURL,String key,String secret) {
    	this.messageType = msgType;
    	this.messageText = msgText;
    	this.destinationURL = destURL;
    	this.oauth_consumer_key = key;
    	this.oauth_shared_secret = secret;
    }   
    
    protected String send() throws Exception {
    	
    	// construct a hash of the message text to include as a custom parameter
    	String hash = new String(Base64.encode(DigestUtils.sha(messageText)));

    	OAuthParameters params = new OAuthParameters();
    	params.setOAuthConsumerKey(oauth_consumer_key);
    	params.setOAuthConsumerSecret(oauth_shared_secret);
    	params.setOAuthNonce(OAuthUtil.getNonce());
    	params.setOAuthTimestamp(OAuthUtil.getTimestamp());
    	params.setOAuthCallback("about:blank");
    	params.setOAuthSignatureMethod("HMAC-SHA1");
    	params.addCustomBaseParameter("oauth_version", "1.0");
    	params.addCustomBaseParameter("oauth_body_hash",hash);

    	String baseString = OAuthUtil.getSignatureBaseString(destinationURL,"POST",params.getBaseParameters());
    	String signature = new OAuthHmacSha1Signer().getSignature(baseString,params);
    	params.setOAuthSignature(signature);
    	params.addCustomBaseParameter("oauth_signature",signature);

    	// construct the signed message in the required format
    	URL u = new URL(destinationURL);
    	HttpURLConnection uc = (HttpURLConnection) u.openConnection();
    	uc.setDoOutput(true);
    	uc.setDoInput(true);
    	uc.setRequestMethod(httpMethod);
    	if (httpMethod.equals("GET")) acceptType = "application/vnd.ims.lti.v2.ToolSettings+json";
    	uc.setRequestProperty("Content-Type",messageType);
    	uc.setRequestProperty("Accept", acceptType);
    	uc.setRequestProperty("Content-Length",Integer.toString(messageText.length()));
    	uc.setRequestProperty("Authorization",buildAuthHeaderString(params));
    	
    	if (!messageAppearsValid()) return "Error: Message parameters were invalid.";

    	// send the message
    	OutputStreamWriter toTC = new OutputStreamWriter(uc.getOutputStream());
    	toTC.write(messageText);
    	toTC.flush();
    	
    	int responseCode = uc.getResponseCode();
    	if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // 200 or 201
    		BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    		StringBuffer res = new StringBuffer();
    		String line;
    		while ((line = reader.readLine()) != null) {
    			res.append(line);
    		}
    		reader.close();
    		toTC.close();
    		return res.toString();
    	} else {
    	 	toTC.close();
    	 	throw new Exception("Server returned status code: " + responseCode);  
    	}
    }

    private boolean messageAppearsValid() {
    	if (messageType == null) return false;
    	if (messageText==null || (messageText.isEmpty() && !httpMethod.equals("GET"))) return false;
    	if (oauth_consumer_key==null || oauth_consumer_key.isEmpty()) return false;
    	if (oauth_shared_secret==null || oauth_shared_secret.isEmpty()) return false;
    	if (destinationURL==null || destinationURL.isEmpty()) return false;
    	return true;
    }
    
	private String buildAuthHeaderString(OAuthParameters params) {
		StringBuffer buffer = new StringBuffer();
		try {
			int cnt = 0;
			buffer.append("OAuth ");
			Map<String, String> paramMap = params.getBaseParameters();
			Object[] paramNames = paramMap.keySet().toArray();
			for (Object paramName : paramNames) {
				String value = paramMap.get(paramName);
				buffer.append(paramName + "=\"" + URLEncoder.encode(value,"UTF-8") + "\"");
				cnt++;
				if (paramNames.length > cnt) {
					buffer.append(",");
				}

			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
		return buffer.toString();
	}

}