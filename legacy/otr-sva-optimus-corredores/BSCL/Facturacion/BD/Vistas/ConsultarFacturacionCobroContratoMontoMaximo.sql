SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoMontoMaximo"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoMontoMaximo" AS
-- Obtiene el Monto Maximo por Tipo de Cobro
SELECT FACT."IdContratoComision"
	  ,FACT."IdCobro"
	  ,FACT."IdDetalleContratoComision"
	  ,COALESCE(SDC."Monto", 0)			AS "Monto"
	  ,COALESCE(SDC."Porcentaje", 0)	AS "Porcentaje"
	  ,COALESCE(SDC."MontoMaximo", 0)	AS "MontoMaximo"
  FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS FACT
    LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON 
     ( FACT."IdDetalleContratoComision" = SDC."IdDetalleContratoComision" 
       AND FACT."EsPorRangos" = 'S' )
    INNER JOIN ( SELECT MAX(SDC."MontoMaximo") AS "MontoMaximo"
    			 	  ,FACT."IdCobro"
		  			  ,FACT."IdDetalleContratoComision"
    			 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS FACT
		    LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON 
     				( FACT."IdDetalleContratoComision" = SDC."IdDetalleContratoComision" 
       			  AND FACT."EsPorRangos" = 'S' )
       			GROUP BY FACT."IdCobro"
		  		        ,FACT."IdDetalleContratoComision" ) AS MAX_MONTO ON 
		  		    ( MAX_MONTO."MontoMaximo" = SDC."MontoMaximo"
		  		  AND MAX_MONTO."IdCobro" = FACT."IdCobro"
		  		  AND MAX_MONTO."IdDetalleContratoComision" = FACT."IdDetalleContratoComision" );

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoMontoMaximo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
