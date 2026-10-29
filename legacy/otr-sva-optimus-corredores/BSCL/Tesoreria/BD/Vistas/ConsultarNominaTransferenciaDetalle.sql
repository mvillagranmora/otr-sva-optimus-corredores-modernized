SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS

SELECT	 NTD."IdNominaTransferenciaDetalle"
		,NTD."IdNominaTransferencia"
		,NTD."IdComprobanteFormaDePago"
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
		,CFP."IdBancoSocio"
		,COALESCE(DI."DocumentoIdentidad",'') AS "RutBancoSocio"
		,COALESCE(BAN."NombreBanco",'') AS "NombreBancoSocio"
		,MON."Nombre" AS "NombreMoneda"
		,CFP."Monto"
		,COMP."IdCaja" 
		,COMP."IdComprobante"
		,COMP."FechaIngresoRegistro" as "FechaIngresoComprobante"
		,COMP."IdTipoPersona"
		,COMP."IdPersona"
		,COMP."NombrePersona"
		,PDE."Direccion" AS "DireccionElectronica"
		,COMP."DocumentoIdentidad"
		,COMP."IdConcepto"
		,CON."Nombre" AS "NombreConcepto"
		,NT."UsaTalonarioCheque"	
		,NT."EsTransferencia"
		,NT."EsNomina"
		,CB."IdBanco" AS "IdBancoCorredora"
		,CB."NombreBanco" AS "NombreBancoCorredora"
 		,NT."IdTipoCuentaBanco"
		,NTD."IdUsuario"
		,NTD."IdEstado"
		,ET."NombreEstado"
		,NT."FechaLiquidacion" AS "FechaLiquidacionNT" 
		,0 AS "NumeroSerieCorredora"
		,FPMC."AliasBanco"
		,CASE 
			WHEN TipoMovimiento."IdModulo" = 'TES' AND FPMC."IdMedioDePago" = 'TDP' THEN
	            (SELECT "Valor" FROM "BSCL.Custodia.BD::CUS.Configuracion" 
	             WHERE "CodigoParametro" = 
	                 CASE 
	                     WHEN ComprobanteDetalle."IdTipoProducto" = 'MERF' 
	                         AND (ComprobanteDetalle."IdTipoOperacion" IN ('COMPAC', 'COMTER', 'ANTPAC')) THEN 'CuentaDCVCuentaPropia'
	                     ELSE 'CuentaDCVCuentaLiquidacion'
	                 END
	             AND "IdEmpresa" = COMP."IdEmpresa") 
			ELSE '' 
    	 END AS "CuentaDCVOrigen"
		,CASE 
        	WHEN TipoMovimiento."IdModulo" = 'TES' AND FPMC."IdMedioDePago" = 'TDP' THEN
            	CuentaCustodio."Codigo" 
        	ELSE '' 
    	 END AS "CuentaDCVDestino"
		,CFP."ClaveDCV"
		,COALESCE(INS."Nemotecnico", '') AS "Nemotecnico"
FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as COMP ON COMP."IdComprobante" = CFP."IdComprobante" 
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" as ET ON ET."IdEstado" = NTD."IdEstado"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON ON MON."IdMoneda" = CFP."IdMoneda"
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = COMP."IdPersona")
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS PDE ON (PDE."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal") 
LEFT JOIN  "BSCL.Plataforma.BD::PLA.Concepto" AS CON ON (CON."IdConcepto" = COMP."IdConcepto")
LEFT JOIN  "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CB ON (CB."IdCuentaBanco" = NT."IdCuentaBanco")
LEFT JOIN  "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (BAN."IdBanco" = CFP."IdBancoSocio")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PSO ON (PSO."IdPersona" = BAN."IdPersona")
LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON (DI."IdDocumentoIdentidad" = PSO."IdDocumentoIdentidad")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TipoMovimiento ON (TipoMovimiento."IdModulo" = 'TES' and  TipoMovimiento."IdTipoMovimiento" = COMP."IdTipoMovimiento")
LEFT OUTER JOIN (
	SELECT "IdComprobante" , MIN("NroLinea") AS "NroLinea"
	FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	GROUP BY "IdComprobante"
) AS COMDETMEN ON  (COMDETMEN."IdComprobante" = COMP."IdComprobante")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioTitularPago ON PortafolioTitularPago."IdPortafolio" = COMP."IdPortafolioTitularPago"
LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle ON  (COMDETMEN."IdComprobante" = ComprobanteDetalle."IdComprobante" AND COMDETMEN."NroLinea" = ComprobanteDetalle."NroLinea")
LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormasDePagoMovimientoComprobante" AS FPMC ON FPMC."IdInterno" = COMP."IdComprobante" AND FPMC."Tipo" = 'Comprobante'
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CuentaCustodio ON CuentaCustodio."IdPortafolio" = PortafolioTitularPago."IdPortafolio" AND CuentaCustodio."IdBoveda" = 'DCV' AND CuentaCustodio."IdEstado" = 'ACT' AND CuentaCustodio."IdEstadoAutorizacion" = 'AUT'
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = ComprobanteDetalle."IdInstrumento"
WHERE ET."NombreEntidad" = 'TES.NominaTransferenciaDetalle'
WITH READ ONLY;

GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;