WITH COMPROBANTES AS 
(
	SELECT 
		 COM."IdPortafolioTitularPago",
  		 COM."IdMoneda",
  		 COM."IdTipoMovimiento",
  		 COM."Monto",
  		 COM."IdComprobante"
 	  	 FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" as CI
	     LEFT JOIN   "BSCL.Tesoreria.BD::TES.Comprobante"  AS COM  
	        ON COM."IdPortafolioTitularPago" = CI."IdPortafolio"    
	        AND CI."IdMoneda"        = COM."IdMoneda"
	      WHERE COM."IdEstado" IN ('GIR')
	      AND CI."IdTrabajo" = :IdTrabajo	      
	      AND COM."FechaLiquidacion"  >=  :p_FechaDesde
	      AND COM."IdEmpresa"         = :p_IdEmpresa
),
PARTIDAS_COMPROBANTES AS
(
	SELECT 
		CDO."IdComprobante" AS "ComprobanteRelacionado",
		Comprobante."IdComprobante" AS "ComprobantePartida", 
		Comprobante."IdEstado" AS "EstadoPartida"
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO
		INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = CD."IdComprobante"
		WHERE Comprobante."IdEstado" = 'ING'
		AND Comprobante."IdConcepto" IN ('PAREGR', 'PARING')
		AND Comprobante."FechaLiquidacion" BETWEEN :p_FechaDesde AND :p_FechaHasta
)

SELECT  COMPROBANTES."IdPortafolioTitularPago" as "IdPortafolio"
	    ,COMPROBANTES."IdMoneda"
	    ,CASE 
	      	WHEN COMPROBANTES."IdTipoMovimiento" = 'MOVEGR' THEN -1*COMPROBANTES."Monto" 
	      	ELSE COMPROBANTES."Monto" 
	     END AS "Monto"
	    ,:p_FechaDesde AS "FechaSaldo"
FROM COMPROBANTES
INNER JOIN PARTIDAS_COMPROBANTES ON COMPROBANTES."IdComprobante" = PARTIDAS_COMPROBANTES."ComprobanteRelacionado"