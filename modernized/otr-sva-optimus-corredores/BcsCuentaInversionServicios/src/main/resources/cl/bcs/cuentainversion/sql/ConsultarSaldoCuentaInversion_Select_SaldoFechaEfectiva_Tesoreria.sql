
WITH
    MovimientosTesoreria AS 
    (
	     SELECT
	     IE."IdPortafolioTitularPago" as "IdPortafolio"
	     ,IE."IdMoneda"
	     ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "Monto"
	     ,:p_FechaDesde AS "FechaSaldo"
	     FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" as CI
	     INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
	        ON CI."IdPortafolio"    = IE."IdPortafolioTitularPago"
	        AND CI."IdMoneda"        = IE."IdMoneda"
	     WHERE
	     IE."IdEstado"           = 'ING'
	     AND CI."IdTrabajo" = :IdTrabajo
	     AND IE."FechaLiquidacion"   >= :p_FechaDesde
	     AND IE."IdEmpresa"          = :p_IdEmpresa
     ),
     ComprobanteTesoreria AS
     (
	      SELECT
	      COM."IdPortafolioTitularPago" as "IdPortafolio"
	      ,COM."IdMoneda"
	      ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "Monto"
	      ,:p_FechaDesde AS "FechaSaldo"
	      FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" as CI
	      INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante"  AS COM  
	        ON CI."IdPortafolio"    = COM."IdPortafolioTitularPago"
	        AND CI."IdMoneda"        = COM."IdMoneda"
	      WHERE
	      COM."IdEstado"          NOT IN ('ANU','MOD','LIQ','GIR')
	      AND CI."IdTrabajo" = :IdTrabajo
	      AND COM."FechaLiquidacion"  >= :p_FechaDesde
	      AND COM."IdEmpresa"         = :p_IdEmpresa
      )
SELECT "IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
FROM
    (
     SELECT "IdPortafolio","IdMoneda","Monto","FechaSaldo"
     FROM MovimientosTesoreria
     UNION ALL
     SELECT "IdPortafolio","IdMoneda","Monto","FechaSaldo"
     FROM ComprobanteTesoreria
    )
GROUP BY "IdPortafolio","FechaSaldo","IdMoneda";
