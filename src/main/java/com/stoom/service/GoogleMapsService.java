package com.stoom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoom.model.Address;
import com.stoom.model.google.GoogleGeoCode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

@ApplicationScoped
public class GoogleMapsService {

    @ConfigProperty(name = "map.key")
    private String key;

    public GoogleGeoCode getGeoCode(Address address) throws Exception {
        String term = String.format("%s,%s", address.getStreetName(), address.getNumber());

        StringBuilder url = new StringBuilder("https");
        url.append("://maps.googleapis.com/maps/api/geocode/json?");

        url.append("key=");
        url.append(key);
        url.append("&");
        url.append("sensor=false&address=");
        url.append(URLEncoder.encode(term, "UTF-8"));

        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            HttpGet request = new HttpGet(url.toString());

            // set common headers (may useless)
            request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0 Iceweasel/31.6.0");
            request.setHeader("Host", "maps.googleapis.com");
            request.setHeader("Connection", "keep-alive");
            request.setHeader("Accept-Language", "en-US,en;q=0.5");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Encoding", "gzip, deflate");

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                HttpEntity entity = response.getEntity();

                // recover String response (for debug purposes)
                StringBuilder result = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine);
                        result.append("\n");
                    }
                }

                // parse result
                ObjectMapper mapper = new ObjectMapper();
                GoogleGeoCode geocode = mapper.readValue(result.toString(), GoogleGeoCode.class);

                if (!"OK".equals(geocode.getStatus())) {
                    if (geocode.getError_message() != null) {
                        throw new Exception(geocode.getError_message());
                    }
                    throw new Exception("Can not find geocode for: " + address);
                }
                return geocode;
            }
        }
    }
}
