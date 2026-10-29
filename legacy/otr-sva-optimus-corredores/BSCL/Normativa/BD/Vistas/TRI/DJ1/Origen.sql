SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Origen" AS
SELECT
		FWD."IdEmpresa",
		CONF."PKRUTCTRAPARTE1"		AS "KRutCtraparte1",
		FWD."KIdContrato",
		FWD."KFechaContrato",
		FWD."HoraContrato",
		0							AS "KIdOpeEstruc",
		CONF."PNATURAINFO"			AS "S1_NaturaInfo",	
		CASE WHEN FWD."TipoEvento" 	IN ('ANT','VCD') THEN 'AOC' ELSE CAR."EventoReporte" END AS "S1_EventoReporte",
		CONF."PLEICTRAPARTE1"		AS "S1_LEICtraparte1",
		FWD."S1_RutCtraparte2", 
		FWD."S1_LEICtraparte2",
		FWD."S1_NomCtraparte2", 
		FWD."S1_PaisCtraparte2", 	
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PRUTENTCOCE" ELSE '' END AS "S1_RutEntidad",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PLEIENTCOCE" ELSE '' END AS "S1_LEIEntidad",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PRUTLIQCTP1" ELSE '' END AS "S1_RUTLiqCtraparte1",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PLEILIQCTP1" ELSE '' END AS "S1_LEILiqCtraparte1",
		CONF."PRUTAGECAL"			AS "S1_RutAgenteCal",
		CONF."PLEIAGECAL"			AS "S1_LEIAgenteCal",
		CONF."PNOMAGECAL"			AS "S1_NomAgenteCal",
		FWD."S1_RutCedente",
		FWD."S1_LEICedente",
		CONF."PPLATAFORMA"			AS "S1_PlataformaNego",
		CONF."PKRUTCTRAPARTE1"		AS "S1_RutIntermediario",
		CONF."PLEIINTERME"			AS "S1_LEIIntermediario",
		FWD."S2_Instrumento", 
		''							AS "S2_IdContratoAgente", 
		''							AS "S2_OpsClase", 
		''							AS "S2_OpPosicionOp", 
		FWD."S2_ModalidadLiq",
		FWD."S2_MonedaComp",
		FWD."S2_FechaInicio",
		FWD."S2_FechaFixing1",
		FWD."S2_FechaTermino",
		FWD."S2_FechaPagOpe",
		'0001-01-01'				AS "S2_FechaInicioMod",
		''							AS "S2_MonedaPrima", 
		0							AS "S2_MontoPrima", 
		CONF."POBJOPERA"			AS "S2_ObjetivoOpe",
		FWD."S2_ConvenioMarco", 
		FWD."S2_PaisContrato", 
		CONF."PGARANTIAS"			AS "S2_Garantias",
		CONF."PCLAURECOUP"			AS "S2_ClausulaRecou",
		CONF."PCLAUTERMI"			AS "S2_ClausulaTermino",
		FWD."FechaAnticipo"			AS "S2_FechaEfecTermino", 
		CONF."PCOMPCARTE"			AS "S2_CompresionCartera",
		'1'						    AS "S2_NumTotIntercam",
		FWD."S2_NumTotPagos", 
		''						    AS "S3_XR_OpInstruSub", 
		FWD."S3_MonedaComprada", 
		FWD."S3_MontoComprada", 
		FWD."S3_XR_MonedaVendida", 
		FWD."S3_XR_MontoVendida", 
		''							AS "S3_XR_TipoTasaEnt",
		''							AS "S3_XR_TipoTasaRec", 
		0							AS "S3_R_TasaFija", 
		0							AS "S3_R_Spread", 
		FWD."S3_I_TipoActivoSub", 
		FWD."S3_I_IdActivoSuby", 
		0							AS "S3_I_TasaPactada", 
		'0001-01-01'				AS "S3_XR_FechaFixing2",
		FWD."S3_XR_ConvTipoCambio", 
		FWD."S3_PrecioFuturo", 
		FWD."S3_XR_PuntosForward", 
		0							AS "S3_XR_PrecioEntrada", 
		0							AS "S3_XR_PrecioSalida", 
		FWD."CodigoPosicionDeclarante"	AS "S3_I_PosicionOpe",
		CONF."PMETVALORA"			AS "S8_MtodoValoriza",
		FWD."S8_MonedaValoriza", 
		COALESCE(FWDHIS."S8_ValorMercado_Historico", FWD."S8_ValorMercado") AS "S8_ValorMercado",
		COALESCE(FWDHIS."S8_ValorMercadoActivo_Historico", FWD."S8_ValorMercadoActivo") AS "S8_ValorMercadoActivo",
		COALESCE(FWDHIS."S8_ValorMercadoPasivo_Historico", FWD."S8_ValorMercadoPasivo") AS "S8_ValorMercadoPasivo",
		0							AS "S8_OpVolatilidad", 
		0							AS "S8_OpDelta", 
		0							AS "S8_OpGamma", 
		0							AS "S8_OpVega", 
		0							AS "S8_AjusteRiesgo", 
		0							AS "S8_AjusteBID", 
		0							AS "S8_OtrosAjustes", 
					
		0							AS "KS4_XR_NroFlu",
		''							AS "S4_XR_TipoFlujo",
		0							AS "S4_XR_FacTasa",
		0							AS "S4_XR_SpreadTasa",
		0							AS "S4_XR_TasaFija",
		'0001-01-01'				AS "S4_XR_FecFixTasa",
		'0001-01-01'				AS "S4_XR_FecIni",
		'0001-01-01'				AS "S4_XR_FecTer",
		0							AS "S4_XR_MontoNoc",
		0							AS "S4_XR_MontoInt",
		''							AS "S4_X_MonMonto",

		0							AS "KS5_CorrPago",
		''							AS "KS5_IdPago",
		'0001-01-01'				AS "S5_FecPago",
		0							AS "S5_MontoPago",
		''							AS "S5_TipoPago",
		''							AS "S5_CodMoneda",

		''							AS "S6_TipoGar",
		''							AS "S6_MonVal",
		''							AS "KS6_IdGar",
		0							AS "S6_MontoUmb",
		0							AS "S6_ValGtia",

		''							AS "S7_TipoGar",
		''							AS "KS7_IdGar",
		''							AS "S7_TipoActGar",
		0							AS "S7_PorcGar",	
		FWD."kProducto", 
		CAR."fecha"					AS "kPeriodoComercial",
		'FX'						AS "kTipoArchivo",
		'N'							AS "Rectifica",
		1							AS "Seccion"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" FWD
INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" CAR 
	ON FWD."KIdContrato" = CAR."FolioCartera" 
	AND FWD."IdCartera" = CAR."IdCartera"
CROSS JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion" CONF
LEFT JOIN 
(
	SELECT 	
		HIS."FechaRespaldo",
		HIS."FolioCartera",
		HIS."Fecha",
		HIS."S8_ValorMercado_Historico",
		HIS."S8_ValorMercadoActivo_Historico",
		HIS."S8_ValorMercadoPasivo_Historico"
	FROM (
		SELECT 	
			H."FechaRespaldo",
 			H."FolioCartera",
 			CAR."fecha" AS "Fecha",
 			TO_DECIMAL(H."INGMontoValorRazonableNeto", 15, 5) AS "S8_ValorMercado_Historico",
			TO_DECIMAL(H."INGMontoValorRazonableActivo", 15, 5) AS "S8_ValorMercadoActivo_Historico",
			TO_DECIMAL(H."INGMontoValorRazonableActivo", 15, 5) - TO_DECIMAL(H."INGMontoValorRazonableNeto", 15, 5) AS "S8_ValorMercadoPasivo_Historico",
			RANK() OVER (PARTITION BY  H."FolioCartera" ORDER BY H."IdCartera" DESC, H."FechaRespaldo" DESC) AS Rank
			FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" FWD
			INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" CAR
				ON FWD."KIdContrato" = CAR."FolioCartera"
				AND FWD."IdCartera" = CAR."IdCartera"
			INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" H
				ON CAR."FolioCartera" = H."FolioCartera"
				AND CAR."IdCartera" = H."IdCartera"
				AND CAR."EventoReporte" = 'NUE'
				AND TO_VARCHAR(H."FechaRespaldo", 'YYYY-MM') = TO_VARCHAR(CAR."fecha", 'YYYY-MM')
	) AS HIS
	WHERE HIS.Rank = 1
) FWDHIS
	ON 	CAR."FolioCartera" = FWDHIS."FolioCartera"
	AND CAR."fecha" = FWDHIS."Fecha"

UNION 

