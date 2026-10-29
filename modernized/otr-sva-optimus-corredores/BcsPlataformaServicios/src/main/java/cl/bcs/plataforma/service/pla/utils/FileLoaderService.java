package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

@Service
public class FileLoaderService implements JavaCallService {

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.FileLoader"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // Legacy was a dummy class with no implementation
        return new JsonObject();
    }
}
