package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Implementation of a {@link PageComponentsProvider} that connects to the
 * "OpenDataHub Web Components Store" and enables all publicly available web
 * components to be used inside the application.
 * 
 * @author danielrampanelli
 */
public class WebComponentsStorePageComponentsProvider implements PageComponentsProvider {

	private static final String BASE_URL = "https://api.webcomponents.opendatahub.testingmachine.eu";

	@Override
	public List<PageComponent> getAvailableComponents() {
		List<PageComponent> pageComponents = new ArrayList<>();

		try {
			HttpResponse<JsonNode> componentsResponse = Unirest.get(String.format("%s/webcomponent", BASE_URL))
					.header("accept", "application/json").asJson();

			JSONArray components = componentsResponse.getBody().getObject().getJSONArray("content");
			for (int i = 0; i < components.length(); i++) {
				JSONObject component = components.getJSONObject(i);

				String componentUuid = component.getString("uuid");
				String componentTitle = component.getString("title");
				String componentDescription = component.getString("descriptionAbstract");

				HttpResponse<JsonNode> configResponse = Unirest
						.get(String.format("%s/webcomponent/%s/config", BASE_URL, componentUuid))
						.header("accept", "application/json").asJson();

				JSONObject componentConfiguration = configResponse.getBody().getObject();
				JSONObject dist = componentConfiguration.getJSONObject("dist");
				JSONObject configuration = componentConfiguration.getJSONObject("configuration");

				String deliveryBaseUrl = componentConfiguration.getString("deliveryBaseUrl");
				String distBaseUrl = String.format("%s/%s", deliveryBaseUrl, dist.getString("basePath"));
				JSONArray distFiles = dist.getJSONArray("files");
				String componentTagName = configuration.getString("tagName");

				PageComponent pageComponent = new PageComponent();
				pageComponent.setUid(componentUuid);
				pageComponent.setTitle(componentTitle);
				pageComponent.setDescription(componentDescription);
				pageComponent.setTagName(componentTagName);

				for (int j = 0; j < distFiles.length(); j++) {
					pageComponent.addAsset(String.format("%s/%s", distBaseUrl, distFiles.getString(j)));
				}

				pageComponents.add(pageComponent);
			}
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pageComponents;
	}

}
