package response.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.transformer.transformers.JacksonTransformer;
import response.transformer.transformers.UserTransformer;

public class TransformerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerFactory.class);

    public Transformer getInstance(TransformerType transformerType) {
        switch (transformerType) {
            case JACKSON:
                return new JacksonTransformer();
            case USER:
                return new UserTransformer();
            default:
                LOGGER.error("No instance found for transformer type: {}.", transformerType);
        }
        return null;
    }
}
