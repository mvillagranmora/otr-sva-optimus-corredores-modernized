SELECT 
"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdProcesamientoValorizaCustodiaMasiva",
SUB."IdSubyacente" AS "IdTablaOrigen",
'SUBYACENTE' AS "TablaOrigen",
'PENDIENTE' AS "EstadoProcesamiento",
SUB."INGFechaOperacion" AS "WS_FechaOperacion",
'S' AS "WS_ValorizaACompra",
COALESCE(SUB."ValorizaPor",'T') AS "WS_ValorizaPor",
SUB."IdClasificacionTipoInstrumento" AS "WS_IdClasificacionTipoInstrumento",
SUB."IdTipoInstrumento" AS "WS_IdTipoInstrumento",
SUB."IdInstrumento" AS "WS_IdInstrumento",
SUB."Nemotecnico" AS "WS_Nemotecnico",
SUB."IdEmisor" AS "WS_IdEmisor",
SUB."FechaEmisionInstrumento" AS "WS_FechaEmision",
SUB."FechaVencimientoOperacion" AS "WS_FechaVencimiento",
SUB."IdTipoReajuste" AS "WS_IdTipoReajuste",
SUB."INGCantidad" AS "WS_Cantidad",
SUB."INGValorRescate" AS "WS_ValorRescate",
NULL AS "WS_PrecioCompra",
NULL AS "WS_TasaCompra",
SUB."INGPrecioSubyacente" AS "WS_PrecioSubyacente",
SUB."INGTasaSubyacente" AS "WS_TasaSubyacente",
NULL AS "WS_PrecioGarantia",
NULL AS "WS_TasaGarantia",
SUB."TIFTasa" AS "WS_TasaTIF",
'SUBYACENTE' AS "WS_UsoPrecio",
NULL AS "WS_TipoCartera",
'NORMATIVO' AS "WS_IdTipoCriterioTasa",
SUB."TipoValorizacion" AS "WS_TipoValorizacion",
SUB."TieneCorteDeCupon" AS "WS_TieneCorteDeCupon",
CAR."IdCliente" AS "WS_IdCliente",		
CAR."IdPortafolio" AS "WS_IdPortafolio",	
SUB."INGMontoSubyacenteActualMonedaInstrumento" AS "WS_BK_INGMontoCompraActualMonedaInstrumento",	
SUB."INGMontoATasaSubyacenteActual" AS "WS_BK_INGMontoATasaCompraActual",	
SUB."INGMontoSubyacenteActualMonedaInstrumento" AS "WS_BK_INGMontoMercadoActualMonedaInstrumento",	
SUB."INGMontoATasaSubyacenteActual" AS "WS_BK_INGMontoATasaMercadoActual",
SUB."IdMonedaInstrumento" AS "WS_IdMonedaInstrumento",
SUB."MontoSubyacente" AS "WS_MontoCompra",
--0 AS "WS_PlazoEnDias",
SUB."IdMonedaEmision" AS "WS_IdMonedaEmision",
CAR."IdTipoOperacion" AS "WS_IdTipoOperacion",
RES."MontoLiquidacion"  AS "WS_MontoInicialSubyacente",
CAR."IdTipoReajuste" as "WS_IdTipoReajusteCabecera",
CAR_ORIGEN."TasaCompra" AS "WS_TasaCompraCarteraOrigen",
RES."Tasa" AS "WS_TasaCompraCarteraPacto"
FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
	ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES   
    ON SUB."FolioSubyacente" = RES."FolioSubyacente" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR_ORIGEN ON RES."IdCarteraOrigen" = CAR_ORIGEN."IdCartera"
WHERE CAR."IdEmpresa" = :IdEmpresa AND SUB."IdEstado" = 'VIG'
AND CAR."IdCliente" = CASE WHEN IFNULL(:IdCliente,0)=0 THEN CAR."IdCliente" ELSE :IdCliente END
AND CAR."IdPortafolio" = CASE WHEN IFNULL(:IdPortafolio,0)=0 THEN CAR."IdPortafolio" ELSE :IdPortafolio END
AND (CAR."IdTipoOperacion" = 'COMPAC' OR CAR."IdTipoOperacion" = 'VENPAC')
AND SUB."INGFechaOperacion" <=:fechaSistema
AND SUB."FechaVencimientoOperacion" >:fechaSistema;