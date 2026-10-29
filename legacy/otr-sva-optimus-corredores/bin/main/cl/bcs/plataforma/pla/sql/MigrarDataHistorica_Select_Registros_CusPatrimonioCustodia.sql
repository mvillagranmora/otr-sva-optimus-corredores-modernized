SELECT "IdPatrimonioCustodia", "IdCliente", "IdPortafolio", "IdInstrumento", "Nemotecnico", "FechaPatrimonio", "TipoFechaConsulta", "CantidadPatrimonio", "MontoPatrimonio", "CantidadCustodia", 
"MontoMercadoCustodia", "CantidadGarantia", "MontoMercadoGarantia", "CantidadSimulCom", "MontoMercadoSimulCom", "CantidadSimulVen", "MontoMercadoSimulVen", "CantidadPrestamo", "MontoMercadoPrestamo", 
"CantidadEmpresti", "MontoMercadoEmpresti", "CantidadPactosV", "MontoMercadoPactosV", "CantidadPactosC", "MontoMercadoPactosC", "CantidadTranomEgr", "MontoMercadoTransEgrNom", "CantidadTranomIng", 
"MontoMercadoTransIngNom", "CantidadTransIng", "MontoMercadoTransIng", "CantidadTransEgr", "MontoMercadoTransEgr", "CantidadTransVenDesEgr", "PrecioMercadoCustodia", "IdRegimenTributario", "IdMonedaSistema", 
"IdEstado", "FechaFuente", "MontoComision", "IdEstadoComision", "PorcentajeCobroComision"
FROM "BSCL.Custodia.BD::CUS.PatrimonioCustodia"
WHERE CAST("FechaPatrimonio" AS DATE) BETWEEN :p_Desde AND :p_Hasta
ORDER BY "IdPatrimonioCustodia", "FechaPatrimonio" DESC
LIMIT :p_Limit OFFSET :p_Offset;