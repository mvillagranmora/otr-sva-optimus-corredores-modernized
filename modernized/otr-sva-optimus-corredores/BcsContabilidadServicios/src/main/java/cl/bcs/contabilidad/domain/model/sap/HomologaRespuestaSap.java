package cl.bcs.contabilidad.domain.model.sap;

/** Row of {@code ITZ.HomologaRespuestaSap}: SAP error code + text fragment to user message (InterfazContable.java:69-78). */
public record HomologaRespuestaSap(Long Codigo, String Valor, String Mensaje) {}
