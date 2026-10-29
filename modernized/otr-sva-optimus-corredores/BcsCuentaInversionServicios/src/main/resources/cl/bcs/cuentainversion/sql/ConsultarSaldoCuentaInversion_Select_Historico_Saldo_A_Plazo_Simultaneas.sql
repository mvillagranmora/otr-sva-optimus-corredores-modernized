WITH
    OperacionesAPlazoSimultaneas AS
    (
     SELECT
     CAR."IdCliente"
     ,CAR."IdPortafolio"
     ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
     ,MAP(CAR."IdTipoOperacion",'VENSIM',-1*CAR."INGMontoAPlazo",CAR."INGMontoAPlazo" ) AS "Monto"
     ,:p_FechaDesde AS "FechaSaldo"     
     FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" AS CI
     INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR
     	ON CI."IdPortafolio"   = CAR."IdPortafolio"
		AND CI."IdMoneda"       = CAR."IdMonedaInstrumento"
     WHERE
		"IdTipoOperacion"                   IN ('COMSIM','VENSIM')
		AND CI."IdTrabajo" = :IdTrabajo
	    AND ((CAR."FechaLiquidacion" <=  TO_DATE(:p_FechaDesde, 'YYYY-MM-DD') AND CAR."IdClasificacionTipoInstrumento" = 'RV' )
		OR(  CAR."FechaVencimientoInstrumento"  <= TO_DATE(:p_FechaDesde, 'YYYY-MM-DD') AND CAR."IdClasificacionTipoInstrumento" != 'RV'))
		AND CAR."IdEmpresa"                     = :p_IdEmpresa
		AND CAR."IdEstado"                      = 'VIG'
		AND CAR."FechaRespaldo"					= TO_DATE(:p_FechaDesde, 'YYYY-MM-DD')
     )
SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
FROM OperacionesAPlazoSimultaneas
GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
