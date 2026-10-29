SELECT 
'0' 					AS "Folio",
"IdSerieFondo" 			AS "Nemotecnico",
"Monto" 				AS "ValorMercado",
'CLP' 					AS "TipoMoneda",
'0' 					AS "Banda", 
"FechaLiquidacion" 		AS "FechaVencimiento",
"Monto" 				AS "ValorPresente",
0.0 					AS "TasaMercado",
0.0 					AS "TasaCompra",
0.0 					AS "PrecioPromedio",
0.0 					AS "Cantidad",
0.0 					AS "CantidadGarantia",
"IdSerieFondo" 			AS "TipoInstrumento" 
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 
WHERE ("TipoSolicitud" IN ('RES') AND "IdMoneda" IN ('CLP'))
AND "Estado" 			= 'CRT' 
AND "IdEmpresa" 		= :IdEmpresa
AND "FechaEmision" 		<= :FiltroFecha
AND "IdCliente" 		= :IdCliente

UNION ALL

SELECT 
CAR."FolioCartera" 											as Folio,
CAR."Nemotecnico"											AS "Nemotecnico", 
CAR."INGMontoMercadoActualMonedaInstrumento" 				AS "ValorMercado",
CASE 
	WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF') THEN CAR."IdTipoReajuste"
	WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF') THEN CAR."IdTipoReajuste"
END 														AS "TipoMoneda",
TO_NVARCHAR(DAYS_BETWEEN(:FiltroFecha,CAR."FechaVencimientoInstrumento"))AS "Banda",
CASE 
  WHEN car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF','FWD') THEN car."FechaVencimientoOperacion" 
  ELSE car."FechaVencimientoInstrumento"
END 														AS "FechaVencimiento",
CAR."INGMontoATasaMercadoActual" 							AS "ValorPresente",
CAR."INGTasaMercadoActual" 									AS "TasaMercado",
CAR."INGTasaCompra" 										AS "TasaCompra",
CAR."INGPrecioPromedioCompra" 								AS "PrecioPromedio",
CAR."INGCantidad" 											AS "Cantidad",
COALESCE(GAR."INGCantidad",0) 								AS "CantidadGarantia",
CAR."IdTipoInstrumento" 									AS "TipoInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" 		CAR 
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa"  		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 
LEFT JOIN "BSCL.Custodia.BD::HIS.Garantia" 								GAR ON CAR."IdCartera" 			= GAR."IdCarteraOrigen"
WHERE  	car."IdEstado" 							= 'VIG'
AND 	car."IdCliente" 						= SOC."IdCliente" 
AND	 	car."IdClasificacionTipoInstrumento"	IN ('RF','IF')
AND 	car."IdEmpresa" 						= :IdEmpresa
AND 	car."IdTipoInstrumento"  				NOT IN ('OSA')  
AND 	car."FechaOperacion" 					<= :FiltroFecha
--AND 	car."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC')
AND 	car."FechaRespaldo"  = :FiltroFecha
AND     GAR ."FechaRespaldo"  = :FiltroFecha

UNION ALL

SELECT 
CAR."FolioCartera" 											as Folio,
CAR."Nemotecnico"											AS "Nemotecnico", 
CAR."INGMontoMercadoActualMonedaInstrumento" 				AS "ValorMercado",
CASE 
	WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF') THEN CAR."IdTipoReajuste"
	WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF') THEN CAR."IdTipoReajuste"
END 														AS "TipoMoneda",
TO_NVARCHAR(DAYS_BETWEEN(:FiltroFecha,  CAR."FechaVencimientoInstrumento" ))AS "Banda",
CASE 
  WHEN CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF','FWD') THEN CAR."FechaVencimientoOperacion" 
  ELSE CAR."FechaVencimientoInstrumento"
END 														AS "FechaVencimiento",
CAR."INGMontoATasaMercadoActual" 							AS "ValorPresente",
CAR."INGTasaMercadoActual" 									AS "TasaMercado",
CAR."INGTasaCompra" 										AS "TasaCompra",
CAR."INGPrecioPromedioCompra" 								AS "PrecioPromedio",
CAR."INGCantidad" 											AS "Cantidad",
0															AS "CantidadGarantia",
CAR."IdTipoInstrumento" 									AS "TipoInstrumento"
FROM "BSCL.Custodia.BD::HIS.Cartera" 		CAR 
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa"  		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 

WHERE  	CAR."IdEstado" 							= 'VIG'
AND 	CAR."IdCliente" 						= SOC."IdCliente" 
AND	 	CAR."IdClasificacionTipoInstrumento"	IN ('RF','IF')
AND 	CAR."IdEmpresa" 						= :IdEmpresa
AND 	CAR."IdTipoInstrumento"  				NOT IN ('OSA')  
AND 	CAR."FechaOperacion" 					<= :FiltroFecha
AND 	CAR."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC')
AND 	CAR."FechaRespaldo"  = :FiltroFecha
