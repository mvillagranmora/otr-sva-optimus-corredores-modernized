SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorRut"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorRut" AS
-- COMPROBANTES CON CONTRATOS AGRUPADO POR RUT DEL CONTRATO
SELECT FACT."IdComprobante"
      ,FACT."IdCliente"
	  ,FACT."NombreLargoCliente"	AS "NombreCliente"
	  ,FACT."DocumentoIdentidad"
	  ,POR."IdPortafolio"
	  ,POR."CodigoPortafolio"
	  ,FACT."IdMoneda"
	  ,FACT."ModPrefFact"
	  ,IND."DetalleIndicador" 		AS "ModoFacturacion"
	  ,FACT."IdClasificacionTipoInstrumento"
	  ,CONT."IdContratoComision"
	  ,CONT."Nombre"				AS "NombreConvenioContrato"	  
	  ,CONT."IdFormatoCobro"
	  ,FACT."IdConcepto"
	  ,CONC."Nombre"				AS "NombreConcepto"
	  ,FACT."MontoLiquidacion"
	  ,FACT."IdEmpresa"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS FACT
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON ( CLI."IdCliente" = FACT."IdCliente" )
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON ( POR."IdPortafolio" = FACT."IdPortafolio" )
INNER JOIN ( SELECT DISTINCT CONT."IdContratoComision", CONT."Nombre", CONT."IdFormatoCobro", CONT."IdMoneda", CONT."IdCliente",CONT."CodigoArea", CONT."IdSegmentoCliente", CONT."IdPortafolio", CONT."IdClasificacionTipoInstrumento", CONTDET."IdConcepto" 
		  	  FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS CONT
             INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS CONTDET ON ( CONTDET."IdContratoComision" = CONT."IdContratoComision"  )	
             WHERE CONT."IdTipoContrato" = 'CONT'
               AND CONT."IdFormatoCobro" = 'AGRRUT'
			   AND CONT."IdEstado" 		 = 'ACT') AS CONT ON ( CONT."IdMoneda" = FACT."IdMoneda" AND 
                   												  CONT."IdCliente" = FACT."IdCliente" AND
                   												  CONT."IdClasificacionTipoInstrumento" = FACT."IdClasificacionTipoInstrumento"
                   												  )
 
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS IND ON IND."Codigo" = FACT."ModPrefFact"
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONC ON CONC."IdConcepto" = FACT."IdConcepto" 
 WHERE CONT."IdFormatoCobro" = 'AGRRUT'
   AND FACT."ModPrefFact"    = 'MODPREFCONT'
ORDER BY FACT."IdComprobante";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorRut" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
