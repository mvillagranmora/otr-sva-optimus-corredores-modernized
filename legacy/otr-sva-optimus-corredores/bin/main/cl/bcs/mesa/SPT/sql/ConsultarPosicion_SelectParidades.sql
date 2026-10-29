SELECT 
PTC."IdParidadYTipoCambio"
,PTC."IdMoneda"
,PTC."NombreMoneda"
,PTC."Fecha"
,PTC."IdEstado"
,PTC."Origen"
,PTC."IdUsuarioIngreso"
,PTC."ParidadMercado"
,PTC."ParidadBID"
,PTC."ParidadASK"
,PTC."LimiteSuperiorParidad"
,PTC."LimiteInferiorParidad"
,PTC."ParidadSBIF"
,PTC."TipoCambioMercado"
,PTC."LimiteSuperiorTipoCambio"
,PTC."LimiteInferiorTipoCambio"
,PTC."PrecioSpotCompra"
,PTC."PrecioSpotVenta"
,PTC."PrecioSpotPromedio"
,COALESCE(TIPMON."MultiplicaODivide",'') AS "MultiplicaODivide"
FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PTC
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (PTC."IdMoneda" = MON."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TIPMON ON (MON."IdTipoMoneda" = TIPMON."IdTipoMoneda")
WHERE PTC."Fecha" = :FechaOperacion 
AND PTC."IdEstado" = 'ACT';