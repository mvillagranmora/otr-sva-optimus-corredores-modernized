SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" AS
 SELECT FACT."IdComprobante"
	   ,FACT."FechaComprobante"
	   ,FACT."IdCliente"
	   ,FACT."NombreLargoCliente"			AS "NombreCliente"
	   ,FACT."DocumentoIdentidad"
	   ,FACT."IdPortafolio"
	   ,POR."CodigoPortafolio"
	   ,FACT."IdMoneda"
	   ,FACT."MontoLiquidacion"
	   ,FACT."IdClasificacionTipoInstrumento"
	   ,FACT."ModPrefFact"
	   ,FACT."IdTipoComprobante"
	   ,FACT."IdTipoOperacion"
 	   ,Ind."DetalleIndicador" 						AS "ModoFacturacion"
	   ,C."IdContratoComision"
	   ,FACT."IdConcepto"
	   ,C."Nombre"									AS "NombreConvenio"
	   ,C."IdTipoContrato"
	   ,COALESCE(ROUND(DER."MontoDerecho"),0) 		AS "MontoDerecho"
	   ,COALESCE(ROUND(COM."MontoComision"),0)		AS "MontoComision"	   
	   ,FACT."IdEmpresa" 
   FROM "BSCL.Facturacion.BD::FAC.Comprobante" 		AS FACT
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 	AS POR ON ( POR."IdPortafolio" = FACT."IdPortafolio" )
  INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS C ON 
      ( C."IdCliente" 						= FACT."IdCliente" 
    AND C."IdClasificacionTipoInstrumento" 	= FACT."IdClasificacionTipoInstrumento" 
    AND C."IdTipoContrato"					= 'CONV'
	AND C."IdEstado"						= 'ACT' )  
   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS Ind ON Ind."Codigo" = FACT."ModPrefFact"  
  INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV ON
      ( MOV."IdComprobante" = FACT."IdComprobante" )
    LEFT JOIN ( SELECT "IdComprobante", "IdCliente", "IdPortafolio", SUM("MontoCobro") AS "MontoDerecho"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle"
			   WHERE "CodigoCobroClasificacion" = 'DERECHO'
			   GROUP BY "IdComprobante", "IdCliente", "IdPortafolio" ) AS DER ON 
		    ( DER."IdComprobante" 	= FACT."IdComprobante" 
		  AND DER."IdCliente" 		= FACT."IdCliente" 
		  AND DER."IdPortafolio" 	= FACT."IdPortafolio" )        
  LEFT JOIN ( SELECT "IdComprobante", "IdCliente", "IdPortafolio", SUM("MontoCobro") AS "MontoComision"
			    FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle"
			   WHERE "CodigoCobroClasificacion" = 'COMISION'
			   GROUP BY "IdComprobante", "IdCliente", "IdPortafolio" ) AS COM ON 
		    ( COM."IdComprobante" 	= FACT."IdComprobante" 
		  AND COM."IdCliente" 		= FACT."IdCliente" 
		  AND COM."IdPortafolio" 	= FACT."IdPortafolio" ) 
  WHERE FACT."ModPrefFact" = 'MODPREFCONV';

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