SELECT
		FWD."IdEmpresa",
		CONF."PKRUTCTRAPARTE1"		AS "KRutCtraparte1",
		FWD."KIdContrato",
		FWD."KFechaContrato",
		FWD."HoraContrato",
		0							AS "KIdOpeEstruc",
		CONF."PNATURAINFO"			AS "S1_NaturaInfo",	
		CASE 
			WHEN  CAR."EventoReporte" NOT IN ('NUE', 'MOD') AND TO_VARCHAR(FWDHIS."FechaRespaldo", 'yyyy-MM') = TO_VARCHAR(FWD."KFechaContrato", 'yyyy-MM') THEN 'NUE'
			ELSE 'MVI'
		END AS "S1_EventoReporte",
		CONF."PLEICTRAPARTE1"		AS "S1_LEICtraparte1",
		FWD."S1_RutCtraparte2", 
		FWD."S1_LEICtraparte2",
		FWD."S1_NomCtraparte2", 
		FWD."S1_PaisCtraparte2", 	
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PRUTENTCOCE" ELSE '' END AS "S1_RutEntidad",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PLEIENTCOCE" ELSE '' END AS "S1_LEIEntidad",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PRUTLIQCTP1" ELSE '' END AS "S1_RUTLiqCtraparte1",
		CASE WHEN CONF."PGARANTIAS"='CC' THEN CONF."PLEILIQCTP1" ELSE '' END AS "S1_LEILiqCtraparte1",
		CONF."PRUTAGECAL"			AS "S1_RutAgenteCal",
		CONF."PLEIAGECAL"			AS "S1_LEIAgenteCal",
		CONF."PNOMAGECAL"			AS "S1_NomAgenteCal",
		FWD."S1_RutCedente",
		FWD."S1_LEICedente",
		CONF."PPLATAFORMA"			AS "S1_PlataformaNego",
		CONF."PKRUTCTRAPARTE1"		AS "S1_RutIntermediario",
		CONF."PLEIINTERME"			AS "S1_LEIIntermediario",
		FWD."S2_Instrumento", 
		''							AS "S2_IdContratoAgente", 
		''							AS "S2_OpsClase", 
		''							AS "S2_OpPosicionOp", 
		FWD."S2_ModalidadLiq",
		FWD."S2_MonedaComp",
		FWD."S2_FechaInicio",
		FWD."S2_FechaFixing1",
		FWD."S2_FechaTermino",
		FWD."S2_FechaPagOpe",
		'0001-01-01'				AS "S2_FechaInicioMod",
		''							AS "S2_MonedaPrima", 
		0							AS "S2_MontoPrima", 
		CONF."POBJOPERA"			AS "S2_ObjetivoOpe",
		FWD."S2_ConvenioMarco", 
		FWD."S2_PaisContrato", 
		CONF."PGARANTIAS"			AS "S2_Garantias",
		CONF."PCLAURECOUP"			AS "S2_ClausulaRecou",
		CONF."PCLAUTERMI"			AS "S2_ClausulaTermino",
		FWD."FechaAnticipo"		    AS "S2_FechaEfecTermino", 
		CONF."PCOMPCARTE"			AS "S2_CompresionCartera",
		'1'						    AS "S2_NumTotIntercam",
		FWD."S2_NumTotPagos", 
		''						    AS "S3_XR_OpInstruSub", 
		FWD."S3_MonedaComprada", 
		FWD."S3_MontoComprada", 
		FWD."S3_XR_MonedaVendida", 
		FWD."S3_XR_MontoVendida", 
		''							AS "S3_XR_TipoTasaEnt",
		''							AS "S3_XR_TipoTasaRec", 
		0							AS "S3_R_TasaFija", 
		0							AS "S3_R_Spread", 
		FWD."S3_I_TipoActivoSub", 
		FWD."S3_I_IdActivoSuby", 
		0							AS "S3_I_TasaPactada", 
		'0001-01-01'				AS "S3_XR_FechaFixing2",
		FWD."S3_XR_ConvTipoCambio", 
		FWD."S3_PrecioFuturo", 
		FWD."S3_XR_PuntosForward", 
		0							AS "S3_XR_PrecioEntrada", 
		0							AS "S3_XR_PrecioSalida", 
		FWD."CodigoPosicionDeclarante"	AS "S3_I_PosicionOpe",
		CONF."PMETVALORA"			AS "S8_MtodoValoriza",
		FWD."S8_MonedaValoriza", 
		COALESCE(FWDHIS."S8_ValorMercado_Historico", FWD."S8_ValorMercado") AS "S8_ValorMercado",
		COALESCE(FWDHIS."S8_ValorMercadoActivo_Historico", FWD."S8_ValorMercadoActivo") AS "S8_ValorMercadoActivo",
		COALESCE(FWDHIS."S8_ValorMercadoPasivo_Historico", FWD."S8_ValorMercadoPasivo") AS "S8_ValorMercadoPasivo",
		0							AS "S8_OpVolatilidad", 
		0							AS "S8_OpDelta", 
		0							AS "S8_OpGamma", 
		0							AS "S8_OpVega", 
		0							AS "S8_AjusteRiesgo", 
		0							AS "S8_AjusteBID", 
		0							AS "S8_OtrosAjustes", 
					
		0							AS "KS4_XR_NroFlu",
		''							AS "S4_XR_TipoFlujo",
		0							AS "S4_XR_FacTasa",
		0							AS "S4_XR_SpreadTasa",
		0							AS "S4_XR_TasaFija",
		'0001-01-01'				AS "S4_XR_FecFixTasa",
		'0001-01-01'				AS "S4_XR_FecIni",
		'0001-01-01'				AS "S4_XR_FecTer",
		0							AS "S4_XR_MontoNoc",
		0							AS "S4_XR_MontoInt",
		''							AS "S4_X_MonMonto",

		0							AS "KS5_CorrPago",
		''							AS "KS5_IdPago",
		'0001-01-01'				AS "S5_FecPago",
		0							AS "S5_MontoPago",
		''							AS "S5_TipoPago",
		''							AS "S5_CodMoneda",

		''							AS "S6_TipoGar",
		''							AS "S6_MonVal",
		''							AS "KS6_IdGar",
		0							AS "S6_MontoUmb",
		0							AS "S6_ValGtia",

		''							AS "S7_TipoGar",
		''							AS "KS7_IdGar",
		''							AS "S7_TipoActGar",
		0							AS "S7_PorcGar",	
		FWD."kProducto", 
		FWDHIS."FechaRespaldo"		AS "kPeriodoComercial",
		'FX'						AS "kTipoArchivo",
		'N'							AS "Rectifica",
		1							AS "Seccion"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" FWD
INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" CAR 
	ON FWD."KIdContrato" = CAR."FolioCartera" 
	AND FWD."IdCartera" = CAR."IdCartera"
CROSS JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion" CONF
INNER JOIN 
(
	SELECT 	
		HIS."FechaRespaldo",
		HIS."FolioCartera",
		HIS."Fecha",
		HIS."S8_ValorMercado_Historico",
		HIS."S8_ValorMercadoActivo_Historico",
		HIS."S8_ValorMercadoPasivo_Historico"
	FROM (
		SELECT 	
				H."FechaRespaldo",
				H."FolioCartera",
				CAR."fecha" AS "Fecha",
				TO_DECIMAL(H."INGMontoValorRazonableNeto", 15, 5) AS "S8_ValorMercado_Historico",
				TO_DECIMAL(H."INGMontoValorRazonableActivo", 15, 5) AS "S8_ValorMercadoActivo_Historico",
				TO_DECIMAL(H."INGMontoValorRazonableActivo", 15, 5) - TO_DECIMAL(H."INGMontoValorRazonableNeto", 15, 5) AS "S8_ValorMercadoPasivo_Historico",
				RANK() OVER (PARTITION BY  H."FolioCartera", TO_VARCHAR(H."FechaRespaldo", 'yyyy-MM') ORDER BY H."FechaRespaldo" DESC) AS "Rank"
				FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" FWD
				INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" CAR
					ON FWD."KIdContrato" = CAR."FolioCartera"
					AND FWD."IdCartera" = CAR."IdCartera"
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" H
					ON CAR."FolioCartera" = H."FolioCartera"
					AND TO_VARCHAR(H."FechaRespaldo", 'yyyy-MM')
						BETWEEN TO_VARCHAR(FWD."KFechaContrato", 'yyyy-MM')
						AND TO_VARCHAR(ADD_MONTHS_LAST(CASE 
															WHEN CAR."EventoReporte" = 'NUE' THEN FWD."S2_FechaTermino"
															ELSE CAR."fecha"
													   END
										, -1), 'yyyy-MM')
					AND NOT (CAR."EventoReporte" IN ('NUE', 'MOD') AND TO_VARCHAR(H."FechaRespaldo", 'yyyy-MM') = TO_VARCHAR(FWD."KFechaContrato", 'yyyy-MM'))
	) AS HIS
	WHERE HIS."Rank" = 1
) FWDHIS
	ON 	FWD."KIdContrato" = FWDHIS."FolioCartera" 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Origen" TO bcs_desarrollador WITH GRANT OPTION;