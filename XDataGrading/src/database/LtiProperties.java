package database;

public class LtiProperties {

	public String getConsumerAuthKey() {
		return Configuration.consumerAuthKey;
	}

	public String getSecretKey() {
		return Configuration.sectetKey;
	}
	
	public String getCallBackURL() {
		return Configuration.callBackUrl;
	}
	
	public String getMoodleXdataUrl() {
		return Configuration.moodleXdataUrl;
	}
}
