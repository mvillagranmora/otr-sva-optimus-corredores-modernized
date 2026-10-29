package cl.bcs.plataforma.service;

import com.google.gson.JsonObject;

public interface JavaCallService {
    String fqn();
    JsonObject execute(JsonObject params);
}
