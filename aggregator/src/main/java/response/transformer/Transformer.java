package response.transformer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class Transformer<S> {

    public <D> D Transform(S sObj, Class<D> valueType){
        throw new NotImplementedException();
    }
}

