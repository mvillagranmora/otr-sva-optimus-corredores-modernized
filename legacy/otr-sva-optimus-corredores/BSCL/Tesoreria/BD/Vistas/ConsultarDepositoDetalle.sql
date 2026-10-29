SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDepositoDetalle";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarDepositoDetalle" AS
SELECT 	 DD."IdDepositoDetalle"
		,DD."IdDeposito"
		,DD."IdTipoDeposito"
		,TD."Nombre" as "NombreTipoDeposito"
		,DD."IdComprobanteFormaDePago"
		,CFP."IdMedioDePago"
		,CFP."NombreMedioDePago"
		,CFP."IdModalidadDePago"
		,CFP."NombreModalidadDePago"
		
		,CFP."IdCuentaBancariaCorredora"
 		,CFP."IdTipoCuentaBancariaCorredora"
 		,CFP."NombreTipoCuentaBancariaCorredora"
 		,CFP."NombreCuentaBancariaCorredora"
		,CFP."NumeroCuentaBancariaCorredora"
 		,CFP."IdTarjetaBancariaCorredora"
 		,CFP."IdTipoTarjetaBancariaCorredora"
 		,CFP."NombreTipoTarjetaBancariaCorredora"
 		,CFP."NumeroTarjetaBancariaCorredora"
 		,CFP."IdChequera"
 		,CFP."NumeroDocumentoCorredora" 		
 		,CFP."FechaDocumentoPagoCorredora"
 		
 		,CFP."IdBancoSocio"
 		,BS."NombreBanco" AS "NombreBancoSocio"
		,CFP."IdCuentaBancariaSocio"
		,CFP."IdTipoCuentaBancariaSocio"
		,CFP."NombreTipoCuentaBancariaSocio"
		,CFP."NombreCuentaBancariaSocio"
		,CFP."NumeroCuentaBancariaSocio"
		,CFP."IdTarjetaBancariaSocio"
		,CFP."IdTipoTarjetaBancariaSocio"
		,CFP."NombreTipoTarjetaBancariaSocio"
		,CFP."NumeroTarjetaBancariaSocio"
		,CFP."NumeroSerieSocio"
		,CFP."NumeroDocumentoSocio"
		,CFP."FechaDocumentoPagoSocio"
		,CFP."IdMoneda"
		,M."Nombre" AS "NombreMoneda"
		,CFP."Monto"
		,C."IdCaja"
		,C."IdComprobante"

		,CASE WHEN "TitularPago" = 'N' THEN C."IdTipoPersonaTitularPago" ELSE C."IdTipoPersona" END AS "IdTipoPersona"
		,CASE WHEN "TitularPago" = 'N' THEN C."IdPersonaTitularPago" ELSE C."IdPersona" END AS "IdPersona"
		,CASE WHEN "TitularPago" = 'N' THEN C."NombrePersonaTitularPago" ELSE C."NombrePersona" END AS "NombrePersona"
		
		,C."IdConcepto"
		,C."FechaLiquidacion" AS "FechaLiquidacionComprobante"
 	 	,CFP."DiasRetencion"
		,CAST((CASE WHEN CFP."IdEstado" <> 'LIQ' THEN ADD_DAYS (TO_DATE (C."FechaLiquidacion", 'YYYY-MM-DD'), CFP."DiasRetencion") ELSE CFP."FechaHoraLiquidacion" END) AS DATE) AS "FechaLiquidacion"
		,DD."IdEstado"
		,DD."IdConcepto" AS "IdConceptoDeposito"
		,CON."Nombre" AS "NombreConceptoDeposito"
		,DD."Descripcion"
		,ET."NombreEstado"
  FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = CFP."IdComprobante"  
 INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as M ON M."IdMoneda" = CFP."IdMoneda"  
 INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" as ET ON ET."IdEstado" = DD."IdEstado"
 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoDeposito" as TD ON TD."IdTipoDeposito" = DD."IdTipoDeposito"
  LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BS ON (BS."IdBanco" = CFP."IdBancoSocio")
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as CON ON CON."IdConcepto" = DD."IdConcepto"
 WHERE ET."NombreEntidad" = 'TES.DepositoDetalle'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarDepositoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
