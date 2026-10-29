SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango" AS
-- Obtiene Monto y Porcentaje por Rango de MontoMaximo
SELECT DISTINCT
	   FACT."IdComprobante"
	  ,FACT."IdDetalleContratoComision"
	  ,FACT."IdContratoComision"
	  ,FACT."IdCobro"
	  ,FACT."IdConcepto"
	  ,COALESCE(SDC."Monto", 0) 	  AS "Monto"
	  ,COALESCE(SDC."Porcentaje", 0)  AS "Porcentaje"
	  ,COALESCE(SDC."MontoMaximo", 0) AS "MontoMaximo"
  FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS FACT
  LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON 
     ( SDC."IdDetalleContratoComision" = FACT."IdDetalleContratoComision" 
   AND FACT."EsPorRangos" = 'S' )
   -- obtiene el minimo MontoMaximo mayor a MontoLiquidacion, si el MontoLiquidacion
   -- es mayor a los Montos Maximos entonces considera el MAX(MontoMaximo) por Tipo de Cobro
 INNER JOIN ( SELECT COALESCE(MIN(SDC."MontoMaximo"), MAX_MONTO."MontoMaximo") AS "MontoMaximo"
				    ,FACT."IdCobro"
  					,FACT."IdDetalleContratoComision"
  					,FACT."IdComprobante"
  					,FACT."MontoLiquidacion"
  				FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS FACT
		   LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON 
                   ( SDC."IdDetalleContratoComision" = FACT."IdDetalleContratoComision" 
                 AND FACT."EsPorRangos" = 'S' AND SDC."MontoMaximo" > FACT."MontoLiquidacion") 
  		   LEFT JOIN ( SELECT DISTINCT "IdDetalleContratoComision"
    			 	  		 ,"IdCobro"
    				  		 ,"Monto"
    				  		 ,"Porcentaje"
                      		 ,"MontoMaximo"
    			  		 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoMontoMaximo" ) AS MAX_MONTO ON 
    			     		( MAX_MONTO."IdDetalleContratoComision" = FACT."IdDetalleContratoComision"
    			   		  AND MAX_MONTO."IdCobro" = FACT."IdCobro" )
		       WHERE FACT."EsPorRangos" = 'S'
    		GROUP BY MAX_MONTO."MontoMaximo"
    				,FACT."IdCobro"
  					,FACT."IdDetalleContratoComision"
  					,FACT."IdComprobante"
  					,FACT."MontoLiquidacion" ) AS MIN_MAX_MONTO ON
  				  ( MIN_MAX_MONTO."MontoMaximo" = SDC."MontoMaximo"
		  		AND MIN_MAX_MONTO."IdCobro" = FACT."IdCobro"
		  		AND MIN_MAX_MONTO."IdComprobante" = FACT."IdComprobante"
		  		AND MIN_MAX_MONTO."IdDetalleContratoComision" = FACT."IdDetalleContratoComision" );

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
