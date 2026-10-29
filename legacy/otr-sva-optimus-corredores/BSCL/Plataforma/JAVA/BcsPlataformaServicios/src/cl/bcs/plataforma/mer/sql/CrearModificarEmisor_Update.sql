UPDATE "BSCL.Plataforma.BD::MER.Emisor"
SET
    "CodigoEmisor" = :p_CodigoEmisor,
    "CodigoRegulador" = :p_CodigoRegulador,
    "Regulador" = :p_Regulador,
    "EmiteIIF" = :p_EmiteIIF,
    "EmiteIRF" = :p_EmiteIRF,
    "EmiteFungible" = :p_EmiteFungible,
    "ValorClasificacionRiesgoIIF" = :p_ValorClasificacionRiesgoIIF,
    "IndicadorAGF" = :p_IndicadorAGF,
    "IdEstado" = :p_IdEstado,
    "IdEntidadClasificadoraRiesgo" = :p_IdEntidadClasificadoraRiesgo,
    "FechaActualizacion" = now(),
    "TipoEntidad" = :p_TipoEntidad
WHERE "IdEmisor" = :p_IdEmisor;