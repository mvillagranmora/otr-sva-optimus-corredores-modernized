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
  COALESCE(gar."INGCantidad",0)                     AS "CantidadGarantia",
  car."IdMonedaInstrumento"                         AS "MonedaPago"
FROM "BSCL.Custodia.BD::HIS.Cartera" 								car 
INNER JOIN "BSCL.Custodia.BD::HIS.Garantia" 						gar ON (car."IdCartera"			= gar."IdCarteraOrigen" )
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa"  		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio ON ( Cambio."IdMoneda"  = car."IdTipoReajuste" AND Cambio."Fecha"  = :FiltroFecha )
WHERE  car."IdEstado" 		= 'VIG'
AND car."IdCliente" 		= SOC."IdCliente" 
AND car."IdEmpresa" 		= :IdEmpresa
AND car."IdTipoInstrumento"  NOT IN ('OSA')  
AND	 	car."IdClasificacionTipoInstrumento"	IN ('RF','IF')
AND CAR."FechaOperacion" 	<= :FiltroFecha
AND car."FechaRespaldo" 	= :FiltroFecha
AND gar."FechaRespaldo" 	= :FiltroFecha

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
FROM "BSCL.Custodia.BD::HIS.Cartera" 								car
LEFT JOIN  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			EMP ON EMP."IdEmpresa" 	 		= :IdEmpresa
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	SOC ON SOC."IdSocioDeNegocio"  	= EMP."IdSocioDeNegocio" 
INNER JOIN "BSCL.Custodia.BD::HIS.Garantia" 						gar ON (car."IdCartera"			= gar."IdCarteraOrigen" )
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"	Cambio ON ( Cambio."IdMoneda"  = car."IdTipoReajuste" AND Cambio."Fecha"  = :FiltroFecha )
WHERE  car."IdEstado" 			= 'VIG'
AND car."IdCliente" 			= SOC."IdCliente" 
AND car."IdClasificacionTipoInstrumento" 	IN ('RF','IF')
AND 	gar."IdGarantia" 						IS null
AND car."IdTipoInstrumento"  	NOT IN ('OSA')  
AND CAR."FechaOperacion" 		<= :FiltroFecha
AND car."FechaRespaldo" 		= :FiltroFecha

