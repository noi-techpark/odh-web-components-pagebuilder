package it.bz.opendatahub.webcomponentspagebuilder.data.converters;

import java.io.IOException;
import java.util.List;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converter component that will map a list of String objects to a JSON
 * representation for persistence in a simple text-based database column.
 * 
 * @author danielrampanelli
 */
public class StringListAttributeConverter implements AttributeConverter<List<String>, String> {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setSerializationInclusion(Include.ALWAYS);

		objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
	}

	protected JavaType getType() {
		return objectMapper.getTypeFactory().constructParametricType(List.class, String.class);
	}

	@Override
	public String convertToDatabaseColumn(List<String> object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public List<String> convertToEntityAttribute(String json) {
		if (json == null) {
			return null;
		}

		try {
			return objectMapper.readValue(json, getType());
		} catch (IOException e) {
			return null;
		}
	}

}