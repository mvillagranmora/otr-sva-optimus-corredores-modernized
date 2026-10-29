SELECT 
car."IdCliente"
,car."IdPortafolio"
,car."FechaLiquidacion"
,CASE WHEN car."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'SIMULTANEAS' 
	 WHEN car."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN 'PACTOS'
	 WHEN car."IdClasificacionTipoInstrumento" IN ('DV') THEN TI."Descripcion"
END as "Item"
,car."IdMonedaInstrumento"
,car."IdMonedaSistema"
,CASE WHEN car."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."MontoAPlazo"	  
	  WHEN car."IdTipoOperacion" IN ('COMFWD','VENFWD') THEN 0-- -1*car."INGMontoATasaCompraActual"
	  WHEN car."IdTipoOperacion" IN ('VENPAC') THEN car."MontoAPlazo" 	  
 	  ELSE car."INGMontoATasaCompraActual"
END as "MontoMonedaSistema"
,car."IdMonedaAdicional"
,CASE WHEN car."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."INGMontoCompraActualMonedaAdicional"
	  ELSE car."INGMontoCompraActualMonedaAdicional"
END as "MontoMonedaAdicional" 
FROM "BSCL.Custodia.BD::HIS.Cartera" AS car
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON car."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
WHERE car."IdEmpresa" = :IdEmpresa
AND CASE 
		WHEN car."IdTipoOperacion" IN ('COMFWD','VENFWD') THEN car."FechaVencimientoOperacion" 
		ELSE car."FechaLiquidacion" 
	END <= :FechaProyectadaDesde
AND car."FechaRespaldo" = :FechaHasta	
AND car."IdEstado" = 'VIG'
AND (car."IdTipoOperacion" IN ('COMSIM','VENSIM')
OR (car."IdClasificacionTipoInstrumento" = 'DV' and car."IdTipoInstrumento" NOT IN ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO','DER'))
OR car."IdTipoInstrumento" = 'PAC')


UNION ALL

SELECT 
car."IdCliente"
,car."IdPortafolio"
,car."FechaLiquidacion"
,CASE 
	WHEN GT."IdGrupoProducto" = 'FU' THEN 'FUTUROS'
	WHEN GT."IdGrupoProducto" = 'OP' THEN 'OPCIONES'
END 											AS "Item"	
,car."IdMonedaInstrumento"
,car."IdMonedaSistema"
,car."MontoAPlazo" 			AS "MontoMonedaSistema"
,car."IdMonedaAdicional"
,car."INGMontoCompraActualMonedaAdicional" 	AS "MontoMonedaAdicional" 
FROM "BSCL.Custodia.BD::HIS.Cartera" AS car
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON car."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" 			AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
INNER JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"	AS GT ON (GT."IdTipoInstrumento" = car."IdTipoInstrumento")

WHERE  car."IdEmpresa" 		= :IdEmpresa
AND car."FechaLiquidacion" <= :FechaProyectadaDesde
AND car."FechaRespaldo" = :FechaHasta
AND car."IdClasificacionTipoInstrumento" = 'DV'
AND car."IdEstado" = 'VIG'
AND GT."IdGrupoProducto" IN ('FU','OP')

UNION ALL 

SELECT 
	 car."IdCliente"
	,car."IdPortafolio"
	,car."FechaLiquidacion"
	,'DEPOSITOS' AS "Item"	
	,car."IdTipoReajuste" AS "IdMonedaInstrumento"
	,car."IdMonedaSistema"
	,car."INGValorRescate"			AS "MontoMonedaSistema"
	,car."IdMonedaAdicional"
	,car."INGMontoCompraActualMonedaAdicional" 	AS "MontoMonedaAdicional" 
FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS P ON CAR."IdPortafolio" = P."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"  AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
WHERE car."IdEmpresa" 		= :IdEmpresa
AND car."FechaVencimientoInstrumento" <= :FechaProyectadaDesde
AND car."FechaRespaldo" = :FechaHasta
AND car."IdClasificacionTipoInstrumento" = 'IF'
AND car."IdEstado" = 'VIG'
AND car."IdTipoInstrumento" != 'PAC' 

