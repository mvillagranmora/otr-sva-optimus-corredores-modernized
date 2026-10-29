
SET SCHEMA "BSCL";
GO
 
DROP PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::ControlFacturacionOperaciones";
GO


--
--  Proposito: Obtener datos para la confeccion de reporte Control de Facturacion de Operaciones
--  Autor: ECF
--  Fecha Creacion: 10-07-2025
--
 
CREATE PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::ControlFacturacionOperaciones" (
	-- Parametros entrada 
    IN p_IdEmpresa BIGINT,	
    IN p_IdUsuario BIGINT,
	IN p_FechaOperacion VARCHAR(20),
	IN p_AgruparPorBolsa VARCHAR(1) DEFAULT 'S',
	IN p_Bolsa VARCHAR(10) DEFAULT NULL,
	IN p_Mercado VARCHAR(10),
	IN p_TipoInstrumento VARCHAR(10),
	IN p_Moneda VARCHAR(10),

	IN p_Modo VARCHAR(10) DEFAULT 'BASE',
	IN p_TipoBloque VARCHAR(50) DEFAULT NULL
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
   --------------------
   --Variables
   --------------------
   	DECLARE v_FechaSistemaUTC    NVARCHAR(40);
    DECLARE v_FechaSistemaLOCAL       NVARCHAR(40);
    DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
    DECLARE v_FechaSistema       SECONDDATE;
    DECLARE v_FechaHoraUTC     NVARCHAR(40);
    DECLARE v_FechaHoraLOCAL     NVARCHAR(40);
   
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
	        p_IdEmpresa                 => 1,
	        p_out_FechaSistemaUTC       => v_FechaSistemaUTC,
	        p_out_FechaSistemaLOCAL     => v_FechaSistemaLOCAL,
	        p_out_FechaHoraSistemaUTC   => v_FechaHoraSistemaUTC,
	        p_out_FechaHoraSistemaLOCAL => v_FechaSistema,
	        p_out_FechaHoraUTC          => v_FechaHoraUTC,
	        p_out_FechaHoraLOCAL        => v_FechaHoraLOCAL
	);
	
		
	CREATE LOCAL TEMPORARY TABLE #ComprobantesUnicos (
	   "IdComprobante" BIGINT,
	   "TotalComisCmbte" DECIMAL(38, 8),
	   "TotalDereCmbte" DECIMAL(38, 8),
	   "TotalGastoCmbte" DECIMAL(38, 8),
	   "TotalImptoCmbte" DECIMAL(38, 8),
	   "MontoLiquidacionFinalFirmado" DECIMAL(38, 8),
	   "IdTipoComprobante" NVARCHAR(20),
	   "AccionTipoOperacion" NVARCHAR(20)
	);
	CREATE LOCAL TEMPORARY TABLE #DetalleBase (
	    "OrdenTipo" INTEGER,
	    "TipoRegistro" NVARCHAR(20),
	    "NombreEmpresa" NVARCHAR(200),
	    "DocumentoIdentidadEmisor" NVARCHAR(50),
	    "FechaCorte" VARCHAR(20),
	    "FechaSistema" NVARCHAR(40),
	    "HoraImpresion" NVARCHAR(40),
	    "IdMonedaLiquidacion" NVARCHAR(10),
	    "IdClasificacionTipoInstrumento" NVARCHAR(30),
	    "NombreTipoInstrumento" NVARCHAR(100),
	    "IdTipoInstrumento" NVARCHAR(30),
	    "IdBolsa" NVARCHAR(30),
	    "DocumentoIdentidadReceptor" NVARCHAR(50),
	    "NombrePortafolio" NVARCHAR(200),
	    "RazonSocialReceptor" NVARCHAR(200),
	    "NombreEmpleadoEjecutivoPortafolio" NVARCHAR(200),
	    "IdTipoComprobante" NVARCHAR(20),
	    "IdComprobante" BIGINT,
	    "NemotecnicoInstrumento" NVARCHAR(100),
	    "CodigoCondicionLiquidacion" NVARCHAR(50),
	    "Precio" DECIMAL(38, 8),
	    "Cantidad" DECIMAL(38, 8),
	    "IdFormaCustodia" NVARCHAR(20),
	    "Compra" DECIMAL(38, 8),
	    "Venta" DECIMAL(38, 8),
	    "TotalComisCmbte" DECIMAL(38, 8),
	    "TotalDereCmbte" DECIMAL(38, 8),
	    "TotalGastoCmbte" DECIMAL(38, 8),
	    "TotalImptoCmbte" DECIMAL(38, 8),
	    "AccionTipoOperacion" NVARCHAR(10),
	    "MontoLiquidacionFinal" DECIMAL(38, 8),
	     "LogoCorredora" BLOB
	);
	CREATE LOCAL TEMPORARY TABLE #ResumenMercado (
							"p_AgruparPorBolsa" NVARCHAR(5),
							"OrdenTipo" INT,
							"TipoRegistro" NVARCHAR(50),
							"NombreEmpresa" NVARCHAR(200),
							"DocumentoIdentidadEmisor" NVARCHAR(50),
							"FechaCorte" VARCHAR(20),
						    "FechaSistema" NVARCHAR(40),
						    "HoraImpresoin" NVARCHAR(40),
							"DocumentoIdentidadReceptor" NVARCHAR(50),
							"IdComprobante" NVARCHAR(50),
							"IdTipoInstrumento" NVARCHAR(50),
							"NombrePortafolio" NVARCHAR(200),
							"NombreEmpleadoEjecutivoPortafolio" NVARCHAR(200),
							"NemotecnicoInstrumento" NVARCHAR(50),
							"CodigoCondicionLiquidacion" NVARCHAR(50),
							"Precio" DECIMAL(38, 8),
							"Cantidad" DECIMAL(38, 8),
							"IdFormaCustodia" NVARCHAR(50),
							"IdClasificacionTipoInstrumento" NVARCHAR(50),
							"IdTipoComprobante" NVARCHAR(50),
							"AccionTipoOperacion" NVARCHAR(50),
							"IdMonedaLiquidacion" NVARCHAR(50),
							"IdBolsa" NVARCHAR(50),
							"Compra" DECIMAL(38, 8),
							"Venta" DECIMAL(38, 8),
							"TotalComisCmbte" DECIMAL(38, 8),
							"TotalDereCmbte" DECIMAL(38, 8),
							"TotalGastoCmbte" DECIMAL(38, 8),
							"TotalImptoCmbte" DECIMAL(38, 8),
							"MontoLiquidacionFinal" DECIMAL(38, 8),
							"LogoCorredora" BLOB
						);
	CREATE LOCAL TEMPORARY TABLE #ResumenDiario (
	    "IdMonedaLiquidacion" NVARCHAR(50),
	    "IdClasificacionTipoInstrumento" NVARCHAR(50),
	    "CodigoCondicionLiquidacion" NVARCHAR(50),
	    "IdBolsa" NVARCHAR(50),
	
	    "CompraCuentaPropia" DECIMAL(38, 8),
	    "CompraTercero" DECIMAL(38, 8),
	    "VentaCuentaPropia" DECIMAL(38, 8),
	    "VentaTercero" DECIMAL(38, 8)
	);
	CREATE LOCAL TEMPORARY TABLE #ResumenMercadoSinBolsa (
						"p_AgruparPorBolsa" NVARCHAR(5),
						"OrdenTipo" INT,
						"TipoRegistro" NVARCHAR(50),
						"NombreEmpresa" NVARCHAR(200),
						"DocumentoIdentidadEmisor" NVARCHAR(50),
						"FechaCorte" NVARCHAR(40),
					    "FechaSistema" NVARCHAR(40),
					    "HoraImpresoin" NVARCHAR(40),
						"DocumentoIdentidadReceptor" NVARCHAR(50),
						"IdComprobante" NVARCHAR(50),
						"IdTipoInstrumento" NVARCHAR(50),
						"NombrePortafolio" NVARCHAR(200),
						"NombreEmpleadoEjecutivoPortafolio" NVARCHAR(200),
						"NemotecnicoInstrumento" NVARCHAR(50),
						"CodigoCondicionLiquidacion" NVARCHAR(50),
						"Precio" DECIMAL(38, 8),
						"Cantidad" DECIMAL(38, 8),
						"IdFormaCustodia" NVARCHAR(50),
						"IdClasificacionTipoInstrumento" NVARCHAR(50),
						"IdTipoComprobante" NVARCHAR(50),
						"AccionTipoOperacion" NVARCHAR(50),
						"IdMonedaLiquidacion" NVARCHAR(50),
						"IdBolsa" NVARCHAR(50),
						"Compra" DECIMAL(38, 8),
						"Venta" DECIMAL(38, 8),
						"TotalComisCmbte" DECIMAL(38, 8),
						"TotalDereCmbte" DECIMAL(38, 8),
						"TotalGastoCmbte" DECIMAL(38, 8),
						"TotalImptoCmbte" DECIMAL(38, 8),
						"MontoLiquidacionFinal" DECIMAL(38, 8),
						"LogoCorredora" BLOB
					);

	-- 1. Detalle General
	INSERT INTO #ComprobantesUnicos
		SELECT
			    "IdComprobante",
			    MAX("TotalComisCmbte") AS "TotalComisCmbte",
			    MAX("TotalDereCmbte") AS "TotalDereCmbte",
			    MAX("TotalGastoCmbte") AS "TotalGastoCmbte",
			    MAX("TotalImptoCmbte") AS "TotalImptoCmbte",
			    CASE
			      WHEN MAX("IdMonedaLiquidacion") = 'USD' THEN
			        ABS(MAX("MontoComprobanteFinal"))
			      ELSE
			        ABS(MAX("MontoLiquidacionFinal"))
			    END AS "MontoLiquidacionFinal",
			    MAX("IdTipoComprobante") AS "IdTipoComprobante",
			    CASE 
				    WHEN MAX("AccionTipoOperacion") = 'VENTA' AND MAX("IdTipoComprobante") IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN 'COMPRA'
				    WHEN MAX("AccionTipoOperacion") = 'COMPRA' AND MAX("IdTipoComprobante") IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN 'VENTA'
			    ELSE MAX("AccionTipoOperacion")
		    END AS "AccionTipoOperacion"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
			WHERE 
			    (:p_Bolsa = '0' OR :p_AgruparPorBolsa <> 'S' OR "IdBolsa" = :p_Bolsa)
			    AND (:p_Mercado = '0' OR "IdClasificacionTipoInstrumento" = :p_Mercado)
			    AND (:p_TipoInstrumento = '0' OR "IdTipoInstrumento" = :p_TipoInstrumento)
			    AND (:p_Moneda = '0' OR "IdMonedaLiquidacion" = :p_Moneda)
			    AND (
				    (COALESCE(:p_TipoBloque, '') = 'RESUMEN_CONSOLIDADO' AND
				     "FechaComprobante" BETWEEN 
				        ADD_DAYS(TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'), -EXTRACT(DAY FROM TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD')) + 1)
				        AND TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'))
				
				    OR (COALESCE(:p_TipoBloque, '') <> 'RESUMEN_CONSOLIDADO' AND
				        (:p_FechaOperacion = '0' OR "FechaComprobante" = :p_FechaOperacion))
				)
			    AND "IdEstadoComprobante" <> 'ANU'
			GROUP BY "IdComprobante";		
	
	INSERT INTO #DetalleBase
		SELECT 
		    0 AS "OrdenTipo",
		    'DETALLE' AS "TipoRegistro",
		    ConsCmbte."NombreEmpresa",
		    ConsEmp."DocumentoIdentidadEmisor",
		    p_FechaOperacion AS "FechaCorte",
		    TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY') AS "FechaSistema",
       		TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI') AS "HoraImpresion",
		    ConsCmbte."IdMonedaLiquidacion",
		    CASE 
			  WHEN ConsCmbte."IdTipoOperacion" IN ('COMOTC', 'VENOTC', 'ANCOTC', 'ANVOTC') THEN 'OC'
			  ELSE ConsCmbte."IdClasificacionTipoInstrumento"
			END AS "IdClasificacionTipoInstrumento",
		    CASE 
			  WHEN ConsCmbte."IdTipoOperacion" IN ('COMOTC', 'VENOTC', 'ANCOTC', 'ANVOTC') THEN 'OTROS CONCEPTOS'
			  ELSE ConsCmbte."NombreTipoInstrumento"
			END AS "NombreTipoInstrumento",
		    CASE 
			  WHEN ConsCmbte."IdTipoOperacion" IN ('COMOTC', 'VENOTC', 'ANCOTC', 'ANVOTC') THEN 'OTROS CONCEPTOS'
			  ELSE ConsCmbte."IdTipoInstrumento"
			END AS "IdTipoInstrumento",
		    ConsCmbte."IdBolsa",
		    COALESCE(IDENT."DocumentoIdentidad", IDENT."DocumentoIdentidad",'') AS "DocumentoIdentidadReceptor", 
		    ConsCmbte."NombrePortafolio", 
		    CAST(ConsCmbteConDet."RazonSocial" AS VARCHAR(200)) AS "RazonSocialReceptor",
		    ConsCmbte."NombreEmpleadoEjecutivoPortafolio",
		    ConsCmbte."IdTipoComprobante",
		    ConsCmbte."IdComprobante",
		    CASE 
		        WHEN ConsCmbte."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN ConsCmbteConDet."NemotecnicoSubyacente"
		        WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN Det."NemoSVS"
		        ELSE ConsCmbteConDet."NemotecnicoInstrumento"
		    END AS "NemotecnicoInstrumento", 
		    CAST(CondLiq."Codigo" AS VARCHAR(200)) AS "CodigoCondicionLiquidacion",
		    CASE
		        WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' AND ConsCmbte."IdTipoComprobante" <> 'BOE' THEN TO_DECIMAL(ROUND(NULLIF(ConsCmbte."ValorTipoCambio",0), 2), 26, 2) 
				WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN ConsCmbteConDet."Tasa"
		        ELSE ConsCmbteConDet."Precio"
		    END AS "Precio",
		    CASE
		        WHEN ConsCmbteConDet."Cantidad" != 0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND ConsCmbte."IdTipoComprobante" = 'FAE' THEN MovCom."ValorRescate"
		        WHEN ConsCmbteConDet."Cantidad" != 0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN ConsCmbteConDet."ValorRescate"
		        WHEN ConsCmbteConDet."Cantidad" != 0 THEN ConsCmbteConDet."Cantidad"
		        ELSE ConsCmbteConDet."Cantidad"
		    END AS "Cantidad",
		    ConsCmbte."IdFormaCustodia",
		    CASE 
		        WHEN ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'VENTA' THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle") 
		        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'COMPRA' AND ConsCmbte."IdTipoComprobante" = 'FAE' AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN MovCom."MontoMovimientoComprobante"
		        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") IN ('COMPRA', 'RESCATE') THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
		        ELSE 0
		    END AS "Compra",
		    CASE
		        WHEN ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") IN ('COMPRA', 'RESCATE') THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle") 
		        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'VENTA' AND ConsCmbte."IdTipoComprobante" = 'FAE' AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN MovCom."MontoMovimientoComprobante"
		        WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND UPPER(ConsCmbteConDet."AccionTipoOperacion") = 'VENTA' THEN COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ConsCmbteConDet."MontoComprobanteDetalle", FormCmbteLoc."MontoDecimales", FormCmbteLoc."MontoTrucaRedondea"), ConsCmbteConDet."MontoComprobanteDetalle")
		        ELSE 0
		    END AS "Venta",
		    ConsCmbte."TotalComisCmbte",
		    ConsCmbte."TotalDereCmbte",
		    ConsCmbte."TotalGastoCmbte",
		    ConsCmbte."TotalImptoCmbte", 
		    CASE 
			    WHEN (ConsCmbteConDet."AccionTipoOperacion") = 'VENTA' AND ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN 'COMPRA'
			    WHEN (ConsCmbteConDet."AccionTipoOperacion") = 'COMPRA' AND ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN 'VENTA'
			    ELSE ConsCmbteConDet."AccionTipoOperacion"
		    END AS "AccionTipoOperacion",
		    CASE 
			  WHEN ConsCmbte."IdMonedaLiquidacion" = 'USD' THEN
			    CASE 
			      WHEN ConsCmbte."MontoComprobanteFinal" < 0 THEN ConsCmbte."MontoComprobanteFinal" * -1
			      ELSE ConsCmbte."MontoComprobanteFinal"
			    END
			  ELSE
			    CASE 
			      WHEN ConsCmbte."MontoLiquidacionFinal" < 0 THEN ConsCmbte."MontoLiquidacionFinal" * -1
			      ELSE ConsCmbte."MontoLiquidacionFinal"
			    END
			END AS "MontoLiquidacionFinal",
		   Logo."LogoCorredora" AS "LogoCorredora"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS ConsCmbte
		INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" AS ConsCmbteConDet
		    ON ConsCmbteConDet."IdComprobante" = ConsCmbte."IdComprobante"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS Det
	    	ON Det."IdComprobanteDetalle" = ConsCmbteConDet."IdComprobanteDetalle"
		INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CondLiq
		    ON CondLiq."IdCondicionDeLiquidacion" = ConsCmbte."IdCondicionLiquidacion"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS MovComDet
		    ON MovComDet."IdComprobanteDetalle" = ConsCmbteConDet."IdComprobanteDetalle"
		    AND (MovComDet."IdTipoInstrumento" ='SPT' OR MovComDet."IdClasificacionTipoInstrumento"= 'IF')
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCom
		    ON MovCom."IdMovimientoComprobante" = MovComDet."IdMovimientoComprobante"
		    AND (MovCom."IdTipoInstrumento" ='SPT' OR MovCom."IdClasificacionTipoInstrumento"= 'IF')
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
		    ON ConsCmbte."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
		    ON PER."IdPersona" = CLI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT
		    ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormCmbteLoc
		    ON FormCmbteLoc."IdMoneda" = ConsCmbteConDet."IdMonedaComprobante"
		    AND FormCmbteLoc."IdTipoOperacion" = ''
		    AND FormCmbteLoc."IdTipoInstrumento" = ConsCmbte."IdTipoInstrumento"
		    AND FormCmbteLoc."IdUsoMonedaFormato" = 'GEN'
		LEFT JOIN (
		    SELECT
		        EMP."IdEmpresa",
		        PER."DocumentoIdentidad" AS "DocumentoIdentidadEmisor"
		    FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
		    LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
		        ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" AND SOC."Borrado" = 'N'
		    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
		        ON PER."IdPersona" = SOC."IdPersona"
		) AS ConsEmp
		    ON ConsEmp."IdEmpresa" = p_IdEmpresa
		CROSS JOIN (
		    SELECT TOP 1 "Binario" AS "LogoCorredora"
		    FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		    WHERE "NombreImagen" = 'LOGO_CORREDOR'
		    ORDER BY "Version" DESC
		) AS Logo
