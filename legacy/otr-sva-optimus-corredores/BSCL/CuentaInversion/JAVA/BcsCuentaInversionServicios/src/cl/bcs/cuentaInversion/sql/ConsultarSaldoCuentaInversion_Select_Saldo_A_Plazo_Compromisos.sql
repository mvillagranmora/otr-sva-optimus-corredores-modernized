WITH
    OperacionesAPlazo AS
    (
      SELECT
      CAR."IdCliente"
      ,CAR."IdPortafolio"
      ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
      ,MAP(CAR."IdTipoOperacion",'EMPRES',-1*CAR."INGMontoATasaCompraActual",CAR."INGMontoATasaCompraActual" ) AS "Monto"
      ,:p_FechaDesde AS "FechaSaldo"                           
      FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" AS CI 
      INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
            ON CI."IdPortafolio"    = CAR."IdPortafolio"
            AND CI."IdMoneda"        = CAR."IdMonedaInstrumento"
      WHERE
          "IdTipoOperacion"           IN ('PRESTA','EMPRES')
          AND "IdTrabajo" 					= :IdTrabajo
          AND CAR."INGFechaLiquidacion"   >= :p_FechaDesde
          AND CAR."IdEmpresa"             = :p_IdEmpresa
          AND CAR."IdEstado"              = 'VIG'
          AND CAR."IdTipoPrestamo"        <> 'PRTVTC'   -- NO deben ser prestamos Venta Corta
     ),
     OperacionesAPlazoPactos AS
     (
      SELECT
      CAR."IdCliente"
      ,CAR."IdPortafolio"
     ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') 
     	   THEN (CASE WHEN CAR."IdTipoReajuste" ='UF' THEN 'CLP' ELSE  CAR."IdTipoReajuste" END)
     	   ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
	  ,MAP(CAR."IdTipoOperacion",'COMPAC',-1*CAR."INGMontoAPlazoMonedaInstrumento",CAR."INGMontoAPlazoMonedaInstrumento" ) AS "Monto"
      ,:p_FechaDesde AS "FechaSaldo"                           
      FROM  "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" as CI
      INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR
           ON CI."IdPortafolio"    = CAR."IdPortafolio"
           AND CI."IdMoneda"       = CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') 
							     	 THEN (CASE WHEN CAR."IdTipoReajuste" ='UF' THEN 'CLP' ELSE  CAR."IdTipoReajuste" END)
							     	 ELSE CAR."IdMonedaInstrumento" END
      WHERE
           "IdTipoOperacion"           IN ('COMPAC','VENPAC')
           AND CAR."INGFechaLiquidacion"   >= :p_FechaDesde
           AND CAR."IdEmpresa"             = :p_IdEmpresa
           AND CAR."IdEstado"              = 'VIG'
       )
       SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
       FROM
       (                       
         SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
         FROM OperacionesAPlazo
         UNION ALL
         SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" 
         FROM OperacionesAPlazoPactos
       )
GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";    
