SET SCHEMA "BSCL";
GO

DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::ConsultaCentralizadaVencimientos";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultaCentralizadaVencimientos"(
	IN 	p_IdEmpresa						BIGINT,
	IN	p_IdCliente						BIGINT,
	IN	p_IdPortafolio					BIGINT,
	IN	p_IdEjecutivo					BIGINT,
	IN 	p_FechaVencimientoDesde			NVARCHAR(10),
	IN 	p_FechaVencimientoHasta			NVARCHAR(10),	
	IN	p_IdClasificacion				NVARCHAR(2),
	IN	p_IdTipoInstrumento				NVARCHAR(3),
	IN  p_Nemotecnico					NVARCHAR(40) DEFAULT NULL,
	IN	p_IdInstrumento					BIGINT,
	IN	p_IdProductoAgrupador			BIGINT DEFAULT NULL,

	OUT p_out_CustodiaVencimientos		TABLE(
										"FechaVencimientoInstrumento"			NVARCHAR(10)
									   ,"IdCliente"								BIGINT
									   ,"DocumentoIdentidad"                    NVARCHAR(40)
									   ,"CodigoP"								NVARCHAR(100)
									   ,"NombreCliente"							NVARCHAR(300)
									   ,"IdPortafolio"							BIGINT
									   ,"RutCAVALI"								NVARCHAR(300)
									   ,"Portafolio"							NVARCHAR(300)
									   ,"NombreCompletoEjecutivo"				NVARCHAR(300)
									   ,"NombreTipoCustodia"					NVARCHAR(300)
									   ,"IdClasificacionTipoInstrumento"		NVARCHAR(3)
									   ,"IdTipoInstrumento"						NVARCHAR(3)
									   ,"CodigoTipoInstrumento"					NVARCHAR(40)
									   ,"NombreTipoInstrumento"					NVARCHAR(100)									   
									   ,"IdInstrumento"							BIGINT
									   ,"Nemotecnico"							NVARCHAR(40)
									   ,"ISIN"									NVARCHAR(40)
									   ,"IdEmisor"								NVARCHAR(40)									   
									   ,"IdBoveda"								NVARCHAR(3)
									   ,"NumeroCuentaBoveda"					NVARCHAR(40)
									   ,"IdMonedaSistema"						NVARCHAR(3)
									   ,"IdMonedaInstrumento"					NVARCHAR(3)
									   ,"IdTipoReajuste"						NVARCHAR(3)
									   ,"IdMonedaMontos"						NVARCHAR(3)
									   ,"Cantidad"								DECIMAL(26,8)
									   ,"VALORRESCATE"							DECIMAL(26,8)
									   ,"PrecioCompra"							DECIMAL(26,8)
									   ,"TasaCompra"							DECIMAL(26,8)
									   ,"MontoATasaCompraActual"				DECIMAL(26,8)
									   ,"MontoCompraActualMonedaInstrumento"	DECIMAL(26,8)
									   ,"MontoInteresCorridoCompraActual"		DECIMAL(26,8)
									   ,"MontoReajusteCorridoCompraActual"		DECIMAL(26,8)
									   ,"EsFungible"							NVARCHAR(100)
									   ,"TieneCorteDeCupon"						NVARCHAR(100)
									   ,"IdConcepto"							NVARCHAR(6)
									   --,"NombreConcepto"						NVARCHAR(300)
									   ,"Origen"								NVARCHAR(40)
									   ,"OrigenTraduccion"						NVARCHAR(520)
									   ,"PoseeInstrumentoSubyacente"			NVARCHAR(1)
									   ,"IdTipoComprobante"						NVARCHAR(3)
		  							   ,"FolioComprobante"						BIGINT
		  							   ,"TipoVencimiento"						NVARCHAR(100)
		  							   ,"TipoVencimientoTraduccion"				NVARCHAR(5000)
		  							   ,"NombreProductoAgrupador"				NVARCHAR(100)
									   ,"EstadoVencimiento"						NVARCHAR(100)
									   ,"AdminCartera"                          NVARCHAR(2) 
									)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

READS SQL DATA AS
BEGIN
	
	v_CustodiaVencimientos =
	WITH
	ClientePortafolio AS
	(
		SELECT
			POR."IdPortafolio"
		   ,CLI."DocumentoIdentidad"
		   ,POR."CodigoPortafolio" AS "CodigoP"
		   ,CAST(CLI."NombreCompleto" AS NVARCHAR(300)) AS "NombreCliente"
		   ,CAST(POR."CodigoPortafolio" ||' '|| POR."Proposito" AS NVARCHAR(300)) AS "Portafolio"
		   ,CLI."DocumentoIdentidadAdicional"
		   ,CLI."TipoDocumentoIdentidadAdicional"
		   ,ProductoAgrupador."NombreProductoAgrupador"
		FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR 	  		 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		WHERE (:p_IdCliente IS NULL OR CLI."IdCliente" = :p_IdCliente)
		AND (:p_IdPortafolio IS NULL OR POR."IdPortafolio" = :p_IdPortafolio)
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	),
	Ejecutivo AS
	(
		SELECT
			"IdPortafolio"
		   ,CAST("NombreCompletoEjecutivo" as NVARCHAR(300)) AS "NombreCompletoEjecutivo"
		FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"
		WHERE
			"EsEjecutivoVigente" = 'S'
		AND (:p_IdEjecutivo IS NULL OR "IdEmpleadoEjecutivoPortafolio" = :p_IdEjecutivo)
	)
	SELECT
		CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND INS."NumeroCupones" = 0 THEN CAR."FechaVencimientoInstrumento" ELSE CAR."INGFechaPagoCorteCupon" END AS "FechaVencimientoInstrumento"
	   ,CAR."IdCliente"  AS "IdCliente"
	   ,CPR."DocumentoIdentidad"
	   ,CPR."CodigoP"
	   ,CPR."NombreCliente"
	   ,CAR."IdPortafolio" AS "IdPortafolio"
	   ,CPR."Portafolio"
	   ,CPR."DocumentoIdentidadAdicional"
	   ,CPR."TipoDocumentoIdentidadAdicional"
	   ,EJC."NombreCompletoEjecutivo"
	   ,CAR."IdClasificacionTipoInstrumento"
	   ,CAR."IdTipoInstrumento"
	   ,CAR."IdInstrumento"
	   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END AS "Nemotecnico"
	   ,CAR."ISIN"
	   ,CAR."IdEmisor"
	   ,CAR."IdBoveda"
	   ,CAR."IdCuentaCustodioPortafolio"
	   ,CAR."IdMonedaSistema"
	   ,CAR."IdMonedaInstrumento"
	   ,CAR."IdTipoReajuste"
	  ,CASE
			WHEN COALESCE(CAR."IdTipoReajuste",'') <> '' THEN CAR."IdTipoReajuste"
			WHEN COALESCE(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento"
			ELSE ''
		END as "IdMonedaMontos"
	   ,SUM(CAR."INGCantidad")	AS "Cantidad"
	   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."IdTipoOperacion" = 'VENPAC' THEN SUM(CAR."INGValorRescate")*-1 ELSE SUM(CAR."INGValorRescate")	END AS "ValorRescate"
	   ,CAR."INGPrecioCompra"	AS "PrecioCompra"
	   ,CAR."INGTasaCompra"	AS "TasaCompra"
	   ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(CAR."INGMontoCorteCupon")
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(CAR."INGValorRescate")
			ELSE SUM(CAR."INGMontoATasaCompraActual")
		END AS "MontoATasaCompraActual"
	   ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(CAR."INGMontoCorteCuponMonedaInstrumento")
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(CAR."INGValorRescate")
			ELSE SUM(CAR."INGMontoCompraActualMonedaInstrumento")
		END AS "MontoCompraActualMonedaInstrumento"
	   ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(CAR."INGMontoInteresCompraProximoMonedaInstrumento")
		END  AS "MontoInteresCorridoCompraActual"
	   ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(CAR."INGMontoReajusteCompraAnteriorMonedaInstrumento")
		END  AS "MontoReajusteCorridoCompraActual"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsFungible") AS "EsFungible"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("TieneCorteDeCupon") AS "TieneCorteDeCupon"
	   ,CAR."IdConcepto"
	   ,'UI_CARTERA' AS "Origen"
	   ,CMC."PoseeInstrumentoSubyacente"
	   ,CAR."IdTipoComprobante"
	   ,CAR."FolioComprobante"
	   ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN 'UI_VENCIMIENTO_CORTE_CUPON'
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' THEN 'UI_VENCIMIENTO_RF'
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN 'UI_VENCIMIENTO_IF'
		END AS "TipoVencimiento"
	   ,CPR."NombreProductoAgrupador"
	   ,CASE
			WHEN CAR."IdEstado" = 'VIG' THEN 'UI_VIGENTE'
			WHEN CAR."IdEstado" = 'VCD' THEN 'UI_VENCIDO'
		END AS "EstadoVencimiento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC
			ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
			ON INS."IdInstrumento" = CAR."IdInstrumento"
	INNER JOIN ClientePortafolio AS CPR
			ON CPR."IdPortafolio" = CASE WHEN CAR."IdTipoOperacion" != 'VENPAC' THEN CAR."IdPortafolio" ELSE CAR."IdPortafolioRelacionado" END
	LEFT JOIN Ejecutivo AS EJC
			ON EJC."IdPortafolio" = CASE WHEN CAR."IdTipoOperacion" != 'VENPAC' THEN CAR."IdPortafolio" ELSE CAR."IdPortafolioRelacionado" END
	WHERE
		CAR."IdEstado" IN ('VIG','VCD')
	AND CAR."EGRIdTipoOperacion" != 'VCDPAC'	
	AND CAR."IdEmpresa" = :p_IdEmpresa
	AND CAR."IdTipoOperacion" != 'VENPAC'	
	AND CAR."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND CAR."IdTipoInstrumento" NOT IN ('SIM', 'PRT', 'EMP', 'PAC','RRV','RRF','OPC')
	AND (:p_IdClasificacion IS NULL OR CAR."IdClasificacionTipoInstrumento" = :p_IdClasificacion)
	AND (:p_IdTipoInstrumento IS NULL OR CAR."IdTipoInstrumento" = :p_IdTipoInstrumento)
	AND (:p_IdInstrumento IS NULL OR CAR."IdInstrumento" = :p_IdInstrumento)
	AND (:p_Nemotecnico IS NULL OR CAR."Nemotecnico" = :p_Nemotecnico)
	AND (
			CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND INS."NumeroCupones" = 0 AND CAR."FechaVencimientoInstrumento" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta	   
	   		OR
	   		CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 AND CAR."INGFechaPagoCorteCupon" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta
		)
	GROUP BY CAR."FechaVencimientoInstrumento", CAR."INGFechaPagoCorteCupon",CAR."IdClasificacionTipoInstrumento", INS."NumeroCupones",CAR."IdCliente",CAR."IdTipoOperacion" 
	   ,CPR."DocumentoIdentidad",CAR."NemoSVS" ,CAR."ISIN" ,CAR."INGPrecioCompra",CAR."INGTasaCompra",CAR."IdEmisor",CAR."IdClienteRelacionado"
	   ,CAR."IdBoveda",CAR."IdCuentaCustodioPortafolio",CAR."IdMonedaSistema","EsFungible","TieneCorteDeCupon" ,CAR."IdConcepto",CAR."IdMonedaInstrumento",CMC."PoseeInstrumentoSubyacente"
	   ,CAR."IdTipoComprobante" ,CPR."NombreProductoAgrupador",CAR."FolioComprobante" ,CAR."IdEstado",CAR."IdTipoReajuste",CAR."IdPortafolioRelacionado"
	   ,CPR."CodigoP",CPR."NombreCliente",CAR."IdPortafolio",CPR."Portafolio",CPR."DocumentoIdentidadAdicional"
	   ,CPR."TipoDocumentoIdentidadAdicional",EJC."NombreCompletoEjecutivo",CAR."IdClasificacionTipoInstrumento",CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico"
	
	UNION ALL ---- SIMULTANEAS, PRESTAMOS 
	SELECT
		CAR."FechaVencimientoOperacion" AS "FechaVencimientoInstrumento"
	   ,CAR."IdCliente"
	   ,CPR."DocumentoIdentidad"
	   ,CPR."CodigoP"
	   ,CPR."NombreCliente"
	   ,CAR."IdPortafolio"
	   ,CPR."Portafolio"
	   ,CPR."DocumentoIdentidadAdicional"
	   ,CPR."TipoDocumentoIdentidadAdicional"
	   ,EJC."NombreCompletoEjecutivo"
	   ,SUB."IdClasificacionTipoInstrumento"
	   ,SUB."IdTipoInstrumento"
	   ,SUB."IdInstrumento"
	   ,SUB."Nemotecnico"
	   ,SUB."ISIN"
	   ,SUB."IdEmisor"
	   ,CAR."IdBoveda"
	   ,CAR."IdCuentaCustodioPortafolio"
	   ,CAR."IdMonedaSistema"
	   ,SUB."IdMonedaInstrumento"
	   ,SUB."IdTipoReajuste"
	   ,CASE
			WHEN COALESCE(SUB."IdTipoReajuste",'') <> '' THEN CAR."IdTipoReajuste"
			WHEN COALESCE(SUB."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento"
			ELSE ''
		END as "IdMonedaMontos"
	   ,SUB."INGCantidad"	AS "Cantidad"
	   ,SUB."INGValorRescate"	AS "ValorRescate"
	   ,SUB."INGPrecioSubyacente"	AS "PrecioCompra"
	   ,SUB."INGTasaSubyacente"	AS "TasaCompra"
	   ,CASE
			WHEN CAR."EGRIdTipoOperacion" IN ('ANSIMC', 'ANSIMV')
			AND CAR."EGRIdConcepto" IN ('ANTSIC', 'ANTSIV')
			AND CAR."IdEstado" IN ('ANT') THEN COALESCE(CAR."EGRMontoARecibirAnticipo", 0)
			ELSE COALESCE(CAR."INGMontoATasaCompraActual", 0)
		END	AS "MontoATasaCompraActual"
	   ,SUB."INGMontoSubyacenteActualMonedaInstrumento" AS "MontoCompraActualMonedaInstrumento"
	   ,SUB."INGMontoInteresSubyacenteProximoMonedaInstrumento" AS "MontoInteresCorridoCompraActual"
	   ,SUB."INGMontoReajusteSubyacenteAnteriorMonedaInstrumento" AS "MontoReajusteCorridoCompraActual"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(SUB."EsFungible") AS "EsFungible"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(SUB."TieneCorteDeCupon") AS "TieneCorteDeCupon"
	   ,CAR."IdConcepto"
	   ,'UI_CARTERA' AS "Origen"
	   ,CMC."PoseeInstrumentoSubyacente"
	   ,CAR."IdTipoComprobante"
	   ,CAR."FolioComprobante"
	   ,CASE
			WHEN CAR."IdTipoInstrumento" = 'SIM' THEN 'VCTO_SIMU'
			WHEN CAR."IdTipoInstrumento" = 'PRT' THEN 'UI_VENCIMIENTO_PRESTAMO'
			WHEN CAR."IdTipoInstrumento" = 'EMP' THEN 'UI_VENCIMIENTO_EMPRESTITO'
			WHEN CAR."IdTipoInstrumento" = 'RRV' THEN 'UI_VENCIMIENTO_REPORTE_RV'
			WHEN CAR."IdTipoInstrumento" = 'RRF' THEN 'UI_VENCIMIENTO_REPORTE_RF'
		END AS "TipoVencimiento"
	   ,CPR."NombreProductoAgrupador" 
	   ,CASE
			WHEN CAR."IdEstado" = 'VIG' AND SUB."IdEstado" = 'VIG' THEN 'UI_VIGENTE'
			WHEN CAR."IdEstado" = 'VCD' AND SUB."IdEstado" = 'VCD' THEN 'UI_VENCIDO'
		END AS "EstadoVencimiento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = car."FolioCartera"
	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC
			ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
	INNER JOIN ClientePortafolio AS CPR
			ON CPR."IdPortafolio" = CAR."IdPortafolio"
	LEFT JOIN Ejecutivo AS EJC
			ON EJC."IdPortafolio" = CAR."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
 	MovNormal."IdEmpresa" = Car."IdEmpresa" and 
 	MovNormal."IdComprobante" = Car."FolioComprobante" 
	INNER JOIN (
(   -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
	SELECT "IdComprobante","NemotecnicoSubyacente",  MAX("IdEmpresa") AS "IdEmpresa", MAX("IdMovimientoComprobante") AS "IdMovimientoComprobante"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"
	GROUP BY "IdComprobante", "NemotecnicoSubyacente"
)) AS MCU ON (MCU."IdEmpresa" = MovNormal."IdEmpresa" AND MCU."IdMovimientoComprobante" = MovNormal."IdMovimientoComprobante" AND MCU."NemotecnicoSubyacente" = MovNormal."NemotecnicoSubyacente")		
	WHERE
		CAR."IdEstado" IN ('VIG','VCD') 
	AND SUB."IdEstado" IN ('VIG','VCD')
	AND CAR."IdEmpresa" = :p_IdEmpresa
	AND CAR."IdConcepto" != 'VNPCME'
	AND CAR."IdTipoInstrumento" IN ('SIM', 'PRT', 'EMP','RRV','RRF','OPC')
	AND (:p_IdClasificacion IS NULL OR SUB."IdClasificacionTipoInstrumento" = :p_IdClasificacion)
	AND (:p_IdTipoInstrumento IS NULL OR SUB."IdTipoInstrumento" = :p_IdTipoInstrumento)
	AND (:p_IdInstrumento IS NULL OR SUB."IdInstrumento" = :p_IdInstrumento)
	AND (:p_Nemotecnico IS NULL OR SUB."Nemotecnico" = :p_Nemotecnico)
	AND CAR."FechaVencimientoOperacion" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta
	
	UNION ALL ---- OPCIONES
	SELECT
		CAR."FechaVencimientoOperacion" AS "FechaVencimientoInstrumento"
	   ,CAR."IdCliente"
	   ,CPR."DocumentoIdentidad"
	   ,CPR."CodigoP"
	   ,CPR."NombreCliente"
	   ,CAR."IdPortafolio"
	   ,CPR."Portafolio"
	   ,CPR."DocumentoIdentidadAdicional"
	   ,CPR."TipoDocumentoIdentidadAdicional"
	   ,EJC."NombreCompletoEjecutivo"
	   ,CAR."IdClasificacionTipoInstrumento"
	   ,CAR."IdTipoInstrumento"
	   ,CAR."IdInstrumento"
	   ,CAR."Nemotecnico"
	   ,CAR."ISIN"
	   ,CAR."IdEmisor"
	   ,CAR."IdBoveda"
	   ,CAR."IdCuentaCustodioPortafolio"
	   ,CAR."IdMonedaSistema"
	   ,CAR."IdMonedaInstrumento"
	   ,CAR."IdTipoReajuste"
	   ,CASE
			WHEN COALESCE(CAR."IdTipoReajuste",'') <> '' THEN CAR."IdTipoReajuste"
			WHEN COALESCE(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento"
			ELSE ''
		END AS "IdMonedaMontos"
	   ,CAR."INGCantidad"	AS "Cantidad"
	   ,CAR."INGValorRescate"	AS "ValorRescate"
	   ,CAR."INGPrecioCompra"	AS "PrecioCompra"
	   ,CAR."INGTasaCompra"	AS "TasaCompra"
	   ,CAR."INGMontoATasaCompraActual" AS "MontoATasaCompraActual"
	   ,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraActualMonedaInstrumento"
	   ,CAR."INGMontoInteresCompraProximoMonedaInstrumento" AS "MontoInteresCorridoCompraActual"
	   ,CAR."INGMontoReajusteCompraAnteriorMonedaInstrumento" AS "MontoReajusteCorridoCompraActual"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsFungible") AS "EsFungible"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("TieneCorteDeCupon") AS "TieneCorteDeCupon"
	   ,CAR."IdConcepto"
	   ,'UI_CARTERA' AS "Origen"
	   ,CMC."PoseeInstrumentoSubyacente"
	   ,CAR."IdTipoComprobante"
	   ,CAR."FolioComprobante"
	   ,'UI_VENCIMIENTO_OPCION' AS "TipoVencimiento"
	   ,CPR."NombreProductoAgrupador"
	   ,CASE
			WHEN CAR."IdEstado" = 'VIG' THEN 'UI_VIGENTE'
			WHEN CAR."IdEstado" = 'VCD' THEN 'UI_VENCIDO'
		END AS "EstadoVencimiento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC
			ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
			ON INS."IdInstrumento" = CAR."IdInstrumento"
	INNER JOIN ClientePortafolio AS CPR
			ON CPR."IdPortafolio" = CAR."IdPortafolio"
	LEFT JOIN Ejecutivo AS EJC
			ON EJC."IdPortafolio" = CAR."IdPortafolio"
	WHERE
		CAR."IdEstado" IN ('VIG','VCD')
	AND CAR."IdEmpresa" = :p_IdEmpresa
	AND CAR."IdClasificacionTipoInstrumento" = 'DV'
	AND CAR."IdTipoInstrumento" = 'OPC'
	AND (:p_IdClasificacion IS NULL OR CAR."IdClasificacionTipoInstrumento" = :p_IdClasificacion)
	AND (:p_IdTipoInstrumento IS NULL OR CAR."IdTipoInstrumento" = :p_IdTipoInstrumento)
	AND (:p_IdInstrumento IS NULL OR CAR."IdInstrumento" = :p_IdInstrumento)
	AND (:p_Nemotecnico IS NULL OR CAR."Nemotecnico" = :p_Nemotecnico)
	AND CAR."FechaVencimientoOperacion" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta
	UNION ALL
--VENPAC  
 SELECT
 		CASE WHEN SUB."IdClasificacionTipoInstrumento" IN ('RF','IF') AND inst3."NumeroCupones" = 0 THEN SUB."FechaVencimientoInstrumento" ELSE SUB."INGFechaPagoCorteCupon" END AS "FechaVencimientoInstrumento"
    	,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = CAR."IdClienteRelacionado" THEN CAR."IdClienteRelacionado" ELSE CAR."IdCliente" END as "IdCliente"	
	    ,CPR."DocumentoIdentidad"
	    ,CPR."CodigoP"
	    ,CPR."NombreCliente"
	    ,CASE WHEN :p_IdCliente > 0 AND :p_IdCliente = CAR."IdClienteRelacionado" THEN CAR."IdPortafolioRelacionado"  ELSE CAR."IdPortafolio" END as "IdPortafolio"
	    ,CPR."Portafolio"
	    ,CPR."DocumentoIdentidadAdicional"
	    ,CPR."TipoDocumentoIdentidadAdicional"
	 	,EJC."NombreCompletoEjecutivo"
 		,SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
   		,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
   		,SUB."IdInstrumento" as "IdInstrumento"
   		,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
    	,SUB."ISIN"
	    ,SUB."IdEmisor"
	    ,SUB."IdBoveda"
	    ,CAR."IdCuentaCustodioPortafolio"
	    ,SUB."IdMonedaSistema"
	    ,SUB."IdMonedaInstrumento"
	    ,SUB."IdTipoReajusteSubyacente" AS "IdTipoReajuste"
	    ,CASE
			WHEN COALESCE(SUB."IdTipoReajusteSubyacente",'') <> '' THEN SUB."IdTipoReajusteSubyacente"
			WHEN COALESCE(SUB."IdMonedaInstrumento",'') <> '' THEN SUB."IdMonedaInstrumento"
			ELSE ''
		END as "IdMonedaMontos"
  		,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUM(SUB."INGValorRescate") ELSE SUM(SUB."INGCantidad")  END 	AS "Cantidad"
  		,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUM(SUB."INGValorRescate") ELSE SUM(SUB."INGCantidad")  END 	AS "ValorRescate" 
  		,SUB."INGPrecioSubyacente"	AS "PrecioCompra"
	    ,SUB."INGTasaSubyacente"	AS "TasaCompra"
	    ,CASE
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 THEN SUM(SUB."INGMontoCorteCupon")
			WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(SUB."INGValorRescate")
			ELSE SUM(SUB."INGMontoATasaCompraActual")
		END AS "MontoATasaCompraActual"
	   ,CASE
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 THEN SUM(SUB."INGMontoCorteCuponMonedaInstrumento")
			WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(SUB."INGValorRescate")
			ELSE SUM(SUB."INGMontoCompraActualMonedaInstrumento")
		END AS "MontoCompraActualMonedaInstrumento"
	   ,CASE
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(SUB."INGMontoInteresCompraProximoMonedaInstrumento")
		END  AS "MontoInteresCorridoCompraActual"
	   ,CASE
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(SUB."INGMontoReajusteCompraAnteriorMonedaInstrumento")
		END  AS "MontoReajusteCorridoCompraActual"
	   ,case when SUB."IdClasificacionTipoInstrumento"= 'IF' then SUB."EsFungible" else 'N' end as "EsFungible"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("TieneCorteDeCupon") AS "TieneCorteDeCupon"
	   ,CAR."IdConcepto"
	   ,'UI_CARTERA' AS "Origen"
	   ,CMC."PoseeInstrumentoSubyacente"
	   ,CAR."IdTipoComprobante"
	   ,CAR."FolioComprobante"
	   ,CASE
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 THEN 'UI_VENCIMIENTO_CORTE_CUPON'
			WHEN SUB."IdClasificacionTipoInstrumento" = 'RF' THEN 'UI_VENCIMIENTO_RF'
			WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN 'UI_VENCIMIENTO_IF'
		END AS "TipoVencimiento"
	   ,CPR."NombreProductoAgrupador"
	   ,CASE
			WHEN CAR."IdEstado" = 'VIG' THEN 'UI_VIGENTE'
		END AS "EstadoVencimiento"
	FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC
		ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN ClientePortafolio AS CPR ON (CPR."IdPortafolio" = CAR."IdPortafolioRelacionado" OR CPR."IdPortafolio" = :p_IdPortafolio )
	LEFT JOIN Ejecutivo AS EJC ON EJC."IdPortafolio" = CAR."IdPortafolio"
	WHERE CAR."IdEmpresa" = :p_IdEmpresa
	AND CAR."IdEstado" IN ('VIG')
	AND	(CAR."IdClienteRelacionado" = CASE WHEN IFNULL(:p_IdCliente,0) > 0 THEN :p_IdCliente ELSE CAR."IdClienteRelacionado" END OR CAR."IdCliente" = CASE WHEN IFNULL(:p_IdCliente,0)>0 THEN :p_IdCliente ELSE CAR."IdCliente" END )
	AND
		 (
				SUB."IdClasificacionTipoInstrumento" IN ('RF','IF') AND inst3."NumeroCupones" = 0 AND SUB."FechaVencimientoInstrumento" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta	   
		   		OR
		   		SUB."IdClasificacionTipoInstrumento" = 'RF' AND inst3."NumeroCupones" > 0 AND SUB."INGFechaPagoCorteCupon" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta
			)
	AND	SUB."IdClasificacionTipoInstrumento" IN ('IF','RF')
	AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
	AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE SUB."IdInstrumento" END
	AND SUB."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE SUB."Nemotecnico" END
	AND CAR."IdTipoInstrumento" = 'PAC'
	AND SUB."IdEstado"='VIG'
	AND CAR."IdTipoOperacion"   = 'VENPAC' 
	GROUP BY SUB."IdClasificacionTipoInstrumento",SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."NemoSVS",SUB."Nemotecnico",SUB."IdEmisor", SUB."EsFungible"
		   ,CAR."IdClienteRelacionado",CAR."IdCliente",CAR."IdPortafolioRelacionado",CAR."IdPortafolio" ,SUB."ISIN",SUB."INGValorRescate",SUB."INGCantidad"
		   ,SUB."IdBoveda",inst3."NumeroCupones",SUB."FechaVencimientoInstrumento",SUB."INGFechaPagoCorteCupon",SUB."INGPrecioSubyacente"
		   ,CAR."IdCuentaCustodioPortafolio",SUB."INGTasaSubyacente",SUB."INGMontoCorteCupon",SUB."INGMontoCompraActualMonedaInstrumento"
		   ,SUB."IdMonedaSistema",SUB."INGMontoATasaCompraActual",SUB."INGMontoInteresCompraProximoMonedaInstrumento",SUB."INGMontoReajusteCompraAnteriorMonedaInstrumento"
		   ,SUB."IdMonedaInstrumento","TieneCorteDeCupon",CAR."IdConcepto",CMC."PoseeInstrumentoSubyacente" ,CAR."IdTipoComprobante"
		   ,CAR."FolioComprobante" , CAR."IdEstado",SUB."IdTipoReajusteSubyacente",CPR."NombreProductoAgrupador"  ,CPR."Portafolio"
		   ,CPR."DocumentoIdentidadAdicional"  ,CPR."DocumentoIdentidad"
		   ,CPR."CodigoP",CPR."NombreCliente",CPR."TipoDocumentoIdentidadAdicional",EJC."NombreCompletoEjecutivo" 
	UNION ALL  --GARANTIAS
	SELECT
		GAR."FechaVencimientoInstrumento"
	   ,GAR."IdCliente"
	   ,CPR."DocumentoIdentidad"
	   ,CPR."CodigoP"
	   ,CPR."NombreCliente"
	   ,GAR."IdPortafolio"
	   ,CPR."Portafolio"
	   ,CPR."DocumentoIdentidadAdicional"
	   ,CPR."TipoDocumentoIdentidadAdicional"
	   ,EJC."NombreCompletoEjecutivo"
	   ,GAR."IdClasificacionTipoInstrumento"
	   ,GAR."IdTipoInstrumento"
	   ,GAR."IdInstrumento"
	   ,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' THEN GAR."NemoSVS" ELSE GAR."Nemotecnico" END AS "Nemotecnico"
	   ,CAR."ISIN"
	   ,GAR."IdEmisor"
	   ,CAR."IdBoveda"
	   ,CAR."IdCuentaCustodioPortafolio"
	   ,GAR."IdMonedaSistema"
	   ,GAR."IdMonedaInstrumento"
	   ,GAR."IdTipoReajuste"
	   ,CASE
			WHEN COALESCE(GAR."IdTipoReajuste",'') <> '' THEN GAR."IdTipoReajuste"
			WHEN COALESCE(GAR."IdMonedaInstrumento",'') <> '' THEN GAR."IdMonedaInstrumento"
			ELSE ''
		END as "IdMonedaMontos"
	   ,SUM(GAR."INGCantidad") AS "Cantidad"
	   ,SUM(GAR."INGValorRescate") AS "ValorRescate"
	   ,GAR."INGPrecioGarantia" AS "PrecioCompra"
	   ,GAR."INGTasaGarantia" AS "TasaCompra"
	   ,CASE
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(GAR."INGMontoCorteCupon")
			WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(GAR."INGValorRescate")
			ELSE SUM(GAR."INGMontoATasaCompraActual")
		END AS "MontoATasaCompraActual"
		,CASE
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(GAR."INGMontoCorteCuponMonedaInstrumento")
			WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' THEN SUM(GAR."INGValorRescate")
			ELSE SUM(GAR."INGMontoInteresCompraProximoMonedaInstrumento")
		END AS "MontoCompraActualMonedaInstrumento"
	   ,CASE
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(GAR."INGMontoInteresCompraProximoMonedaInstrumento")
		END  AS "MontoInteresCorridoCompraActual"
	   ,CASE
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN SUM(0)
			ELSE SUM(GAR."INGMontoReajusteCompraAnteriorMonedaInstrumento")
		END  AS "MontoReajusteCorridoCompraActual"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(GAR."EsFungible") AS "EsFungible"
	   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(GAR."TieneCorteDeCupon") AS "TieneCorteDeCupon"
	   ,GAR."IdConcepto"
	   ,'UI_GARANTIA' AS "Origen"
	   ,'N' AS "PoseeInstrumentoSubyacente"
	   ,GAR."IdTipoComprobante"
	   ,GAR."FolioComprobante"
	   ,CASE
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 THEN 'UI_VENCIMIENTO_CORTE_CUPON'
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' THEN 'UI_VENCIMIENTO_RF'
			WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' THEN 'UI_VENCIMIENTO_IF'
		END AS "TipoVencimiento"
	   ,CPR."NombreProductoAgrupador"
	   ,CASE
			WHEN GAR."IdEstado" = 'VIG' THEN 'UI_VIGENTE'
			WHEN GAR."IdEstado" = 'VCD' THEN 'UI_VENCIDO'
		END AS "EstadoVencimiento"
	FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			ON CAR."IdCartera" = GAR."IdCarteraOrigen"
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
			ON INS."IdInstrumento" = GAR."IdInstrumento"
	INNER JOIN ClientePortafolio AS CPR
			ON CPR."IdPortafolio" = GAR."IdPortafolio"
	LEFT JOIN Ejecutivo AS EJC
			ON EJC."IdPortafolio" = GAR."IdPortafolio"
	WHERE
	GAR."IdEstado" IN ('VIG')
	AND CAR."IdTipoOperacion" != 'VENPAC'
	AND GAR."FechaVencimientoInstrumento" > '0001-01-01'
	AND GAR."IdEmpresa" = :p_IdEmpresa
	AND GAR."IdClasificacionTipoInstrumento" IN ('RF','IF','DV')
	AND (:p_IdClasificacion IS NULL OR GAR."IdClasificacionTipoInstrumento" = :p_IdClasificacion)
	AND (:p_IdTipoInstrumento IS NULL OR GAR."IdTipoInstrumento" = :p_IdTipoInstrumento)
	AND (:p_IdInstrumento IS NULL OR GAR."IdInstrumento" = :p_IdInstrumento)
	AND (:p_Nemotecnico IS NULL OR GAR."Nemotecnico" = :p_Nemotecnico)
	AND (
			GAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND INS."NumeroCupones" = 0 AND GAR."FechaVencimientoInstrumento" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta	   
	   		OR
	   		GAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 AND GAR."INGFechaPagoCorteCupon" BETWEEN :p_FechaVencimientoDesde AND :p_FechaVencimientoHasta
		)
	GROUP BY GAR."FechaVencimientoInstrumento",GAR."IdCliente",CPR."DocumentoIdentidad",CPR."CodigoP",CPR."NombreCliente",GAR."IdPortafolio",CPR."Portafolio",CPR."DocumentoIdentidadAdicional",
		CPR."TipoDocumentoIdentidadAdicional",EJC."NombreCompletoEjecutivo",GAR."IdClasificacionTipoInstrumento",GAR."IdTipoInstrumento",GAR."IdInstrumento",GAR."NemoSVS",GAR."Nemotecnico",CAR."ISIN",
		GAR."IdEmisor",CAR."IdBoveda",CAR."IdCuentaCustodioPortafolio",GAR."IdMonedaSistema",GAR."IdMonedaInstrumento",GAR."IdTipoReajuste",GAR."EsFungible",GAR."TieneCorteDeCupon",GAR."IdConcepto",
		GAR."IdTipoComprobante",GAR."FolioComprobante",INS."NumeroCupones",CPR."NombreProductoAgrupador",GAR."IdEstado",GAR."INGPrecioGarantia",GAR."INGTasaGarantia";

 	p_out_CustodiaVencimientos =
	WITH
	Diccionario AS
	(
		SELECT
			DIC."CodDiccionario"
		   ,DIC."Descripcion"
		FROM "BSCL.Plataforma.BD::PLA.Diccionario" AS DIC
		INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CNF ON CNF."CodigoParametro" = 'PLA.IdiomaSistema' AND CNF."Valor" = DIC."CodLenguaje"
		WHERE CNF."IdEmpresa" = :p_IdEmpresa
 	)
	SELECT DISTINCT
		CVEN."FechaVencimientoInstrumento"
	   ,CVEN."IdCliente"
	   ,CVEN."DocumentoIdentidad"
	   ,CVEN."CodigoP"
	   ,CVEN."NombreCliente"
	   ,CVEN."IdPortafolio"
	   ,CVEN."DocumentoIdentidadAdicional" AS "RutCAVALI"
	   ,CVEN."Portafolio"
	   ,CVEN."NombreCompletoEjecutivo"
	   ,CCPI."NombreTipoCustodia"
	   ,CVEN."IdClasificacionTipoInstrumento"
	   ,CVEN."IdTipoInstrumento"
	   ,TINS."Codigo" AS "CodigoTipoInstrumento"
	   ,TINS."Nombre" AS "NombreTipoInstrumento"
	   ,CVEN."IdInstrumento"
	   ,CVEN."Nemotecnico"
	   ,CVEN."ISIN"
	   ,CVEN."IdEmisor"
	   ,CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE CVEN."IdBoveda" END "IdBoveda"
	   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
	   ,CVEN."IdMonedaSistema"
	   ,CVEN."IdMonedaInstrumento"
	   ,CVEN."IdTipoReajuste"
	   ,CVEN."IdMonedaMontos"
	   ,CVEN."Cantidad"
	   ,CVEN."ValorRescate" AS ValorRescate
	   ,CVEN."PrecioCompra"
	   ,CVEN."TasaCompra"
	   ,CVEN."MontoATasaCompraActual"
	   ,CVEN."MontoCompraActualMonedaInstrumento"
	   ,CVEN."MontoInteresCorridoCompraActual"
	   ,CVEN."MontoReajusteCorridoCompraActual"
	   ,CVEN."EsFungible"
	   ,CVEN."TieneCorteDeCupon"
	   ,CVEN."IdConcepto"
	   --,CPT."NombreConcepto"
	   ,CVEN."Origen"
	   ,DIC."Descripcion" AS "OrigenTraduccion"
	   ,CVEN."PoseeInstrumentoSubyacente"
	   ,CVEN."IdTipoComprobante"
	   ,CVEN."FolioComprobante"
	   ,CVEN."TipoVencimiento"
	   ,DICTVEN."Descripcion" AS "TipoVencimientoTraduccion"
	   ,CVEN."NombreProductoAgrupador"
	   ,DICEST."Descripcion" AS "EstadoVencimiento"
	   ,CASE 
	     WHEN Ind."ValorIndicador" = 'S' THEN 'SI'
	    ELSE 'NO'
	    END AS "AdminCartera"	
	FROM :v_CustodiaVencimientos AS CVEN
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = CVEN."IdTipoInstrumento"
	--INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia" AS CPT ON CPT."IdConcepto" = CVEN."IdConcepto" and CPT."NombreEntidad" = 'CUS.MOVIMIENTOCUSTODIA'
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CVEN."IdCuentaCustodioPortafolio"	
	LEFT JOIN (SELECT SUM(x."INGValorRescate") AS ValorRescateGarantia, x."IdInstrumento",x."IdPortafolio",x."IdCliente",x."IdEmisor",x."FechaVencimientoInstrumento", x."IdMonedaEmision", x."IdMonedaInstrumento", x."IdMonedaSistema" FROM "BSCL.Custodia.BD::GTA.Garantia" x
				WHERE (:p_IdCliente IS NULL OR x."IdCliente"=:p_IdCliente) AND(:p_IdPortafolio IS NULL OR x."IdPortafolio" = :p_IdPortafolio) AND(:p_IdInstrumento IS NULL OR x."IdInstrumento"=:p_IdInstrumento) AND (:p_Nemotecnico IS NULL OR x."Nemotecnico" = :p_Nemotecnico) AND  "IdEstado" ='VIG' 
				GROUP BY x."IdInstrumento",x."IdInstrumento",x."IdPortafolio",x."IdCliente", x."FechaVencimientoInstrumento",x."IdEmisor",x."IdMonedaEmision", x."IdMonedaInstrumento", x."IdMonedaSistema") AS GAR
		 ON (CVEN."IdEmisor" = GAR."IdEmisor" AND GAR."IdInstrumento" = CVEN."IdInstrumento" AND GAR."IdMonedaInstrumento" = CVEN."IdMonedaInstrumento" AND GAR."FechaVencimientoInstrumento" = CVEN."FechaVencimientoInstrumento" AND GAR."IdPortafolio"=CVEN."IdPortafolio")
    LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS  Ind ON Ind."IdEntidad" = CVEN."IdPortafolio" AND Ind."IdTipoEntidad" = 'PORTAFOLIO' AND "IdTipoIndicador" = 8
    LEFT JOIN Diccionario AS DIC ON DIC."CodDiccionario" = CVEN."Origen"
	LEFT JOIN Diccionario AS DICTVEN ON DICTVEN."CodDiccionario" = CVEN."TipoVencimiento"
	LEFT JOIN Diccionario AS DICEST ON DICEST."CodDiccionario" = CVEN."EstadoVencimiento"
	WHERE CVEN."EstadoVencimiento" IN ('UI_VIGENTE','UI_VENCIDO');

END;				 		
GO