WHERE 
	  (:p_Bolsa = '0' OR :p_AgruparPorBolsa <> 'S' OR ConsCmbte."IdBolsa" = :p_Bolsa)
	  AND (:p_Mercado = '0' OR ConsCmbte."IdClasificacionTipoInstrumento" = :p_Mercado)
	  AND (:p_TipoInstrumento = '0' OR ConsCmbte."IdTipoInstrumento" = :p_TipoInstrumento)
	  AND (:p_Moneda = '0' OR ConsCmbte."IdMonedaLiquidacion" = :p_Moneda)
	  AND (
		    (COALESCE(:p_TipoBloque, '') = 'RESUMEN_CONSOLIDADO' AND
		     ConsCmbte."FechaComprobante" BETWEEN 
		        ADD_DAYS(TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'), -EXTRACT(DAY FROM TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD')) + 1)
		        AND TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'))
		
		    OR (COALESCE(:p_TipoBloque, '') <> 'RESUMEN_CONSOLIDADO' AND
		        (:p_FechaOperacion = '0' OR ConsCmbte."FechaComprobante" = :p_FechaOperacion))
		)
	 AND(ConsCmbte."IdEstadoComprobante" <> 'ANU');	 
IF NOT EXISTS (SELECT 1 FROM #DetalleBase) AND :p_TipoBloque IN ('RESUMEN_DIARIO','RESUMEN_CONSOLIDADO') THEN
		SELECT
				'NODATA' AS "IdMonedaLiquidacion",
				NULL AS "IdClasificacionTipoInstrumento",
				NULL AS "CodigoCondicionLiquidacion",
				NULL AS "IdBolsa",
				0 AS "CompraCuentaPropia",
				0 AS "CompraTercero",
				0 AS "VentaCuentaPropia",
				0 AS "VentaTercero"
		FROM DUMMY;
ELSE IF NOT EXISTS (SELECT 1 FROM #DetalleBase) AND (:p_TipoBloque IS NULL OR :p_TipoBloque = '' OR :p_TipoBloque NOT IN ('RESUMEN_DIARIO', 'RESUMEN_CONSOLIDADO')) THEN
		SELECT 
		  NULL AS "p_AgruparPorBolsa",
		  100 AS "OrdenTipo",
		  'SIN_DATOS' AS "TipoRegistro",
		  NULL AS "NombreEmpresa",
		  NULL AS "DocumentoIdentidadEmisor",
		  NULL AS "FechaCorte",
		  NULL AS "FechaSistema",
		  NULL AS "HoraImpresion",
		  NULL AS "NombrePortafolio",
		  NULL AS "RazonSocialReceptor",
		  NULL AS "NombreEmpleadoEjecutivoPortafolio",
		  NULL AS "NemotecnicoInstrumento",
		  NULL AS "CodigoCondicionLiquidacion",
		  NULL AS "Precio",
		  NULL AS "Cantidad",
		  NULL AS "IdFormaCustodia",
		  NULL AS "IdTipoComprobante",
		  NULL AS "AccionTipoOperacion",
		  NULL AS "IdMonedaLiquidacion",
		  NULL AS "NombreTipoInstrumento",
		  NULL AS "IdBolsa",
		  NULL AS "IdClasificacionTipoInstrumento",
		  NULL AS "IdTipoInstrumento",
		  NULL AS "DocumentoIdentidadReceptor",
		  100 AS "IdComprobante",
		  NULL AS "Compra",
		  NULL AS "Venta",
		  NULL AS "TotalComisCmbte",
		  NULL AS "TotalDereCmbte",
		  NULL AS "TotalGastoCmbte",
		  NULL AS "TotalImptoCmbte",
		  NULL AS "MontoLiquidacionFinal"
		FROM DUMMY;
ELSE
	IF p_Modo = 'BASE' THEN
		IF p_AgruparPorBolsa = 'S' THEN
	        SELECT
	        p_AgruparPorBolsa AS "p_AgruparPorBolsa",
	        0 AS "OrdenTipo",
	        'DETALLE' AS "TipoRegistro",
	        "NombreEmpresa",
	        "DocumentoIdentidadEmisor",
	        "FechaCorte",
	        "FechaSistema",
	        "HoraImpresion",
	        "NombrePortafolio",
	        "RazonSocialReceptor",
	        "NombreEmpleadoEjecutivoPortafolio",
	        "NemotecnicoInstrumento",
	        "CodigoCondicionLiquidacion",
	        "Precio",
	        "Cantidad",
	        "IdFormaCustodia",
	        "IdTipoComprobante",
	        "IdMonedaLiquidacion",
	        "IdBolsa",
	        "IdClasificacionTipoInstrumento",
	        "IdTipoInstrumento",
	        "DocumentoIdentidadReceptor",
	        "IdComprobante",
	        "Compra",
	        "Venta",
	        "TotalComisCmbte",
	        "TotalDereCmbte",
	        "TotalGastoCmbte",
	        "TotalImptoCmbte",
	        "MontoLiquidacionFinal",
	        "LogoCorredora"
		    FROM #DetalleBase
		
		    UNION ALL
		
		    SELECT 
		        R.*, Logo."LogoCorredora"
		    FROM (
		        SELECT 
		        	p_AgruparPorBolsa AS "p_AgruparPorBolsa",
		            1 AS "OrdenTipo",    
		            'RESUMEN' AS "TipoRegistro",
		            MAX("NombreEmpresa"),
		            MAX("DocumentoIdentidadEmisor"),
		            MAX("FechaCorte"),
		            MAX("FechaSistema"),
		            MAX("HoraImpresion"),
		            NULL AS "NombrePortafolio",
		            NULL AS "RazonSocialReceptor",
		            NULL AS "NombreEmpleadoEjecutivoPortafolio",
		            NULL AS "NemotecnicoInstrumento",
		            NULL AS "CodigoCondicionLiquidacion",
		            NULL AS "Precio",
		            NULL AS "Cantidad",
		            NULL AS "IdFormaCustodia",
		            MAX("IdTipoComprobante"),
		            MAX("IdMonedaLiquidacion"),
		            MAX("IdBolsa"),
		            MAX("IdClasificacionTipoInstrumento"),
		            MAX("IdTipoInstrumento"),
		            MAX("DocumentoIdentidadReceptor"), 
		            "IdComprobante",
		            SUM("Compra"),
		            SUM("Venta"),
		            MAX("TotalComisCmbte"),
		            MAX("TotalDereCmbte"),
		            MAX("TotalGastoCmbte"),
		            MAX("TotalImptoCmbte"),
		            MAX("MontoLiquidacionFinal")
		        FROM #DetalleBase
		        GROUP BY "IdComprobante"
		    ) AS R
		    CROSS JOIN (
		        SELECT TOP 1 "LogoCorredora"
		        FROM #DetalleBase
		        WHERE "LogoCorredora" IS NOT NULL
		    ) AS Logo
		 
		    UNION ALL
		
		    SELECT 
		        R.*, Logo."LogoCorredora"
		    FROM (
		        SELECT 
					p_AgruparPorBolsa AS "p_AgruparPorBolsa",
					2 AS "OrdenTipo",
					'RESUMEN_CLIENTE' AS "TipoRegistro",
				
					MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
					MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					MAX("DBAG"."FechaCorte") AS "FechaCorte",
					MAX("DBAG"."FechaSistema") AS "FechaSistema",
					MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
					MAX("DBAG"."NombrePortafolio") AS "NombrePortafolio",
					NULL AS "RazonSocialReceptor",
					NULL AS "NombreEmpleadoEjecutivoPortafolio",
					NULL AS "NemotecnicoInstrumento",
					NULL AS "CodigoCondicionLiquidacion",
					NULL AS "Precio",
					NULL AS "Cantidad",
					NULL AS "IdFormaCustodia",
					NULL AS "IdTipoComprobante",
				
					"DBAG"."IdMonedaLiquidacion",
					MAX("DBAG"."IdBolsa") AS "IdBolsa",
					"DBAG"."IdClasificacionTipoInstrumento",
					"DBAG"."IdTipoInstrumento",
					"DBAG"."DocumentoIdentidadReceptor",
					MAX("DBAG"."IdComprobante") AS "IdComprobante",
				
					SUM("DBAG"."Compra") AS "Compra",
					SUM("DBAG"."Venta") AS "Venta",
				
					SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
					SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
					SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
					SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
					SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
				
				FROM (
					-- Agrupación por comprobante para evitar duplicados
					SELECT
						"IdComprobante",
						MAX("NombreEmpresa") AS "NombreEmpresa",
						MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						MAX("FechaCorte") AS "FechaCorte",
						MAX("FechaSistema") AS "FechaSistema",
						MAX("HoraImpresion") AS "HoraImpresion",
						MAX("NombrePortafolio") AS "NombrePortafolio",
						"DocumentoIdentidadReceptor",
						"IdMonedaLiquidacion",
						"IdClasificacionTipoInstrumento",
						"IdTipoInstrumento",
						MAX("IdTipoComprobante") AS "IdTipoComprobante",
						MAX("IdBolsa") AS "IdBolsa",
				
						SUM(CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1 ELSE "Compra" END) AS "Compra",
						SUM(CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1 ELSE "Venta" END) AS "Venta"
					FROM #DetalleBase
					GROUP BY
						"IdComprobante",
						"DocumentoIdentidadReceptor",
						"IdMonedaLiquidacion",
						"IdClasificacionTipoInstrumento",
						"IdTipoInstrumento"
				) AS "DBAG"
				
				LEFT JOIN (
					-- Montos únicos por comprobante ya calculados
					SELECT
						"IdComprobante",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalComisCmbte" * -1 ELSE "TotalComisCmbte" END AS "TotalComisCmbte",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalDereCmbte" * -1 ELSE "TotalDereCmbte" END AS "TotalDereCmbte",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalGastoCmbte" * -1 ELSE "TotalGastoCmbte" END AS "TotalGastoCmbte",
						CASE 
							WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
							WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
							ELSE "TotalImptoCmbte"
							END AS "TotalImptoCmbte",
						CASE 
							WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1 
							ELSE "MontoLiquidacionFinalFirmado" END AS "MontoLiquidacionFinalFirmado"
					FROM #ComprobantesUnicos
				) AS "CUAG"
					ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
				
				GROUP BY
					"DBAG"."IdMonedaLiquidacion",
					"DBAG"."IdClasificacionTipoInstrumento",
					"DBAG"."IdTipoInstrumento",
					"DBAG"."DocumentoIdentidadReceptor"
		    ) AS R
		    CROSS JOIN (
		        SELECT TOP 1 "LogoCorredora"
		        FROM #DetalleBase
		        WHERE "LogoCorredora" IS NOT NULL
		    ) AS Logo;
		 ELSE
	        SELECT
	        p_AgruparPorBolsa AS "p_AgruparPorBolsa",
	        0 AS "OrdenTipo",
	        'DETALLE' AS "TipoRegistro",
	        "NombreEmpresa",
	        "DocumentoIdentidadEmisor",
	        "FechaCorte",
	        "FechaSistema",
	        "HoraImpresion",
	        "NombrePortafolio",
	        "RazonSocialReceptor",
	        "NombreEmpleadoEjecutivoPortafolio",
	        "NemotecnicoInstrumento",
	        "CodigoCondicionLiquidacion",
	        "Precio",
	        "Cantidad",
	        "IdFormaCustodia",
	        "IdTipoComprobante",
	        "IdMonedaLiquidacion",
	        "IdBolsa",
	        "IdClasificacionTipoInstrumento",
	        "IdTipoInstrumento",
	        "DocumentoIdentidadReceptor",
	        "IdComprobante",
	        "Compra",
	        "Venta",
	        "TotalComisCmbte",
	        "TotalDereCmbte",
	        "TotalGastoCmbte",
	        "TotalImptoCmbte",
	        "MontoLiquidacionFinal",
	        "LogoCorredora"
		    FROM #DetalleBase
		
		    UNION ALL
		
		    SELECT 
		        R.*, Logo."LogoCorredora"
		    FROM (
		        SELECT 
		        	p_AgruparPorBolsa AS "p_AgruparPorBolsa",
		            1 AS "OrdenTipo",    
		            'RESUMEN' AS "TipoRegistro",
		            MAX("NombreEmpresa"),
		            MAX("DocumentoIdentidadEmisor"),
		            MAX("FechaCorte"),
		            MAX("FechaSistema"),
		            MAX("HoraImpresion"),
		            NULL AS "NombrePortafolio",
		            NULL AS "RazonSocialReceptor",
		            NULL AS "NombreEmpleadoEjecutivoPortafolio",
		            NULL AS "NemotecnicoInstrumento",
		            NULL AS "CodigoCondicionLiquidacion",
		            NULL AS "Precio",
		            NULL AS "Cantidad",
		            NULL AS "IdFormaCustodia",
		            MAX("IdTipoComprobante"),
		            MAX("IdMonedaLiquidacion"),
		            MAX("IdBolsa"),
		            MAX("IdClasificacionTipoInstrumento"),
		            MAX("IdTipoInstrumento"),
		            MAX("DocumentoIdentidadReceptor"), 
		            "IdComprobante",
		            SUM("Compra"),
		            SUM("Venta"),
		            MAX("TotalComisCmbte"),
		            MAX("TotalDereCmbte"),
		            MAX("TotalGastoCmbte"),
		            MAX("TotalImptoCmbte"),
		            MAX("MontoLiquidacionFinal")
		        FROM #DetalleBase
		        GROUP BY "IdComprobante"
		    ) AS R
		    CROSS JOIN (
		        SELECT TOP 1 "LogoCorredora"
		        FROM #DetalleBase
		        WHERE "LogoCorredora" IS NOT NULL
		    ) AS Logo
		 
		    UNION ALL
		
		    SELECT 
		        R.*, Logo."LogoCorredora"
		    FROM (
		        SELECT
					p_AgruparPorBolsa AS "p_AgruparPorBolsa",
					2 AS "OrdenTipo",
					'RESUMEN_CLIENTE' AS "TipoRegistro",
				
					MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
					MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					MAX("DBAG"."FechaCorte") AS "FechaCorte",
					MAX("DBAG"."FechaSistema") AS "FechaSistema",
					MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
					MAX("DBAG"."NombrePortafolio") AS "NombrePortafolio",
					NULL AS "RazonSocialReceptor",
					NULL AS "NombreEmpleadoEjecutivoPortafolio",
					NULL AS "NemotecnicoInstrumento",
					NULL AS "CodigoCondicionLiquidacion",
					NULL AS "Precio",
					NULL AS "Cantidad",
					NULL AS "IdFormaCustodia",
					NULL AS "IdTipoComprobante",
				
					"DBAG"."IdMonedaLiquidacion",
					MAX("DBAG"."IdBolsa") AS "IdBolsa",
					"DBAG"."IdClasificacionTipoInstrumento",
					"DBAG"."IdTipoInstrumento",
					"DBAG"."DocumentoIdentidadReceptor",
					MAX("DBAG"."IdComprobante") AS "IdComprobante",
				
					SUM("DBAG"."Compra") AS "Compra",
					SUM("DBAG"."Venta") AS "Venta",
					SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
					SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
					SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
					SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
					SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
				
				FROM (
					SELECT
						"IdComprobante",
						MAX("NombreEmpresa") AS "NombreEmpresa",
						MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						MAX("FechaCorte") AS "FechaCorte",
						MAX("FechaSistema") AS "FechaSistema",
						MAX("HoraImpresion") AS "HoraImpresion",
						MAX("NombrePortafolio") AS "NombrePortafolio",
						"DocumentoIdentidadReceptor",
						"IdMonedaLiquidacion",
						"IdClasificacionTipoInstrumento",
						"IdTipoInstrumento",
						MAX("IdTipoComprobante") AS "IdTipoComprobante",
						MAX("IdBolsa") AS "IdBolsa",
				
						SUM(CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1 ELSE "Compra" END) AS "Compra",
						SUM(CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1 ELSE "Venta" END) AS "Venta"
					FROM #DetalleBase
					GROUP BY
						"IdComprobante",
						"DocumentoIdentidadReceptor",
						"IdMonedaLiquidacion",
						"IdClasificacionTipoInstrumento",
						"IdTipoInstrumento"
				) AS "DBAG"
				
				LEFT JOIN (
					SELECT
						"IdComprobante",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalComisCmbte" * -1 ELSE "TotalComisCmbte" END AS "TotalComisCmbte",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalDereCmbte" * -1 ELSE "TotalDereCmbte" END AS "TotalDereCmbte",
						CASE WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "TotalGastoCmbte" * -1 ELSE "TotalGastoCmbte" END AS "TotalGastoCmbte",
						CASE 
							WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
							WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
							ELSE "TotalImptoCmbte"
							END AS "TotalImptoCmbte",
						CASE 
							WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')  THEN "MontoLiquidacionFinalFirmado" * -1 
							ELSE "MontoLiquidacionFinalFirmado" END AS "MontoLiquidacionFinalFirmado"
					FROM #ComprobantesUnicos
				) AS "CUAG"
					ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
				
				GROUP BY
					"DBAG"."IdMonedaLiquidacion",
					"DBAG"."IdClasificacionTipoInstrumento",
					"DBAG"."IdTipoInstrumento",
					"DBAG"."DocumentoIdentidadReceptor"
		    ) AS R
		    CROSS JOIN (
		        SELECT TOP 1 "LogoCorredora"
		        FROM #DetalleBase
		        WHERE "LogoCorredora" IS NOT NULL
		    ) AS Logo;
		   END IF;
	ELSEIF p_Modo = 'SUB' THEN
			 IF p_AgruparPorBolsa = 'S' THEN
			    	IF p_TipoBloque = 'RESUMEN_MERCADO' THEN			
			    		TRUNCATE TABLE #ResumenMercado;
						INSERT INTO #ResumenMercado
						SELECT 
							p_AgruparPorBolsa AS "p_AgruparPorBolsa",
							3 AS "OrdenTipo",
							'RESUMEN_MERCADO' AS "TipoRegistro",
							MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
							MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							MAX("DBAG"."FechaCorte") AS "FechaCorte",
							MAX("DBAG"."FechaSistema") AS "FechaSistema",
							MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
							MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
							MAX("DBAG"."IdComprobante") AS "IdComprobante",
							MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
							NULL AS "NombrePortafolio",
							NULL AS "NombreEmpleadoEjecutivoPortafolio",
							NULL AS "NemotecnicoInstrumento",
							NULL AS "CodigoCondicionLiquidacion",
							NULL AS "Precio",
							NULL AS "Cantidad",
							NULL AS "IdFormaCustodia",
							"DBAG"."IdClasificacionTipoInstrumento",
							"DBAG"."IdTipoComprobante",
							"DBAG"."AccionTipoOperacion",
							"DBAG"."IdMonedaLiquidacion",
							"DBAG"."IdBolsa",
							SUM("DBAG"."Compra") AS "Compra",
							SUM("DBAG"."Venta") AS "Venta",
							SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
							SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
							SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
							SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
							SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal",
							(
								SELECT TOP 1 "LogoCorredora"
								FROM #DetalleBase
								WHERE "LogoCorredora" IS NOT NULL
							) AS "LogoCorredora"
						FROM (
							SELECT
								"IdComprobante",
								MAX("NombreEmpresa") AS "NombreEmpresa",
								MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
								MAX("FechaCorte") AS "FechaCorte",
								MAX("FechaSistema") AS "FechaSistema",
								MAX("HoraImpresion") AS "HoraImpresion",
								MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
								MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
								"IdClasificacionTipoInstrumento",
								"IdTipoComprobante",
								"AccionTipoOperacion",
								"IdMonedaLiquidacion",
								"IdBolsa",
								SUM("Compra") AS "Compra",
								SUM("Venta") AS "Venta"
							FROM #DetalleBase
							GROUP BY
								"IdComprobante",
								"IdClasificacionTipoInstrumento",
								"IdTipoComprobante",
								"AccionTipoOperacion",
								"IdMonedaLiquidacion",
								"IdBolsa"
						) AS "DBAG"
						LEFT JOIN (
							SELECT
								"IdComprobante",
								"TotalComisCmbte",
								"TotalDereCmbte",
								"TotalGastoCmbte",
								"TotalImptoCmbte",
								"MontoLiquidacionFinalFirmado"
							FROM #ComprobantesUnicos
						) AS "CUAG"
							ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
						GROUP BY 
							"DBAG"."IdMonedaLiquidacion",
							"DBAG"."IdClasificacionTipoInstrumento",
							"DBAG"."IdTipoComprobante",
							"DBAG"."IdBolsa",
							"DBAG"."AccionTipoOperacion";	
						SELECT * FROM #ResumenMercado;
				    ELSEIF p_TipoBloque = 'RESUMEN_BOLSA' THEN
						-- RESUMEN_BOLSA
						SELECT 
						    R.*, Logo."LogoCorredora"
						FROM (
						   SELECT
								p_AgruparPorBolsa AS "p_AgruparPorBolsa",
								4 AS "OrdenTipo",
								'RESUMEN_BOLSA' AS "TipoRegistro",
							
								MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
								MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
								MAX("DBAG"."FechaCorte") AS "FechaCorte",
								MAX("DBAG"."FechaSistema") AS "FechaSistema",
								MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
								MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
								MAX("DBAG"."IdComprobante") AS "IdComprobante",
								MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
							
								NULL AS "NombrePortafolio",
								NULL AS "NombreEmpleadoEjecutivoPortafolio",
								NULL AS "NemotecnicoInstrumento",
								NULL AS "CodigoCondicionLiquidacion",
								NULL AS "Precio",
								NULL AS "Cantidad",
								NULL AS "IdFormaCustodia",
							
								MAX("DBAG"."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
								"DBAG"."IdTipoComprobante",
								"DBAG"."AccionTipoOperacion",
								"DBAG"."IdMonedaLiquidacion",
								"DBAG"."IdBolsa",
							
								SUM("DBAG"."Compra") AS "Compra",
								SUM("DBAG"."Venta") AS "Venta",
							
								SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
								SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
								SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
								SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
								SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
							
							FROM (
								SELECT
									"IdComprobante",
									MAX("NombreEmpresa") AS "NombreEmpresa",
									MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
									MAX("FechaCorte") AS "FechaCorte",
									MAX("FechaSistema") AS "FechaSistema",
									MAX("HoraImpresion") AS "HoraImpresion",
									MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
									MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
									MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
									"IdTipoComprobante",
									"AccionTipoOperacion",
									"IdMonedaLiquidacion",
									"IdBolsa",
									SUM("Compra") AS "Compra",
									SUM("Venta") AS "Venta"
								FROM #DetalleBase
								GROUP BY
									"IdComprobante",
									"IdTipoComprobante",
									"AccionTipoOperacion",
									"IdMonedaLiquidacion",
									"IdBolsa"
							) AS "DBAG"
							
							LEFT JOIN (
								SELECT
									"IdComprobante",
									"TotalComisCmbte",
									"TotalDereCmbte",
									"TotalGastoCmbte",
									"TotalImptoCmbte",
									"MontoLiquidacionFinalFirmado"
								FROM #ComprobantesUnicos
							) AS "CUAG"
								ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
							
							GROUP BY
								"DBAG"."IdMonedaLiquidacion",
								"DBAG"."IdTipoComprobante",
								"DBAG"."IdBolsa",
								"DBAG"."AccionTipoOperacion"
						) AS R
						CROSS JOIN (
						    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
						) AS Logo
						
						UNION ALL
						
						-- RESUMEN_GENERALBOLSA
						SELECT 
						    R.*, Logo."LogoCorredora"
						FROM (
						    SELECT
								p_AgruparPorBolsa AS "p_AgruparPorBolsa",
								7 AS "OrdenTipo",
								'RESUMEN_GENERALBOLSA' AS "TipoRegistro",
							
								MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
								MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
								MAX("DBAG"."FechaCorte") AS "FechaCorte",
								MAX("DBAG"."FechaSistema") AS "FechaSistema",
								MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
								MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
								MAX("DBAG"."IdComprobante") AS "IdComprobante",
								MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
							
								NULL AS "NombrePortafolio",
								NULL AS "NombreEmpleadoEjecutivoPortafolio",
								NULL AS "NemotecnicoInstrumento",
								NULL AS "CodigoCondicionLiquidacion",
								NULL AS "Precio",
								NULL AS "Cantidad",
								NULL AS "IdFormaCustodia",
							
								MAX("DBAG"."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
								NULL AS "IdTipoComprobante",
								NULL AS "AccionTipoOperacion",
								"DBAG"."IdMonedaLiquidacion",
								"DBAG"."IdBolsa",
							
								SUM("DBAG"."Compra") AS "Compra",
								SUM("DBAG"."Venta") AS "Venta",
							
								SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
								SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
								SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
								SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
								SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
							
							FROM (
								SELECT
									"IdComprobante",
									MAX("NombreEmpresa") AS "NombreEmpresa",
									MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
									MAX("FechaCorte") AS "FechaCorte",
									MAX("FechaSistema") AS "FechaSistema",
									MAX("HoraImpresion") AS "HoraImpresion",
									MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
									MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
									MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
									"IdMonedaLiquidacion",
									"IdBolsa",
							
									SUM(CASE 
										WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1 
										ELSE "Compra" 
									END) AS "Compra",
							
									SUM(CASE 
										WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1
										ELSE "Venta" 
									END) AS "Venta"
							
								FROM #DetalleBase
								GROUP BY 
									"IdComprobante",
									"IdMonedaLiquidacion",
									"IdBolsa"
							) AS "DBAG"
							
							LEFT JOIN (
							    SELECT
							        "IdComprobante",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalComisCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalComisCmbte" * -1
							            ELSE "TotalComisCmbte"
							        END) AS "TotalComisCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalDereCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalDereCmbte" * -1
							            ELSE "TotalDereCmbte"
							        END) AS "TotalDereCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalGastoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalGastoCmbte" * -1
							            ELSE "TotalGastoCmbte"
							        END) AS "TotalGastoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
							            ELSE "TotalImptoCmbte"
							        END) AS "TotalImptoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1
							            ELSE "MontoLiquidacionFinalFirmado"
							        END) AS "MontoLiquidacionFinalFirmado"
							
							    FROM #ComprobantesUnicos
								GROUP BY "IdComprobante"
							) AS "CUAG"
								ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
							
							GROUP BY
								"DBAG"."IdMonedaLiquidacion",
								"DBAG"."IdBolsa"
						) AS R
						CROSS JOIN (
						    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
						) AS Logo;
			    ELSEIF p_TipoBloque = 'RESUMEN_MONEDA' THEN
					-- RESUMEN_MONEDA
					SELECT 
					    R.*, Logo."LogoCorredora"
					FROM (
					    SELECT
					        p_AgruparPorBolsa AS "p_AgruparPorBolsa",
					        4 AS "OrdenTipo",
					        'RESUMEN_MONEDA' AS "TipoRegistro",
					
					        MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
					        MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					        MAX("DBAG"."FechaCorte") AS "FechaCorte",
					        MAX("DBAG"."FechaSistema") AS "FechaSistema",
					        MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
					        MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
					        MAX("DBAG"."IdComprobante") AS "IdComprobante",
					        MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
					
					        NULL AS "NombrePortafolio",
					        NULL AS "NombreEmpleadoEjecutivoPortafolio",
					        NULL AS "NemotecnicoInstrumento",
					        NULL AS "CodigoCondicionLiquidacion",
					        NULL AS "Precio",
					        NULL AS "Cantidad",
					        NULL AS "IdFormaCustodia",
					
					        MAX("DBAG"."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
					        "DBAG"."IdTipoComprobante",
					        "DBAG"."AccionTipoOperacion",
					        "DBAG"."IdMonedaLiquidacion",
					        MAX("DBAG"."IdBolsa") AS "IdBolsa",
					
					        SUM("DBAG"."Compra") AS "Compra",
					        SUM("DBAG"."Venta") AS "Venta",
					
					        SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
					        SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
					        SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
					        SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
					        SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
					
					    FROM (
					        SELECT
					            "IdComprobante",
					            MAX("NombreEmpresa") AS "NombreEmpresa",
					            MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					            MAX("FechaCorte") AS "FechaCorte",
					            MAX("FechaSistema") AS "FechaSistema",
					            MAX("HoraImpresion") AS "HoraImpresion",
					            MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
					            MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
					            MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
					            "IdTipoComprobante",
					            "AccionTipoOperacion",
					            "IdMonedaLiquidacion",
					            MAX("IdBolsa") AS "IdBolsa",
					
					            SUM("Compra") AS "Compra",
					            SUM("Venta") AS "Venta"
					        FROM #DetalleBase
					        GROUP BY 
					            "IdComprobante",
					            "IdTipoComprobante",
					            "AccionTipoOperacion",
					            "IdMonedaLiquidacion"
					    ) AS "DBAG"
					
					    LEFT JOIN (
					        SELECT
					            "IdComprobante",
					            SUM("TotalComisCmbte") AS "TotalComisCmbte",
					            SUM("TotalDereCmbte") AS "TotalDereCmbte",
					            SUM("TotalGastoCmbte") AS "TotalGastoCmbte",
					            SUM("TotalImptoCmbte") AS "TotalImptoCmbte",
					            SUM("MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinalFirmado"
					        FROM #ComprobantesUnicos
					        GROUP BY "IdComprobante"
					    ) AS "CUAG"
					    ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
					
					    GROUP BY 
					        "DBAG"."IdTipoComprobante",
					        "DBAG"."AccionTipoOperacion",
					        "DBAG"."IdMonedaLiquidacion"
					) AS R
					CROSS JOIN (
					    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
					) AS Logo
					
					UNION ALL
					
					-- RESUMEN_GENERAL
					SELECT 
					    R.*, Logo."LogoCorredora"
					FROM (
					    SELECT
						    p_AgruparPorBolsa AS "p_AgruparPorBolsa",
						    5 AS "OrdenTipo",
						    'RESUMEN_GENERAL' AS "TipoRegistro",
						
						    MAX(DB."NombreEmpresa") AS "NombreEmpresa",
						    MAX(DB."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						    MAX(DB."FechaCorte") AS "FechaCorte",
						    MAX(DB."FechaSistema") AS "FechaSistema",
						    MAX(DB."HoraImpresion") AS "HoraImpresion",
						    MAX(DB."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						    MAX(DB."IdComprobante") AS "IdComprobante",
						    MAX(DB."IdTipoInstrumento") AS "IdTipoInstrumento",
						
						    NULL AS "NombrePortafolio",
						    NULL AS "NombreEmpleadoEjecutivoPortafolio",
						    NULL AS "NemotecnicoInstrumento",
						    NULL AS "CodigoCondicionLiquidacion",
						    NULL AS "Precio",
						    NULL AS "Cantidad",
						    NULL AS "IdFormaCustodia",
						    NULL AS "IdClasificacionTipoInstrumento",
						    NULL AS "IdTipoComprobante",
						    NULL AS "AccionTipoOperacion",
						
						    DB."IdMonedaLiquidacion",
						    MAX(DB."IdBolsa") AS "IdBolsa",
						
						    SUM(DB."Compra") AS "Compra",
						    SUM(DB."Venta") AS "Venta",
						
						    SUM(CU."TotalComisCmbte") AS "TotalComisCmbte",
						    SUM(CU."TotalDereCmbte") AS "TotalDereCmbte",
						    SUM(CU."TotalGastoCmbte") AS "TotalGastoCmbte",
						    SUM(CU."TotalImptoCmbte") AS "TotalImptoCmbte",
						    SUM(CU."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
						
						FROM (
						    SELECT
						        "IdComprobante",
						        MAX("NombreEmpresa") AS "NombreEmpresa",
						        MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						        MAX("FechaCorte") AS "FechaCorte",
						        MAX("FechaSistema") AS "FechaSistema",
						        MAX("HoraImpresion") AS "HoraImpresion",
						        MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						        MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
						        MAX("IdBolsa") AS "IdBolsa",
						        "IdMonedaLiquidacion",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1
						            ELSE "Compra"
						        END) AS "Compra",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1
						            ELSE "Venta"
						        END) AS "Venta"
						
						    FROM #DetalleBase
						    GROUP BY "IdComprobante", "IdMonedaLiquidacion"
						) AS DB
						
						LEFT JOIN (
						    SELECT
						        "IdComprobante",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalComisCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalComisCmbte" * -1
						            ELSE "TotalComisCmbte"
						        END) AS "TotalComisCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalDereCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalDereCmbte" * -1
						            ELSE "TotalDereCmbte"
						        END) AS "TotalDereCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalGastoCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalGastoCmbte" * -1
						            ELSE "TotalGastoCmbte"
						        END) AS "TotalGastoCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
						            ELSE "TotalImptoCmbte"
						        END) AS "TotalImptoCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1
						            ELSE "MontoLiquidacionFinalFirmado"
						        END) AS "MontoLiquidacionFinalFirmado"
						
						    FROM #ComprobantesUnicos
						    GROUP BY "IdComprobante"
						) AS CU
						ON CU."IdComprobante" = DB."IdComprobante"
						
						GROUP BY DB."IdMonedaLiquidacion"
					) AS R
					CROSS JOIN (
					    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
					) AS Logo;
			    END IF;
		ELSE
			-- CASO: SUBREPORTES NO AGRUPAN POR BOLSA
				IF p_TipoBloque = 'RESUMEN_MERCADO' AND P_AGRUPARPORBOLSA = 'N' THEN
					TRUNCATE TABLE #ResumenMercadoSinBolsa;
	
					INSERT INTO #ResumenMercadoSinBolsa
						SELECT
						  p_AgruparPorBolsa AS "p_AgruparPorBolsa",
						  3 AS "OrdenTipo",
						  'RESUMEN_MERCADO' AS "TipoRegistro",
						
						  MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
						  MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						  MAX("DBAG"."FechaCorte") AS "FechaCorte",
						  MAX("DBAG"."FechaSistema") AS "FechaSistema",
						  MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
						  MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						  MAX("DBAG"."IdComprobante") AS "IdComprobante",
						  MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
						
						  NULL AS "NombrePortafolio",
						  NULL AS "NombreEmpleadoEjecutivoPortafolio",
						  NULL AS "NemotecnicoInstrumento",
						  NULL AS "CodigoCondicionLiquidacion",
						  NULL AS "Precio",
						  NULL AS "Cantidad",
						  NULL AS "IdFormaCustodia",
						
						  "DBAG"."IdClasificacionTipoInstrumento",
						  "DBAG"."IdTipoComprobante",
						  "DBAG"."AccionTipoOperacion",
						  "DBAG"."IdMonedaLiquidacion",
						  MAX("DBAG"."IdBolsa") AS "IdBolsa",
						
						  SUM("DBAG"."Compra") AS "Compra",
						  SUM("DBAG"."Venta") AS "Venta",
						
						  SUM("CU"."TotalComisCmbte") AS "TotalComisCmbte",
						  SUM("CU"."TotalDereCmbte") AS "TotalDereCmbte",
						  SUM("CU"."TotalGastoCmbte") AS "TotalGastoCmbte",
						  SUM("CU"."TotalImptoCmbte") AS "TotalImptoCmbte",
						  SUM("CU"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal",
						
						  (
						    SELECT TOP 1 "LogoCorredora"
						    FROM #DetalleBase
						    WHERE "LogoCorredora" IS NOT NULL
						  ) AS "LogoCorredora"
						
						FROM (
						  SELECT
						    "IdComprobante",
						    MAX("NombreEmpresa") AS "NombreEmpresa",
						    MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						    MAX("FechaCorte") AS "FechaCorte",
						    MAX("FechaSistema") AS "FechaSistema",
						    MAX("HoraImpresion") AS "HoraImpresion",
						    MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						    MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
						    MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
						    MAX("IdTipoComprobante") AS "IdTipoComprobante",
						    MAX("AccionTipoOperacion") AS "AccionTipoOperacion",
						    MAX("IdMonedaLiquidacion") AS "IdMonedaLiquidacion",
						    MAX("IdBolsa") AS "IdBolsa",
						    SUM("Compra") AS "Compra",
						    SUM("Venta") AS "Venta"
						  FROM #DetalleBase
						  GROUP BY "IdComprobante"
						) AS "DBAG"
						LEFT JOIN #ComprobantesUnicos AS "CU"
						  ON "CU"."IdComprobante" = "DBAG"."IdComprobante"
						
						GROUP BY
						  "DBAG"."IdMonedaLiquidacion",
						  "DBAG"."IdClasificacionTipoInstrumento",
						  "DBAG"."IdTipoComprobante",
						  "DBAG"."AccionTipoOperacion";
				
					SELECT * FROM #ResumenMercadoSinBolsa;
				ELSEIF p_TipoBloque = 'RESUMEN_BOLSA' THEN
					 SELECT 
				        CAST(NULL AS NVARCHAR(5)) AS "p_AgruparPorBolsa",
				        CAST(4 AS INT) AS "OrdenTipo",
				        CAST('RESUMEN_BOLSA' AS NVARCHAR(50)) AS "TipoRegistro",
				        CAST(NULL AS NVARCHAR(200)) AS "NombreEmpresa",
				        CAST(NULL AS NVARCHAR(50)) AS "DocumentoIdentidadEmisor",
				        CAST(NULL AS DATE) AS "FechaCorte",
				        CAST(NULL AS NVARCHAR(50)) AS "FechaSistema",
				        CAST(NULL AS NVARCHAR(50)) AS "HoraImpresion",
				        CAST(NULL AS NVARCHAR(50)) AS "DocumentoIdentidadReceptor",
				        CAST(NULL AS NVARCHAR(50)) AS "IdComprobante",
				        CAST(NULL AS NVARCHAR(50)) AS "IdTipoInstrumento",
				        CAST(NULL AS NVARCHAR(200)) AS "NombrePortafolio",
				        CAST(NULL AS NVARCHAR(200)) AS "NombreEmpleadoEjecutivoPortafolio",
				        CAST(NULL AS NVARCHAR(50)) AS "NemotecnicoInstrumento",
				        CAST(NULL AS NVARCHAR(50)) AS "CodigoCondicionLiquidacion",
				        CAST(NULL AS DECIMAL(38, 8)) AS "Precio",
				        CAST(NULL AS DECIMAL(38, 8)) AS "Cantidad",
				        CAST(NULL AS NVARCHAR(50)) AS "IdFormaCustodia",
				        CAST(NULL AS NVARCHAR(50)) AS "IdClasificacionTipoInstrumento",
				        CAST(NULL AS NVARCHAR(50)) AS "IdTipoComprobante",
				        CAST(NULL AS NVARCHAR(50)) AS "AccionTipoOperacion",
				        CAST(NULL AS NVARCHAR(50)) AS "IdMonedaLiquidacion",
				        CAST(NULL AS NVARCHAR(50)) AS "IdBolsa",
				        CAST(NULL AS DECIMAL(38, 8)) AS "Compra",
				        CAST(NULL AS DECIMAL(38, 8)) AS "Venta",
				        CAST(NULL AS DECIMAL(38, 8)) AS "TotalComisCmbte",
				        CAST(NULL AS DECIMAL(38, 8)) AS "TotalDereCmbte",
				        CAST(NULL AS DECIMAL(38, 8)) AS "TotalGastoCmbte",
				        CAST(NULL AS DECIMAL(38, 8)) AS "TotalImptoCmbte",
				        CAST(NULL AS DECIMAL(38, 8)) AS "MontoLiquidacionFinal",
				        (
							SELECT TOP 1 "LogoCorredora"
							FROM #DetalleBase
							WHERE "LogoCorredora" IS NOT NULL
						) AS "LogoCorredora"
				        FROM DUMMY;
			    ELSEIF p_TipoBloque = 'RESUMEN_MONEDA' THEN
					-- RESUMEN_MONEDA
					SELECT 
					    R.*, Logo."LogoCorredora"
					FROM (
					    SELECT
					        p_AgruparPorBolsa AS "p_AgruparPorBolsa",
					        4 AS "OrdenTipo",
					        'RESUMEN_MONEDA' AS "TipoRegistro",
					
					        MAX("DBAG"."NombreEmpresa") AS "NombreEmpresa",
					        MAX("DBAG"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					        MAX("DBAG"."FechaCorte") AS "FechaCorte",
					        MAX("DBAG"."FechaSistema") AS "FechaSistema",
					        MAX("DBAG"."HoraImpresion") AS "HoraImpresion",
					        MAX("DBAG"."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
					        MAX("DBAG"."IdComprobante") AS "IdComprobante",
					        MAX("DBAG"."IdTipoInstrumento") AS "IdTipoInstrumento",
					
					        NULL AS "NombrePortafolio",
					        NULL AS "NombreEmpleadoEjecutivoPortafolio",
					        NULL AS "NemotecnicoInstrumento",
					        NULL AS "CodigoCondicionLiquidacion",
					        NULL AS "Precio",
					        NULL AS "Cantidad",
					        NULL AS "IdFormaCustodia",
					
					        MAX("DBAG"."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
					        "DBAG"."IdTipoComprobante",
					        "DBAG"."AccionTipoOperacion",
					        "DBAG"."IdMonedaLiquidacion",
					        MAX("DBAG"."IdBolsa") AS "IdBolsa",
					
					        SUM("DBAG"."Compra") AS "Compra",
					        SUM("DBAG"."Venta") AS "Venta",
					
					        SUM("CUAG"."TotalComisCmbte") AS "TotalComisCmbte",
					        SUM("CUAG"."TotalDereCmbte") AS "TotalDereCmbte",
					        SUM("CUAG"."TotalGastoCmbte") AS "TotalGastoCmbte",
					        SUM("CUAG"."TotalImptoCmbte") AS "TotalImptoCmbte",
					        SUM("CUAG"."MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinal"
					
					    FROM (
					        SELECT
					            "IdComprobante",
					            MAX("NombreEmpresa") AS "NombreEmpresa",
					            MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
					            MAX("FechaCorte") AS "FechaCorte",
					            MAX("FechaSistema") AS "FechaSistema",
					            MAX("HoraImpresion") AS "HoraImpresion",
					            MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
					            MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
					            MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
					            "IdTipoComprobante",
					            "AccionTipoOperacion",
					            "IdMonedaLiquidacion",
					            MAX("IdBolsa") AS "IdBolsa",
					
					            SUM("Compra") AS "Compra",
					            SUM("Venta") AS "Venta"
					        FROM #DetalleBase
					        GROUP BY 
					            "IdComprobante",
					            "IdTipoComprobante",
					            "AccionTipoOperacion",
					            "IdMonedaLiquidacion"
					    ) AS "DBAG"
					
					    LEFT JOIN (
					        SELECT
					            "IdComprobante",
					            SUM("TotalComisCmbte") AS "TotalComisCmbte",
					            SUM("TotalDereCmbte") AS "TotalDereCmbte",
					            SUM("TotalGastoCmbte") AS "TotalGastoCmbte",
					            SUM("TotalImptoCmbte") AS "TotalImptoCmbte",
					            SUM("MontoLiquidacionFinalFirmado") AS "MontoLiquidacionFinalFirmado"
					        FROM #ComprobantesUnicos
					        GROUP BY "IdComprobante"
					    ) AS "CUAG"
					    ON "CUAG"."IdComprobante" = "DBAG"."IdComprobante"
					
					    GROUP BY 
					        "DBAG"."IdTipoComprobante",
					        "DBAG"."AccionTipoOperacion",
					        "DBAG"."IdMonedaLiquidacion"
					) AS R
					CROSS JOIN (
					    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
					) AS Logo
					
					UNION ALL
					
					-- RESUMEN_GENERAL
					SELECT 
					    R.*, Logo."LogoCorredora"
					FROM (
					    SELECT
						    p_AgruparPorBolsa AS "p_AgruparPorBolsa",
						    5 AS "OrdenTipo",
						    'RESUMEN_GENERAL' AS "TipoRegistro",
						
						    MAX(DB."NombreEmpresa") AS "NombreEmpresa",
						    MAX(DB."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						    MAX(DB."FechaCorte") AS "FechaCorte",
						    MAX(DB."FechaSistema") AS "FechaSistema",
						    MAX(DB."HoraImpresion") AS "HoraImpresion",
						    MAX(DB."DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						    MAX(DB."IdComprobante") AS "IdComprobante",
						    MAX(DB."IdTipoInstrumento") AS "IdTipoInstrumento",
						
						    NULL AS "NombrePortafolio",
						    NULL AS "NombreEmpleadoEjecutivoPortafolio",
						    NULL AS "NemotecnicoInstrumento",
						    NULL AS "CodigoCondicionLiquidacion",
						    NULL AS "Precio",
						    NULL AS "Cantidad",
						    NULL AS "IdFormaCustodia",
						    NULL AS "IdClasificacionTipoInstrumento",
						    NULL AS "IdTipoComprobante",
						    NULL AS "AccionTipoOperacion",
						
						    DB."IdMonedaLiquidacion",
						    MAX(DB."IdBolsa") AS "IdBolsa",
						
						    SUM(DB."Compra") AS "Compra",
						    SUM(DB."Venta") AS "Venta",
						    SUM(CU."TotalComisCmbte") AS "TotalComisCmbte",
						    SUM(CU."TotalDereCmbte") AS "TotalDereCmbte",
						    SUM(CU."TotalGastoCmbte") AS "TotalGastoCmbte",
						    SUM(CU."TotalImptoCmbte") AS "TotalImptoCmbte",
						    SUM(CU."MontoLiquidacionFinal") AS "MontoLiquidacionFinal"
						
						FROM (
						    SELECT
						        "IdComprobante",
						        MAX("NombreEmpresa") AS "NombreEmpresa",
						        MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
						        MAX("FechaCorte") AS "FechaCorte",
						        MAX("FechaSistema") AS "FechaSistema",
						        MAX("HoraImpresion") AS "HoraImpresion",
						        MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
						        MAX("IdTipoInstrumento") AS "IdTipoInstrumento",
						        MAX("IdBolsa") AS "IdBolsa",
						        "IdMonedaLiquidacion",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1
						            ELSE "Compra"
						        END) AS "Compra",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1
						            ELSE "Venta"
						        END) AS "Venta"
						
						    FROM #DetalleBase
						    GROUP BY "IdComprobante", "IdMonedaLiquidacion"
						) AS DB
						
						LEFT JOIN (
						    SELECT
						        "IdComprobante",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalComisCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalComisCmbte" * -1
						            ELSE "TotalComisCmbte"
						        END) AS "TotalComisCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalDereCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalDereCmbte" * -1
						            ELSE "TotalDereCmbte"
						        END) AS "TotalDereCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalGastoCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalGastoCmbte" * -1
						            ELSE "TotalGastoCmbte"
						        END) AS "TotalGastoCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
						            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
						            ELSE "TotalImptoCmbte"
						        END) AS "TotalImptoCmbte",
						
						        SUM(CASE
						            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1
						            ELSE "MontoLiquidacionFinalFirmado"
						        END) AS "MontoLiquidacionFinal"
						
						    FROM #ComprobantesUnicos
						    GROUP BY "IdComprobante"
						) AS CU
						ON CU."IdComprobante" = DB."IdComprobante"
						
						GROUP BY DB."IdMonedaLiquidacion"
					) AS R
					CROSS JOIN (
					    SELECT TOP 1 "LogoCorredora"
					    FROM #DetalleBase
					    WHERE "LogoCorredora" IS NOT NULL
					) AS Logo;
			    END IF;
			 END IF;
	ELSEIF p_Modo = 'xls' THEN
		IF p_TipoBloque = 'CUADRO_PRODUCTO' THEN
						-- CUADRO_PRODUCTO_RESUMEN
						SELECT 
						    R.*, Logo."LogoCorredora"
						FROM (
						    SELECT
							    p_AgruparPorBolsa AS "p_AgruparPorBolsa",
							    10 AS "OrdenTipo",
							    'CUADRO_PRODUCTO_RESUMEN' AS "TipoRegistro",
							
							    MAX(DB."NombreEmpresa") AS "NombreEmpresa",
							    MAX(DB."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							    MAX(DB."FechaCorte") AS "FechaCorte",
							    MAX(DB."FechaSistema") AS "FechaSistema",
							    MAX(DB."HoraImpresion") AS "HoraImpresion",
							
							    DB."IdTipoInstrumento",
							    DB."IdMonedaLiquidacion",
							    MAX(DB."NombreTipoInstrumento") AS "NombreTipoInstrumento",
							
							    -- Campos omitidos
							    NULL AS "DocumentoIdentidadReceptor",
							    NULL AS "IdComprobante",
							    NULL AS "NombrePortafolio",
							    NULL AS "NombreEmpleadoEjecutivoPortafolio",
							    NULL AS "NemotecnicoInstrumento",
							    NULL AS "CodigoCondicionLiquidacion",
							    NULL AS "Precio",
							    NULL AS "Cantidad",
							    NULL AS "IdFormaCustodia",
							    NULL AS "IdClasificacionTipoInstrumento",
							    NULL AS "IdTipoComprobante",
							    NULL AS "AccionTipoOperacion",
							    NULL AS "IdBolsa",
							
							    SUM(DB."Compra") AS "Compra",
							    SUM(DB."Venta") AS "Venta",
							    SUM(CU."TotalComisCmbte") AS "TotalComisCmbte",
							    SUM(CU."TotalDereCmbte") AS "TotalDereCmbte",
							    SUM(CU."TotalGastoCmbte") AS "TotalGastoCmbte",
							    SUM(CU."TotalImptoCmbte") AS "TotalImptoCmbte",
							    SUM(CU."MontoLiquidacionFinal") AS "MontoLiquidacionFinal"
							
							FROM (
							    SELECT
							        "IdComprobante",
							        "IdTipoInstrumento",
							        "IdMonedaLiquidacion",
							
							        MAX("NombreEmpresa") AS "NombreEmpresa",
							        MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							        MAX("FechaCorte") AS "FechaCorte",
							        MAX("FechaSistema") AS "FechaSistema",
							        MAX("HoraImpresion") AS "HoraImpresion",
							        MAX("NombreTipoInstrumento") AS "NombreTipoInstrumento",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1
							            ELSE "Compra"
							        END) AS "Compra",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1
							            ELSE "Venta"
							        END) AS "Venta"
							    FROM #DetalleBase
							    GROUP BY "IdComprobante", "IdTipoInstrumento", "IdMonedaLiquidacion", "IdTipoComprobante"
							) AS DB
							
							LEFT JOIN (
							    SELECT
							        "IdComprobante",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalComisCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalComisCmbte" * -1
							            ELSE "TotalComisCmbte"
							        END) AS "TotalComisCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalDereCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalDereCmbte" * -1
							            ELSE "TotalDereCmbte"
							        END) AS "TotalDereCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalGastoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalGastoCmbte" * -1
							            ELSE "TotalGastoCmbte"
							        END) AS "TotalGastoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
							            ELSE "TotalImptoCmbte"
							        END) AS "TotalImptoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1
							            ELSE "MontoLiquidacionFinalFirmado"
							        END) AS "MontoLiquidacionFinal"
							
							    FROM #ComprobantesUnicos
							    GROUP BY "IdComprobante"
							) AS CU
							ON CU."IdComprobante" = DB."IdComprobante"
							
							GROUP BY DB."IdTipoInstrumento", DB."IdMonedaLiquidacion"
						) AS R
						CROSS JOIN (
						    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
						) AS Logo
						
						UNION ALL
						
						-- CUADRO_PRODUCTO_TOTAL
						SELECT 
						    R.*, Logo."LogoCorredora"
						FROM (
						    SELECT
							    p_AgruparPorBolsa AS "p_AgruparPorBolsa",
							    10 AS "OrdenTipo",
							    'CUADRO_PRODUCTO_TOTAL' AS "TipoRegistro",
							
							    MAX("DB"."NombreEmpresa") AS "NombreEmpresa",
							    MAX("DB"."DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							    MAX("DB"."FechaCorte") AS "FechaCorte",
							    MAX("DB"."FechaSistema") AS "FechaSistema",
							    MAX("DB"."HoraImpresion") AS "HoraImpresion",
							
							    NULL AS "IdTipoInstrumento",
							    "DB"."IdMonedaLiquidacion",
							    MAX("DB"."NombreTipoInstrumento") AS "NombreTipoInstrumento",
							
							    NULL AS "DocumentoIdentidadReceptor",
							    NULL AS "IdComprobante",
							    NULL AS "NombrePortafolio",
							    NULL AS "NombreEmpleadoEjecutivoPortafolio",
							    NULL AS "NemotecnicoInstrumento",
							    NULL AS "CodigoCondicionLiquidacion",
							    NULL AS "Precio",
							    NULL AS "Cantidad",
							    NULL AS "IdFormaCustodia",
							    NULL AS "IdClasificacionTipoInstrumento",
							    NULL AS "IdTipoComprobante",
							    NULL AS "AccionTipoOperacion",
							    NULL AS "IdBolsa",
							
							    SUM("DB"."Compra") AS "Compra",
							    SUM("DB"."Venta") AS "Venta",
							    SUM("CU"."TotalComisCmbte") AS "TotalComisCmbte",
							    SUM("CU"."TotalDereCmbte") AS "TotalDereCmbte",
							    SUM("CU"."TotalGastoCmbte") AS "TotalGastoCmbte",
							    SUM("CU"."TotalImptoCmbte") AS "TotalImptoCmbte",
							    SUM("CU"."MontoLiquidacionFinal") AS "MontoLiquidacionFinal"
							
							FROM (
							    SELECT
							        "IdComprobante",
							        "IdMonedaLiquidacion",
							        MAX("NombreEmpresa") AS "NombreEmpresa",
							        MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							        MAX("FechaCorte") AS "FechaCorte",
							        MAX("FechaSistema") AS "FechaSistema",
							        MAX("HoraImpresion") AS "HoraImpresion",
							        MAX("NombreTipoInstrumento") AS "NombreTipoInstrumento",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Compra" * -1
							            ELSE "Compra"
							        END) AS "Compra",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "Venta" * -1
							            ELSE "Venta"
							        END) AS "Venta"
							
							    FROM #DetalleBase
							    GROUP BY "IdComprobante", "IdMonedaLiquidacion", "IdTipoComprobante"
							) AS "DB"
							
							LEFT JOIN (
							    SELECT
							        "IdComprobante",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalComisCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalComisCmbte" * -1
							            ELSE "TotalComisCmbte"
							        END) AS "TotalComisCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalDereCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalDereCmbte" * -1
							            ELSE "TotalDereCmbte"
							        END) AS "TotalDereCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalGastoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalGastoCmbte" * -1
							            ELSE "TotalGastoCmbte"
							        END) AS "TotalGastoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" = 'COMPRA' THEN "TotalImptoCmbte" * -1
							            WHEN "IdTipoComprobante" NOT IN ('NCR', 'NCE', 'NEO','NCO','NEE') AND "AccionTipoOperacion" IN ('VENTA', 'VENTA CORTA') THEN "TotalImptoCmbte" * -1
							            ELSE "TotalImptoCmbte"
							        END) AS "TotalImptoCmbte",
							
							        SUM(CASE
							            WHEN "IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE') THEN "MontoLiquidacionFinalFirmado" * -1
							            ELSE "MontoLiquidacionFinalFirmado"
							        END) AS "MontoLiquidacionFinal"
							
							    FROM #ComprobantesUnicos
							    GROUP BY "IdComprobante"
							) AS "CU"
							ON "CU"."IdComprobante" = "DB"."IdComprobante"
							
							GROUP BY "DB"."IdMonedaLiquidacion"
						) AS R
						CROSS JOIN (
						    SELECT TOP 1 "LogoCorredora" FROM #DetalleBase WHERE "LogoCorredora" IS NOT NULL
						) AS Logo;
			ELSEIF p_TipoBloque = 'RESUMEN_DIARIO' THEN
				-- RESUMEN_DIARIO
				TRUNCATE TABLE #ResumenDiario;
						INSERT INTO #ResumenDiario
							SELECT 
							    R.*
							FROM (
							    SELECT
							        DB."IdMonedaLiquidacion",
							        DB."IdClasificacionTipoInstrumento",
							        DB."CodigoCondicionLiquidacion",
							        DB."IdBolsa",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "CompraCuentaPropia",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "CompraTercero",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "VentaCuentaPropia",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "VentaTercero"
							
							    FROM (
							        SELECT 
							            "IdComprobante",
							            MAX("IdMonedaLiquidacion") AS "IdMonedaLiquidacion",
							            MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
							            MAX("CodigoCondicionLiquidacion") AS "CodigoCondicionLiquidacion",
							            MAX("IdBolsa") AS "IdBolsa",
							            MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							            MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
							            MAX("AccionTipoOperacion") AS "AccionTipoOperacion",
							            MAX("IdTipoComprobante") AS "IdTipoComprobante",
							            ABS(MAX("MontoLiquidacionFinal")) AS "MontoLiquidacionFinal"
							        FROM #DetalleBase
							        GROUP BY "IdComprobante"
							    ) AS DB
							
							    GROUP BY 
							        DB."IdClasificacionTipoInstrumento",
							        DB."CodigoCondicionLiquidacion",
							        DB."IdMonedaLiquidacion",
							        DB."IdBolsa"
							) AS R;
							SELECT * FROM #ResumenDiario;
			ELSEIF p_TipoBloque = 'RESUMEN_CONSOLIDADO' THEN
				-- RESUMEN_DIARIO
				TRUNCATE TABLE #ResumenDiario;
						INSERT INTO #ResumenDiario
							SELECT 
							    R.*
							FROM (
							    SELECT
							        DB."IdMonedaLiquidacion",
							        DB."IdClasificacionTipoInstrumento",
							        DB."CodigoCondicionLiquidacion",
							        DB."IdBolsa",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "CompraCuentaPropia",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'COMPRA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "CompraTercero",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" = DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "VentaCuentaPropia",
							
							        SUM(CASE 
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA' 
							                 AND DB."IdTipoComprobante" IN ('NCR', 'NCE', 'NEO','NCO','NEE')
							            THEN DB."MontoLiquidacionFinal" * -1
							            WHEN DB."DocumentoIdentidadEmisor" <> DB."DocumentoIdentidadReceptor" 
							                 AND DB."AccionTipoOperacion" = 'VENTA'
							            THEN DB."MontoLiquidacionFinal"
							            ELSE 0 
							        END) AS "VentaTercero"
							
							    FROM (
							        SELECT 
							            "IdComprobante",
							            MAX("IdMonedaLiquidacion") AS "IdMonedaLiquidacion",
							            MAX("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento",
							            MAX("CodigoCondicionLiquidacion") AS "CodigoCondicionLiquidacion",
							            MAX("IdBolsa") AS "IdBolsa",
							            MAX("DocumentoIdentidadEmisor") AS "DocumentoIdentidadEmisor",
							            MAX("DocumentoIdentidadReceptor") AS "DocumentoIdentidadReceptor",
							            MAX("AccionTipoOperacion") AS "AccionTipoOperacion",
							            MAX("IdTipoComprobante") AS "IdTipoComprobante",
							            ABS(MAX("MontoLiquidacionFinal")) AS "MontoLiquidacionFinal"
							        FROM #DetalleBase
							        GROUP BY "IdComprobante"
							    ) AS DB
							
							    GROUP BY 
							        DB."IdClasificacionTipoInstrumento",
							        DB."CodigoCondicionLiquidacion",
							        DB."IdMonedaLiquidacion",
							        DB."IdBolsa"
							) AS R;
							SELECT * FROM #ResumenDiario;
					END IF;
				END IF;
		END IF;
END IF;
-- Limpieza de temporales
DROP TABLE #DetalleBase;
DROP TABLE #ComprobantesUnicos;
DROP TABLE #ResumenMercadoSinBolsa;
DROP TABLE #ResumenMercado;
DROP TABLE #ResumenDiario;



END;

GO