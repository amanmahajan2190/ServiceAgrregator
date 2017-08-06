package response.transformer.transformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.RootTask;
import response.transformer.Transformer;

public class UserTransformer extends Transformer<RootTask> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserTransformer.class);

    @Override
    public <D> D Transform(RootTask sObj, Class<D> valueType) {
        LOGGER.info("User Transformer Called.");
        try {
            return (D) valueType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}

