package klaue.furrycrossposter.sites.sofurry;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * losely based on http://wiki.sofurry.com/wiki/How_to_use_OTP_authentication
 * @author klaue
 *
 */
public class SoFurryAuthentification {
	//Ajax message types
	public static final int AJAXTYPE_APIERROR = 5;
	public static final int AJAXTYPE_OTPAUTH = 6;
	
	private String curPadding = "@6F393fk6FzVz9aM63CfpsWE0J1Z7flEl9662X";
	private long curSequence = 0;
	private String curSalt = "";

	private String username = null;
	private String password = null;
	
	public SoFurryAuthentification(String user, String pass) {
		username = user;
		password = pass;
	}

	//Get the MD5 sum of a given input string
	private static String getMd5Hash(final String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Map<String, String> addAuthParametersToQuery(Map<String, String> queryParams) {
		TreeMap<String, String> resultMap = new TreeMap<>(queryParams);
		String hashedPassword = getMd5Hash(password+curSalt);
		String hash = getMd5Hash(hashedPassword + curPadding + curSequence);
		
		resultMap.put("otpuser", username);
		resultMap.put("otphash", hash);
		resultMap.put("otpsequence", Long.toString(curSequence));
		curSequence += 1;
		return resultMap;
	}
	
	/**
	 * Check if passed json string contains data indicating a sequence mismatch, as well as the new sequence data
	 * @param httpResult
	 * @return true if no sequence data found or sequence correct, false if the request needs to be resent with the new enclosed sequence data
	 * @throws JSONException 
	 */
	public boolean parseResponse(String httpResult) {
		//check for OTP sequence json and parse it.
		JsonElement jelement = null;
		try {
			jelement = new JsonParser().parse(httpResult);
		} catch (JsonSyntaxException e) {
			// probably not even json
			return true;
		}
		JsonObject  jobject = jelement.getAsJsonObject();
		JsonElement elem = jobject.get("messageType");
		int messageType = elem == null ? 0 : jobject.get("messageType").getAsInt();
		if (messageType == AJAXTYPE_OTPAUTH) {
			try {
				int newSequence = jobject.get("newSequence").getAsInt();
				String newPadding = jobject.get("newPadding").getAsString();
				String newSalt = jobject.get("salt").getAsString();
				curSequence = newSequence;
				curPadding = newPadding;
				curSalt = newSalt;
			} catch (Exception e) {
				// probably nullpointer because field not there
				e.printStackTrace();
			}
			return false;
		}
		
		return true;
	}
	
	private UrlEncodedFormEntity entityFromMap(Map<String, String> map) throws UnsupportedEncodingException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		for (String key : map.keySet()) {
			urlParameters.add(new BasicNameValuePair(key, map.get(key)));
		}
		return new UrlEncodedFormEntity(urlParameters);
	}
	
	private HttpEntity entityFromMapAndFile(Map<String, String> map, String fileParamName, File fileParam) throws UnsupportedEncodingException {
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addBinaryBody(fileParamName, fileParam, ContentType.DEFAULT_BINARY, fileParam.getName());
		
		for (String key : map.keySet()) {
			builder.addTextBody(key, map.get(key), ContentType.DEFAULT_TEXT);
		}
		return builder.build();
	}
	
	public String requestPost(String url, Map<String, String> parameters) {
		return post(url, parameters, null, null);
	}
	
	public String requestPostMultipart(String url, Map<String, String> parameters, String fileParamName, File fileParam) {
		return post(url, parameters, fileParamName, fileParam);
	}
	
	private String post(String url, Map<String, String> parameters, String fileParamName, File fileParam) {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			// try for 5 times, updating the params each time
			String httpResult = null;
			int tries = 0;
			boolean success = false;
			do {
				HttpPost request = new HttpPost(url);
				Map<String, String> paramsToSend = addAuthParametersToQuery(parameters);
				
				System.out.print("request try " + tries + ": " + url + (url.contains("?") ? "&" : "?"));
				Iterator<Map.Entry<String,String>> it = paramsToSend.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String,String> pair = it.next();
			        System.out.print(pair.getKey() + "=" + pair.getValue());
			        if (it.hasNext()) System.out.print("&");
				}
				System.out.print(" with padding " + curPadding + ", salt " + curSalt);
				if (fileParam != null) {
					System.out.println(", and file " + fileParam.getName());
				} else {
					System.out.println();
				}
				
				if (fileParam == null) {
					request.setEntity(entityFromMap(paramsToSend));
				} else {
					request.setEntity(entityFromMapAndFile(paramsToSend, fileParamName, fileParam));
				}
				HttpResponse response = httpClient.execute(request);
				httpResult = EntityUtils.toString(response.getEntity());
				
				System.out.println("httpResult try " + tries + ": " + httpResult);
				++tries;
				success = parseResponse(httpResult);
				//if (tries >= 5) break;
			} while(!success && tries < 5);
			
			if (!success) return null;
			return httpResult;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}