SELECT 
car."IdCliente"
,car."FechaLiquidacion"
,CASE WHEN car."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'SIMULTANEAS' 
	 WHEN car."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN 'PACTOS'
	 WHEN car."IdClasificacionTipoInstrumento" IN ('DV') THEN TI."Descripcion"
END as "Item"
,car."IdMonedaSistema"
,CASE WHEN car."IdTipoOperacion" IN ('VENSIM','COMPAC') THEN -1*car."INGMontoATasaCompraActual"
 	  ELSE car."INGMontoATasaCompraActual"
END as "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS car
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI ON (TI."IdTipoInstrumento" = car."IdTipoInstrumento")
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" as CLIENTE_PASO ON CLIENTE_PASO."IdClientePaso" = car."IdCliente"
WHERE
CLIENTE_PASO."IdTrabajo" = :IdTrabajo
AND car."IdEmpresa" = :IdEmpresa
AND car."FechaLiquidacion" <= :FechaProyectadaDesde
AND car."IdEstado" = 'VIG'
AND (car."IdTipoOperacion" IN ('COMSIM','VENSIM','COMPAC','VENPAC')
OR car."IdClasificacionTipoInstrumento" IN ('DV'))