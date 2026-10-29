package cl.bcs.mesa.javacall;

import com.google.gson.JsonObject;

public interface JavaCallService {
    String fqn();
    JsonObject execute(JsonObject params);
}
