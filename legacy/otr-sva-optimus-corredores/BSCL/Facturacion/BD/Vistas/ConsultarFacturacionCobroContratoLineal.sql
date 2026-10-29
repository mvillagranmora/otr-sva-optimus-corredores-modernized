SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal" AS  
  -- Obtiene Monto y Porcentaje para el calculo de comision y derecho por monto unico o lineal
  SELECT  DISTINCT FACT."IdContratoComision"
		 ,FACT."IdCobro"
		 ,FACT."EsPorRangos"
		 ,FACT."IdConcepto"
		 ,COALESCE(SDC."Monto",0)		AS "Monto"
		 ,COALESCE(SDC."Porcentaje",0)	AS "Porcentaje"
   FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS FACT
   LEFT JOIN "BSCL.Plataforma.BD::COM.SubdetalleContratoComision" AS SDC ON 
      ( SDC."IdDetalleContratoComision" = FACT."IdDetalleContratoComision" 
    AND FACT."EsPorRangos" = 'N' );

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
