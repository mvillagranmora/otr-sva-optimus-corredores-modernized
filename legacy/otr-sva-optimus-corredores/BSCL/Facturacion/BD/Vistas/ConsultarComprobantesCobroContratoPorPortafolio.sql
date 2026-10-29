SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorPortafolio"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorPortafolio" AS
-- COMPROBANTES CON CONTRATOS AGRUPADO POR PORTAFOLIO
SELECT FACT."IdComprobante"
      ,FACT."IdCliente"
	  ,FACT."NombreLargoCliente"	AS "NombreCliente"
	  ,FACT."DocumentoIdentidad"
	  ,FACT."IdPortafolio"
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
INNER JOIN ( SELECT DISTINCT CONT."IdContratoComision", CONT."Nombre", CONT."IdFormatoCobro", CONT."IdMoneda", CONT."IdCliente",CONT."CodigoArea", CONT."IdSegmentoCliente", CONT."IdClasificacionTipoInstrumento", CONT."IdPortafolio", CONTDET."IdConcepto" 
		  	 FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS CONT
             INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS CONTDET ON ( CONTDET."IdContratoComision" = CONT."IdContratoComision"  )	
             WHERE CONT."IdTipoContrato" = 'CONT' 
               AND CONT."IdFormatoCobro" = 'DETPOR'
			   AND CONT."IdEstado"		 = 'ACT') AS CONT ON ( CONT."IdMoneda" = FACT."IdMoneda" AND 
                   												  CONT."IdCliente" = FACT."IdCliente" AND
                   												  CONT."IdClasificacionTipoInstrumento" = FACT."IdClasificacionTipoInstrumento" AND 
																  CONT."IdPortafolio" = FACT."IdPortafolio")
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS IND ON IND."Codigo" = FACT."ModPrefFact"
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONC ON CONC."IdConcepto" = FACT."IdConcepto" 
 WHERE CONT."IdFormatoCobro" = 'DETPOR'
   AND FACT."ModPrefFact"    = 'MODPREFCONT'
ORDER BY FACT."IdComprobante";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorPortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
