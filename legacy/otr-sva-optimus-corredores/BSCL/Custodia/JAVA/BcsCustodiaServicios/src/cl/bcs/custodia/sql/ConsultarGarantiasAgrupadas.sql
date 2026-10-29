SELECT 
"IdCliente",
"IdPortafolio",
"NombreCliente",
"IdOrigenGarantia",
"NombreOrigenGarantia",
"DocumentoIdentidad",
"CodigoPortafolio",
"IdClasificacionTipoInstrumento",
"IdConcepto",
"NombreConcepto",
"RazonSocialContraparte",
"IdInstrumento",
"Nemotecnico",
"IdTipoInstrumento",
"NombreTipoInstrumento",
"IdContraparteGarantia",
"IdMonedaInstrumento",
SUM("INGCantidad") AS "INGCantidadAgrupada",
SUM("CantidadGarantia") AS "CantidadAgrupada",
SUM("MontoValorizado") AS  "MontoAgrupado",
SUM("MontoValorizadoMonedaInstrumento") AS "MontoValorizadoMonedaInstrumentoAGR"


FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
WHERE "IdEstado" = 'VIG' AND "IdOrigenGarantia" IN ('CTA','CUS')
AND (:p_IdCliente IS NULL OR "IdCliente" = :p_IdCliente)
AND (:p_IdPortafolio IS NULL OR "IdPortafolio" = :p_IdPortafolio)
AND (:p_FechaDeLiquidacion IS NULL OR "INGFechaLiquidacion" <= :p_FechaDeLiquidacion)
AND (:p_IdContraparteGarantia IS NULL OR "IdContraparteGarantia" = :p_IdContraparteGarantia)
AND (:p_IdMonedaInstrumento IS NULL OR "IdMonedaInstrumento" = :p_IdMonedaInstrumento)
AND (:p_IdInstrumento IS NULL OR "IdInstrumento" = :p_IdInstrumento)
AND (:p_IdOrigenGarantia IS NULL OR "IdOrigenGarantia" = :p_IdOrigenGarantia)


GROUP BY 
"IdCliente",
"IdPortafolio",
"IdConcepto" ,
"IdInstrumento",
"IdContraparteGarantia",
"IdOrigenGarantia",
"NombreConcepto",
"NombreCliente",
"IdMonedaInstrumento",
"DocumentoIdentidad",
"CodigoPortafolio",
"IdClasificacionTipoInstrumento",
"Nemotecnico",
"IdTipoInstrumento",
"NombreTipoInstrumento",
"NombreOrigenGarantia",
"RazonSocialContraparte"