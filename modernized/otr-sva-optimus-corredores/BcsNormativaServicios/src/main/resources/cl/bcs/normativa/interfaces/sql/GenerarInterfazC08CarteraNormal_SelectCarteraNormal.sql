SELECT  car."Nemotecnico"                               AS "Nemotecnico", 
        car."INGMontoMercadoActualMonedaInstrumento"    AS "ValorMer",
        car."IdCartera"                                 AS "IdCartera", 
        car."FolioCartera"                              AS "FolioCartera", 
        car."INGTasaMercadoActual"                      AS "TasaMercadoActual", 
        car."INGTasaCompra"                             AS "TasaCompra", 
        car."FechaVencimientoOperacion"                 AS "FechaVencimiento",
        car."IdClasificacionTipoInstrumento"            AS "TipoInstrumento",
        car."IdTipoReajuste"                            AS "TipoMoneda",
        car."INGMontoATasaCompraActual"                 AS "ValorPresente",
        car."INGTasaCompra"                             AS "TasaCompra",
        car."INGCantidad"                               AS "Cantidad",
        car."INGPrecioPromedioCompra"                   AS "PrecioPromedio",
        COALESCE(gar."INGCantidad",0)                   AS "CantidadGarantia",
        car."IdMonedaInstrumento"                       AS "MonedaPago"   
FROM "BSCL.Custodia.BD::CUS.Cartera" 								car 
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" 						gar	ON car."IdCartera"			= gar."IdCarteraOrigen"
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa"  		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio ON ( Cambio."IdMoneda"  = car."IdTipoReajuste" AND Cambio."Fecha"  = :FiltroFecha )
WHERE  	car."IdEstado" 			= 'VIG' 
AND 	car."IdCliente" 		= SOC."IdCliente" 
AND	 	car."IdClasificacionTipoInstrumento"	IN ('RF','IF')
AND 	car."IdEmpresa" 		= :IdEmpresa
AND 	car."IdTipoInstrumento"  NOT IN ('OSA')  
AND 	car."FechaOperacion" 	<= :FiltroFecha
			
UNION

SELECT  
  car."Nemotecnico"                                 AS "Nemotecnico", 
  car."INGMontoMercadoActualMonedaInstrumento"      AS "ValorMer",
  car."IdCartera"                                   AS "IdCartera", 
  car."FolioCartera"                                AS "FolioCartera", 
  car."INGTasaMercadoActual"                        AS "TasaMercadoActual", 
  car."INGTasaCompra"                               AS "TasaCompra", 
  car."FechaVencimientoInstrumento"                 AS "FechaVencimiento",
  car."IdClasificacionTipoInstrumento"              AS "TipoInstrumento",
  car."IdTipoReajuste"                              AS "TipoMoneda",
  car."INGMontoATasaCompraActual"                   AS "ValorPresente",
  car."INGTasaCompra"                               AS "TasaCompra",
  car."INGCantidad"                                 AS "Cantidad",
  car."INGPrecioPromedioCompra"                     AS "PrecioPromedio",
  0                                                 AS "CantidadGarantia",
  car."IdMonedaInstrumento"                         AS "MonedaPago"
FROM "BSCL.Custodia.BD::CUS.Cartera" 		car 
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa"  		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" 						gar		ON car."IdCartera"			= gar."IdCarteraOrigen"
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio ON ( Cambio."IdMoneda"  = car."IdTipoReajuste" AND Cambio."Fecha"  = :FiltroFecha )
WHERE  	car."IdEstado" 							= 'VIG'
AND 	car."IdCliente" 						= SOC."IdCliente" 
AND	 	car."IdClasificacionTipoInstrumento"	IN ('RF','IF')
AND 	car."IdEmpresa" 						= :IdEmpresa
AND 	gar."IdGarantia" 						IS null
AND 	car."IdTipoInstrumento"  				NOT IN ('OSA')  
AND 	car."FechaOperacion" 					<= :FiltroFecha



UNION ALL		 
		 
SELECT  ffmm."IdSerieFondo" 	AS "Nemotecnico",
		ffmm."Monto" 			AS "ValorMer",
		0 						AS "IdCartera", 
		0 						AS "FolioCartera", 
		0.0 					AS "TasaMercadoActual", 
		0.0 					AS "TasaCompra", 
		ffmm."FechaLiquidacion" AS "FechaVencimiento",
		ffmm."IdSerieFondo" 	AS "TipoInstrumento" ,
		ffmm."IdMoneda" 		AS "TipoMoneda",
		ffmm."Monto"            AS "ValorPresente",
        0.0                     AS "TasaCompra",
        0.0                     AS "Cantidad",
        0.0                     AS "PrecioPromedio",
		0.0                     AS "CantidadGarantia",
		'CLP'					AS "MonedaPago"	
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" ffmm
		WHERE "TipoSolicitud" IN ('RES')  --INCLUIR TODOS LOS RESCATE (SORE EN SEBRA)	 
		AND ffmm."Estado"   IN ('CRT') 
	    AND ffmm."IdMoneda" IN ('CLP')
		AND ffmm."IdEmpresa" = :IdEmpresa
		AND ffmm."FechaEmision"	<= :FiltroFecha
		ORDER BY "Nemotecnico";