SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::DetalleComprobanteTesoreria";

CREATE PROCEDURE
-- 
--  Proposito: Obtiene data de un comprobante y su formas de pago para desplegar en reporte
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 16/09/2016
--
"BSCL.Tesoreria.CrystalReports.Servicios::DetalleComprobanteTesoreria" (
     IN p_IdEmpresa 				BIGINT,
     IN p_IdComprobante            	BIGINT
 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
	DECLARE v_FechaHoraSistema 		NVARCHAR(40);
	DECLARE v_SegundosZonaHoraria 	BIGINT;
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa,v_FechaHoraSistema);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.SegundosZonaHoraria', v_SegundosZonaHoraria);


	--Se recupera la ultima versión del logo cargado en el sistema
	v_logo = 
	SELECT TOP 1
		   "Binario" AS "LogoCorredora"
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
	 WHERE "NombreImagen" = 'LOGO_CORREDOR' 
	   AND "IdEmpresa" = p_IdEmpresa 
	 ORDER BY "Version" DESC;

	v_Result =
	SELECT
			 COM."IdComprobante"
			,COM."TipoComprobante"
			,COM."NombreTipoMovimiento"
			,COM."NombreConcepto"
			,COM."NombreCaja"
			,COM."IdMoneda"	
			,COM."NombreMoneda"
			,COM."Monto"
			,TO_CHAR(COM."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
			,TO_CHAR(COM."FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaIngresoRegistro"
			,TO_CHAR(COM."FechaMovimiento",'DD-MM-YYYY') AS "FechaMovimiento"
			,COM."NombreTipoSocioDeNegocio"
			,COM."NombrePersona"
			,COM."DocumentoIdentidad"	
			,COM."TitularPago"
			,COM."NombrePersonaTitularPago"	
			,COM."NombrePortafolioTitularPago"
			,COM."SecuenciaAgrupacion"
			,COM."NombreEstado" AS  "NombreEstadoComprobante"
			,CFP."NombreMedioDePago"
			,CFP."NombreModalidadDePago"	
			,CFP."NombreBancoCorredora"
			,CFP."NombreTipoCuentaBancariaCorredora"
			,CFP."NombreCuentaBancariaCorredora"
			,CFP."NumeroCuentaBancariaCorredora"
			,CFP."NombreTipoTarjetaBancariaCorredora"
			,CFP."NumeroTarjetaBancariaCorredora"
			,CFP."IdChequera"
			,CFP."NumeroDocumentoCorredora"
			,CFP."NumeroSerieCorredora"
			,TO_CHAR (CFP."FechaDocumentoPagoCorredora",'DD-MM-YYYY') AS "FechaDocumentoPagoCorredora"
			,CFP."NombreBancoSocio"
			,CFP."NombreTipoCuentaBancariaSocio"
			,CFP."NombreCuentaBancariaSocio"
			,CFP."NumeroCuentaBancariaSocio"
			,CFP."NombreTipoTarjetaBancariaSocio"
			,CFP."NumeroTarjetaBancariaSocio"
			,CFP."NumeroSerieSocio"
			,CFP."NumeroDocumentoSocio"
			,TO_CHAR (CFP."FechaDocumentoPagoSocio",'DD-MM-YYYY') AS "FechaDocumentoPagoSocio"
			,CFP."IdMoneda" AS "MonedaFormaDePago"
			,CFP."Monto" AS "MontoFormaDePago"			
	  		,CFP."NombreEstado" AS "NombreEstadoFormaDePago"
	  		--,IMAG."LogoCorredora"
			,DGEN."RazonSocial" AS "RazonCorredora"
		   	,DGEN."RUT" AS "RutCorredora"
		   	,DGEN."Direccion" AS "DireccionCorredora"		   
		   	,DGEN."Telefono" AS "TelefonoCorredora"		   
		   	,TO_CHAR (:v_FechaHoraSistema,'DD-MM-YYYY') AS "FechaSistema"
		   	,TO_CHAR (ADD_SECONDS(:v_FechaHoraSistema,:v_SegundosZonaHoraria),'HH:mm:ss') AS "HoraSistema"
	  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM
	 INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" as DGEN ON DGEN."IdEmpresa" = COM."IdEmpresa"
	  --LEFT JOIN :v_logo as IMAG ON IMAG."IdEmpresa" = COM."IdEmpresa"
	  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")		
	 WHERE COM."IdComprobante" = :p_IdComprobante
	   AND COM."IdEmpresa"     = :p_IdEmpresa;
	   
		BEGIN
			SELECT * FROM :v_Result 
			CROSS JOIN :v_logo;
		END;
END;

  