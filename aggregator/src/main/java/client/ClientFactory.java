package client;

import client.clients.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    public <T> T getInstance(ClientType clientType) {
        switch (clientType) {
            case HTTP:
                HttpClient httpClient = new HttpClient();
                return (T) httpClient;

            default:
                LOGGER.error("No instance found for client type: {}.", clientType);
        }

        return null;
    }
}
