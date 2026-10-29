SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorPortafolio"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorPortafolio" AS
-- CONTRATO AGRUPADO POR PORTAFOLIO
SELECT "IdComprobante"
      ,"IdCliente"
	  ,"NombreCliente"
	  ,"DocumentoIdentidad"
	  ,"IdPortafolio"
	  ,"CodigoPortafolio"
	  ,"IdMoneda"
	  ,"ModPrefFact"
	  ,"ModoFacturacion"
	  ,"IdClasificacionTipoInstrumento"
	  ,"IdContratoComision"
	  ,"NombreContrato"						AS "NombreConvenioContrato"	
	  ,"IdConceptoFactura"
	  ,"IdFormatoCobro"
	  ,COALESCE("MontoDerecho",0) + 
	   COALESCE("MontoComision",0)			AS "Monto"	  
	  ,COALESCE("MontoDerecho",0)			AS "MontoDerecho"
	  ,COALESCE("MontoComision",0) 			AS "MontoComision"
	  ,"IdEmpresa"
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContrato"
WHERE "IdFormatoCobro"		= 'DETPOR'
  AND "IdConceptoFactura" 	= "IdConceptoContrato"

 UNION ALL	    
-- CONTRATO AGRUPADO POR RUT
SELECT CONT."IdComprobante"
	  ,CONT."IdCliente"
	  ,CONT."NombreCliente"
	  ,CONT."DocumentoIdentidad"
	  ,CONT."IdPortafolioContrato"  			AS "IdPortafolio"
	  ,POR."CodigoPortafolio"
	  ,CONT."IdMoneda"
	  ,CONT."ModPrefFact"
	  ,CONT."ModoFacturacion"
	  ,CONT."IdClasificacionTipoInstrumento"
	  ,CONT."IdContratoComision"
	  ,CONT."NombreContrato"					AS "NombreConvenioContrato"	  
	  ,CONT."IdConceptoFactura"
	  ,"IdFormatoCobro"
	  ,COALESCE(CONT."MontoDerecho",0) + 
	   COALESCE(CONT."MontoComision",0)			AS "Monto"	  
	  ,COALESCE(CONT."MontoDerecho",0)			AS "MontoDerecho"
	  ,COALESCE(CONT."MontoComision",0)			AS "MontoComision"
	  ,"IdEmpresa"
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContrato" AS CONT
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CONT."IdPortafolioContrato"
WHERE CONT."IdFormatoCobro" 	= 'AGRRUT'
  AND CONT."IdConceptoFactura" 	= CONT."IdConceptoContrato"
ORDER BY "NombreCliente"
        ,"ModoFacturacion";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorPortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
