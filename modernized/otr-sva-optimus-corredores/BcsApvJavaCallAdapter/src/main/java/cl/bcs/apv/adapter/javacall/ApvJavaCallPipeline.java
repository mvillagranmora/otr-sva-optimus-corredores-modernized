package cl.bcs.apv.adapter.javacall;

import com.google.gson.JsonObject;

/**
 * Test-facing and gateway-facing contract: a legacy JavaCall invocation (class name + legacy JSON) goes in, the legacy JSON envelope comes out
 * ({@code P_OUT_*} fields as the legacy Salida has them). Runtime exceptions of the use case propagate unchanged.
 */
public interface ApvJavaCallPipeline {

    JsonObject invocar(String claseLegacy, JsonObject entrada) throws Exception;
}
