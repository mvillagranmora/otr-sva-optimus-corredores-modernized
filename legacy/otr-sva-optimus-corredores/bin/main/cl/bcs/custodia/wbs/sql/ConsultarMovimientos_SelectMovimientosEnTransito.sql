SELECT 
"IdMovimientoCarteraEnTransito",
"EsNominal",
"NombreEstado",
"CodigoPortafolio",
"FechaIngresoRegistro",
"FechaLiquidacion",
"IdTipoOperacion",
"NombreConcepto",
"Nemotecnico",
"IdCondicionLiquidacion",
"Cantidad",
"IdMonedaInstrumento",
"MontoCompraMonedaInstrumento",
"DocumentoIdentidad",
"NombreCliente",
"IdTipoReajuste",
"PrecioCompra",
"MontoCompra",
"NombreBoveda",
"IdMatrizAgrupador",
"NemoSVS",
"IdTipoInstrumento",
"TipoInstrumento"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" CMCT
WHERE 
(:IdTipoInstrumento IS NULL OR CMCT."IdTipoInstrumento" = :IdTipoInstrumento) 
AND CMCT."FechaOperacion" BETWEEN :FechaInOperacion AND :FechaTerOperacion
AND (:DocumentoIdentidad IS NULL OR CMCT."DocumentoIdentidad" = :DocumentoIdentidad) 
AND (:CodigoPortafolio IS NULL OR CMCT."CodigoPortafolio" = :CodigoPortafolio) 
AND (:IdClasificacionTipoInstrumento IS NULL OR CMCT."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento)
AND (:Nemotecnico IS NULL OR CMCT."Nemotecnico" = :Nemotecnico)
AND (:IdEstado IS NULL OR CMCT."IdEstado" = :IdEstado)
AND (:IdMonedaInstrumento IS NULL OR CMCT."IdMonedaInstrumento" = :IdMonedaInstrumento)
AND (:IdTipoOperacion IS NULL OR CMCT."IdTipoOperacion" = :IdTipoOperacion)
AND (:Concepto IS NULL OR CMCT."IdConcepto" = :Concepto)
AND (:IdMatrizAgrupador IS NULL OR CMCT."IdMatrizAgrupador" = :IdMatrizAgrupador)
