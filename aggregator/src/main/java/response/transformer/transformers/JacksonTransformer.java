package response.transformer.transformers;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import response.transformer.Transformer;

import java.io.IOException;

public class JacksonTransformer extends Transformer<String> {
    @Override
    public <T> T Transform(String sObj, Class<T> dtoType){
        if(sObj == null || dtoType == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(sObj, dtoType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
