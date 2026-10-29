-- Inserta datos de forma de pago	
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
(
	"IdComprobante","NumeroReferencia","IdTipoMovimiento","IdMedioDePago","NombreMedioDePago","IdModalidadDePago",
	"NombreModalidadDePago","IdCuentaBancariaCorredora","NombreCuentaBancariaCorredora","NumeroCuentaBancariaCorredora",
	"IdTipoCuentaBancariaCorredora","NombreTipoCuentaBancariaCorredora","IdTarjetaBancariaCorredora",
	"IdTipoTarjetaBancariaCorredora","NombreTipoTarjetaBancariaCorredora","NumeroTarjetaBancariaCorredora","IdChequera",
	"NumeroDocumentoCorredora","FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio",
	"NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio",
	"IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio","NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio",
	"NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio","IdMoneda","Monto","IdPlaza","DiasRetencion",
	"FechaAfectaSaldo"
)

	-- SELECT DATOS FORMA DE PAGO COMPROBANTE NORMAL (no es partida)
	SELECT 	MI."IdComprobante",MI."NumeroReferencia",IEFP."IdTipoMovimiento",IEFP."IdMedioDePago",IEFP."NombreMedioDePago",IEFP."IdModalidadDePago",IEFP."NombreModalidadDePago",				
			IEFP."IdCuentaBancariaCorredora",IEFP."NombreCuentaBancariaCorredora",COALESCE(IEFP."NumeroCuentaBancariaCorredora",''),COALESCE(IEFP."IdTipoCuentaBancariaCorredora",''),
			COALESCE(IEFP."NombreTipoCuentaBancariaCorredora",''),IEFP."IdTarjetaBancariaCorredora",IEFP."IdTipoTarjetaBancariaCorredora",IEFP."NombreTipoTarjetaBancariaCorredora",
			IEFP."NumeroTarjetaBancariaCorredora",IEFP."IdChequera",IEFP."NumeroDocumentoCorredora", IEFP."FechaDocumentoPagoCorredora", 
			IEFP."IdCuentaBancariaSocio",IEFP."IdBancoSocio",IEFP."NombreCuentaBancariaSocio",IEFP."NumeroCuentaBancariaSocio",IEFP."IdTipoCuentaBancariaSocio",
			IEFP."NombreTipoCuentaBancariaSocio",IEFP."IdTarjetaBancariaSocio",IEFP."IdTipoTarjetaBancariaSocio",IEFP."NombreTipoTarjetaBancariaSocio",
			IEFP."NumeroTarjetaBancariaSocio",IEFP."NumeroSerieSocio",IEFP."NumeroDocumentoSocio", IEFP."FechaDocumentoPagoSocio",
			IEFP."IdMoneda",CAST(SUM(IEFP."Monto") AS DECIMAL(26,8)) AS "Monto",COALESCE(IbC."IdPlaza",0) as "IdPlaza",
			CASE WHEN (IEFP."IdBancoSocio" = IbC."IdBanco" AND IEFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionMismoBanco"
				 WHEN (IEFP."IdBancoSocio" <> IbC."IdBanco" AND IEFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionOtroBanco"
				 ELSE 0
			 END AS "DiasRetencion",
			'0001-01-01' as "FechaAfectaSaldo"
	  FROM "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" AS MI
	 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP 
	 		 ON IEFP."IdIngresoEgreso"  = MI."IdIngresoEgreso"
	 	-- FALTA .. revisar si v_InformacionBancoCorredora se puede llevar a una tabla global
	  --LEFT JOIN :v_InformacionBancoCorredora AS IbC ON IbC."IdCuentaBanco" 	= IEFP."IdCuentaBancariaCorredora"
	  LEFT JOIN (SELECT DISTINCT CB."IdPersona",SN."NombreCompleto",SN."DocumentoIdentidadDefecto",SN."CodigoTipoPersona",
					   CB."IdCuentaBanco",CB."IdBanco",CB."IdPlaza",CB."PlazoRetencionMismoBanco",CB."PlazoRetencionOtroBanco",
					   CB."NombreCuentaBancaria",CB."NumeroCuentaBancaria",CB."IdTipoCuentaBancaria",TCBC."NombreTipoCuentaBancaria"
				  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CB
				 INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
				 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" as SN ON SN."IdPersona" = CB."IdPersona"
				 WHERE SN."IdEmpresa" = 1) AS IbC ON IbC."IdCuentaBanco" 	= IEFP."IdCuentaBancariaCorredora"
	  
	 WHERE IEFP."IdEstado" = 'ING' AND LENGTH(MI."TipoComprobante") = 0 
	 GROUP BY MI."Grupo",MI."IdComprobante",MI."NumeroReferencia",IEFP."IdTipoMovimiento",IEFP."IdMedioDePago",IEFP."NombreMedioDePago",IEFP."IdModalidadDePago",
			IEFP."NombreModalidadDePago",IEFP."IdCuentaBancariaCorredora",IEFP."IdTipoCuentaBancariaCorredora",IEFP."NombreTipoCuentaBancariaCorredora",
			IEFP."NombreCuentaBancariaCorredora",IEFP."NumeroCuentaBancariaCorredora",IEFP."IdTarjetaBancariaCorredora",IEFP."IdTipoTarjetaBancariaCorredora",
			IEFP."NombreTipoTarjetaBancariaCorredora",IEFP."NumeroTarjetaBancariaCorredora",IEFP."IdChequera",IEFP."IdBancoSocio",
			IEFP."NumeroDocumentoCorredora",IEFP."FechaDocumentoPagoCorredora",IEFP."IdCuentaBancariaSocio",IEFP."IdTipoCuentaBancariaSocio",
			IEFP."NombreTipoCuentaBancariaSocio",IEFP."NombreCuentaBancariaSocio",IEFP."NumeroCuentaBancariaSocio",IEFP."IdTarjetaBancariaSocio",
			IEFP."IdTipoTarjetaBancariaSocio",IEFP."NombreTipoTarjetaBancariaSocio",IEFP."NumeroTarjetaBancariaSocio",
			IEFP."FechaVctoTarjetaBancariaSocio",IEFP."NumeroSerieSocio",IEFP."NumeroDocumentoSocio",IEFP."FechaDocumentoPagoSocio",IEFP."IdMoneda",
			IbC."IdPlaza",IbC."IdBanco",IbC."PlazoRetencionMismoBanco",IbC."PlazoRetencionOtroBanco"
					
UNION ALL

	-- SELECT DATOS FORMA DE PAGO COMPROBANTE PARTIDA DE INGRESOS EGRESOS
	SELECT DISTINCT MI."IdComprobante",MI."NumeroReferencia",CASE WHEN MI."TipoComprobante" = 'PARING' THEN 'MOVING' WHEN MI."TipoComprobante" = 'PAREGR' THEN 'MOVEGR' END as "IdTipoMovimiento",				
			FP."IdMedioDePago",COALESCE(FP."NombreMedioDePago",'') as "NombreMedioDePago",FP."IdModalidadDePago",COALESCE(FP."NombreModalidadDePago",'') as "NombreModalidadDePago",
			FP."IdCuentaBancariaCorredora",COALESCE(FP."NombreCuentaBancaria",'') as "NombreCuentaBancariaCorredora",COALESCE(FP."NumeroCuentaBancaria",'') as "NumeroCuentaBancariaCorredora",
 			COALESCE(FP."IdTipoCuentaBancaria",'') as "IdTipoCuentaBancariaCorredora",COALESCE(FP."NombreTipoCuentaBancaria",'') as "NombreTipoCuentaBancariaCorredora",
			FP."IdTarjetaBancariaCorredora",COALESCE(FP."IdTipoTarjetaBancaria",'') as "IdTipoTarjetaBancariaCorredora",COALESCE(FP."NombreTipoTarjetaBancaria",'') as "NombreTipoTarjetaBancariaCorredora",
			COALESCE(FP."NumeroTarjetaBancaria",'') as "NumeroTarjetaBancariaCorredora",FP."IdChequera",FP."NumeroDocumentoCorredora",FP."FechaDocumentoPagoCorredora",
			FP."IdCuentaBancariaSocio",FP."IdBancoSocio",FP."NombreCuentaBancariaSocio",FP."NumeroCuentaBancariaSocio",FP."IdTipoCuentaBancariaSocio",
			FP."NombreTipoCuentaBancariaSocio",FP."IdTarjetaBancariaSocio",FP."IdTipoTarjetaBancariaSocio",FP."NombreTipoTarjetaBancariaSocio",
			FP."NumeroTarjetaBancariaSocio",FP."NumeroSerieSocio",FP."NumeroDocumentoSocio",FP."FechaDocumentoPagoSocio",FP."IdMoneda",FP."Monto",
			COALESCE(FP."IdPlaza",0) as "IdPlaza",
			CASE WHEN (FP."IdBancoSocio" = FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionMismoBanco"
				 WHEN (FP."IdBancoSocio" <> FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionOtroBanco"
				 ELSE 0
			 END AS "DiasRetencion",
			'0001-01-01' as "FechaAfectaSaldo"
	  FROM "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" AS MI
	 INNER JOIN "BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso" AS FP 
	 		 ON FP."NumeroReferencia" = MI."NumeroReferencia"
	 WHERE LENGTH(MI."TipoComprobante") > 0;