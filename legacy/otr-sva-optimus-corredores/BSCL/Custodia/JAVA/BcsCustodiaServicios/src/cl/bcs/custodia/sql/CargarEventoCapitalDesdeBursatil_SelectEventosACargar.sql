SELECT DISTINCT
	bur.ACF_MERCADO, bur.ACF_NEMO, bur.ACF_SEC_VC, bur.ACF_DESCRIP_VC, bur.ACF_FAC_ACC_ANT, bur.ACF_FAC_ACC_NUE, bur.ACF_FEC_ACT_VC, bur.ACF_FEC_LIM, bur.ACF_FEC_PAGO, bur.ACF_NEMO_2, bur.ACF_NUM_ACC_ANT, bur.ACF_NUM_ACC_DER, bur.ACF_NUM_ACC_NUE,
	CAST(bur.ACF_NUM_DIVDO AS BIGINT) AS ACF_NUM_DIVDO, COALESCE(bur.ACF_PRE_ANT_VC,0) AS ACF_PRE_ANT_VC, bur.ACF_PRE_EX_VC, bur.ACF_TIPO_VC, COALESCE(bur.ACF_TIPO_DIV,'') AS ACF_TIPO_DIV, COALESCE(bur.ACF_VAL_ACC,0) AS ACF_VAL_ACC, bur.ACF_VAL_AN_DS, bur.ACF_VAL_EX_DS, bur.ACF_VAL_LIB_AJU, bur.ACF_VIG_NAA, bur.ACF_VC_PROC, bur.ACF_MONEDA, bur.ACV_MERCADO, bur.ACV_NEMO,
	bur.ACV_SEC_VC, bur.ACV_INDICE, bur.ACV_HORA_MOD, bur.ACV_DESCRIP_VC, bur.ACV_FAC_ACC_ANT, bur.ACV_FAC_ACC_NUE, bur.ACV_FEC_ACT_VC, bur.ACV_FEC_LIM, bur.ACV_FEC_PAGO, bur.ACV_NEMO_2, bur.ACV_NUM_ACC_ANT, bur.ACV_NUM_ACC_DER, bur.ACV_NUM_ACC_NUE,
	bur.ACV_NUM_DIVDO, bur.ACV_PRE_ANT_VC, bur.ACV_PRE_EX_VC, bur.ACV_TIPO_VC, bur.ACV_TIPO_DIV, bur.ACV_VAL_ACC, bur.ACV_VAL_AN_DS, bur.ACV_VAL_EX_DS, bur.ACV_VAL_LIB_AJU, bur.ACV_VIG_NAA, bur.ACV_VC_PROC, bur.ACV_MONEDA, bur.ACV_USUARIO_MOD, 
	bur.ACV_TERMINAL_MOD, bur.ACV_TIPO_MOD, 
	ECV."IdEventoCapital" AS ECV_EVENTO_CAPITAL,
	Instrumento."IdClasificacionTipoInstrumento",
	Instrumento."IdTipoInstrumento",
	Instrumento."IdEmisor",
	Instrumento."IdInstrumento",
	Instrumento."Nemotecnico",
	CASE 
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'US$' THEN 'USD'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'US$' THEN 'USD'
		ELSE 'CLP'
	END AS "IdMoneda"
FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" AS BUR
INNER JOIN (
	SELECT  "ACF_NEMO", "ACF_SEC_VC", MAX("ACV_INDICE") AS "ACV_INDICE"
	--FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil"
	FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" 
	GROUP BY "ACF_NEMO", "ACF_SEC_VC"
) AS TMP
	ON BUR."ACF_NEMO" = TMP."ACF_NEMO"
	AND BUR."ACF_SEC_VC" = TMP."ACF_SEC_VC"
	AND BUR."ACV_INDICE" = TMP."ACV_INDICE"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"	ECV 
	ON 	ECV."IdEmpresa" = :p_IdEmpresa 						
	AND ECV."IdTipoEventoCapital" = BUR."ACF_TIPO_VC"					
	AND ECV."Secuencia"	= BUR."ACF_SEC_VC"					
	AND LTRIM(RTRIM(ECV."Nemo")) = LTRIM(RTRIM(BUR."ACF_NEMO"))      
	AND ECV."IdClasificacionTipoInstrumento" = CASE BUR."ACF_MERCADO"
													WHEN 'AC' THEN 'RV'																	
													WHEN 'CF' THEN 'RV' -- CFI																	
													ELSE BUR."ACF_MERCADO" 
												END														
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
ON Trim(BUR."ACF_NEMO") = Trim(Instrumento."Nemotecnico")	
WHERE bur."ACF_FEC_LIM" BETWEEN :p_Fecha_inicio AND :p_Fecha_fin		-- BUSCA POR FECHA LIMITE
AND (RTRIM(LTRIM(IFNULL(bur."ACF_NEMO",''))) <> '') -- Los dividendos que en Ruebas est�n sin factor o sin moneda entran igual a optimus
AND Instrumento."IdClasificacionTipoInstrumento" <> 'FM'
UNION
SELECT DISTINCT
	bur.ACF_MERCADO, bur.ACF_NEMO, bur.ACF_SEC_VC, bur.ACF_DESCRIP_VC, bur.ACF_FAC_ACC_ANT, bur.ACF_FAC_ACC_NUE, bur.ACF_FEC_ACT_VC, bur.ACF_FEC_LIM, bur.ACF_FEC_PAGO, bur.ACF_NEMO_2, bur.ACF_NUM_ACC_ANT, bur.ACF_NUM_ACC_DER, bur.ACF_NUM_ACC_NUE,
	CAST(bur.ACF_NUM_DIVDO AS BIGINT) AS ACF_NUM_DIVDO, COALESCE(bur.ACF_PRE_ANT_VC,0) AS ACF_PRE_ANT_VC, bur.ACF_PRE_EX_VC, bur.ACF_TIPO_VC, COALESCE(bur.ACF_TIPO_DIV,'') AS ACF_TIPO_DIV, COALESCE(bur.ACF_VAL_ACC,0) AS ACF_VAL_ACC, bur.ACF_VAL_AN_DS, bur.ACF_VAL_EX_DS, bur.ACF_VAL_LIB_AJU, bur.ACF_VIG_NAA, bur.ACF_VC_PROC, bur.ACF_MONEDA, bur.ACV_MERCADO, bur.ACV_NEMO,
	bur.ACV_SEC_VC, bur.ACV_INDICE, bur.ACV_HORA_MOD, bur.ACV_DESCRIP_VC, bur.ACV_FAC_ACC_ANT, bur.ACV_FAC_ACC_NUE, bur.ACV_FEC_ACT_VC, bur.ACV_FEC_LIM, bur.ACV_FEC_PAGO, bur.ACV_NEMO_2, bur.ACV_NUM_ACC_ANT, bur.ACV_NUM_ACC_DER, bur.ACV_NUM_ACC_NUE,
	bur.ACV_NUM_DIVDO, bur.ACV_PRE_ANT_VC, bur.ACV_PRE_EX_VC, bur.ACV_TIPO_VC, bur.ACV_TIPO_DIV, bur.ACV_VAL_ACC, bur.ACV_VAL_AN_DS, bur.ACV_VAL_EX_DS, bur.ACV_VAL_LIB_AJU, bur.ACV_VIG_NAA, bur.ACV_VC_PROC, bur.ACV_MONEDA, bur.ACV_USUARIO_MOD, 
	bur.ACV_TERMINAL_MOD, bur.ACV_TIPO_MOD, 
	ECV."IdEventoCapital" AS ECV_EVENTO_CAPITAL,
	Instrumento."IdClasificacionTipoInstrumento",
	Instrumento."IdTipoInstrumento",
	Instrumento."IdEmisor",
	Instrumento."IdInstrumento",
	Instrumento."Nemotecnico",
	CASE 
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'US$' THEN 'USD'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'US$' THEN 'USD'
		ELSE 'CLP'
	END AS "IdMoneda"
FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" AS BUR
INNER JOIN (
	SELECT  "ACF_NEMO", "ACF_SEC_VC", MAX("ACV_INDICE") AS "ACV_INDICE"
	--FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil"
	FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" 
	GROUP BY "ACF_NEMO", "ACF_SEC_VC"
) AS TMP
	ON BUR."ACF_NEMO" = TMP."ACF_NEMO"
	AND BUR."ACF_SEC_VC" = TMP."ACF_SEC_VC"
	AND BUR."ACV_INDICE" = TMP."ACV_INDICE"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"	ECV 
	ON 	ECV."IdEmpresa" = 1 						
	AND ECV."IdTipoEventoCapital" = BUR."ACF_TIPO_VC"					
	AND ECV."Secuencia"	= BUR."ACF_SEC_VC"					
	AND LTRIM(RTRIM(ECV."Nemo")) = LTRIM(RTRIM(BUR."ACF_NEMO"))      
	AND ECV."IdClasificacionTipoInstrumento" = CASE BUR."ACF_MERCADO"
													WHEN 'AC' THEN 'RV'																	
													WHEN 'CF' THEN 'RV' -- CFI																	
													ELSE BUR."ACF_MERCADO" 
												END														
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
ON Trim(BUR."ACF_NEMO") = Trim(Instrumento."Nemotecnico")	
WHERE LEFT(bur."ACV_HORA_MOD",10) > bur."ACV_FEC_LIM" AND LEFT(bur."ACV_HORA_MOD",10)<= bur."ACV_FEC_PAGO" 	-- UNE LOS QUE SON MODIFICADOS DESPUES DE LA FECHA LIMITE Y ANTES DEL PAGO
AND (RTRIM(LTRIM(IFNULL(bur."ACF_NEMO",''))) <> '')
AND Instrumento."IdClasificacionTipoInstrumento" <> 'FM'
UNION
SELECT DISTINCT
	bur.ACF_MERCADO, bur.ACF_NEMO, bur.ACF_SEC_VC, bur.ACF_DESCRIP_VC, bur.ACF_FAC_ACC_ANT, bur.ACF_FAC_ACC_NUE, bur.ACF_FEC_ACT_VC, bur.ACF_FEC_LIM, bur.ACF_FEC_PAGO, bur.ACF_NEMO_2, bur.ACF_NUM_ACC_ANT, bur.ACF_NUM_ACC_DER, bur.ACF_NUM_ACC_NUE,
	CAST(bur.ACF_NUM_DIVDO AS BIGINT) AS ACF_NUM_DIVDO, COALESCE(bur.ACF_PRE_ANT_VC,0) AS ACF_PRE_ANT_VC, bur.ACF_PRE_EX_VC, bur.ACF_TIPO_VC, COALESCE(bur.ACF_TIPO_DIV,'') AS ACF_TIPO_DIV, COALESCE(bur.ACF_VAL_ACC,0) AS ACF_VAL_ACC, bur.ACF_VAL_AN_DS, bur.ACF_VAL_EX_DS, bur.ACF_VAL_LIB_AJU, bur.ACF_VIG_NAA, bur.ACF_VC_PROC, bur.ACF_MONEDA, bur.ACV_MERCADO, bur.ACV_NEMO,
	bur.ACV_SEC_VC, bur.ACV_INDICE, bur.ACV_HORA_MOD, bur.ACV_DESCRIP_VC, bur.ACV_FAC_ACC_ANT, bur.ACV_FAC_ACC_NUE, bur.ACV_FEC_ACT_VC, bur.ACV_FEC_LIM, bur.ACV_FEC_PAGO, bur.ACV_NEMO_2, bur.ACV_NUM_ACC_ANT, bur.ACV_NUM_ACC_DER, bur.ACV_NUM_ACC_NUE,
	bur.ACV_NUM_DIVDO, bur.ACV_PRE_ANT_VC, bur.ACV_PRE_EX_VC, bur.ACV_TIPO_VC, bur.ACV_TIPO_DIV, bur.ACV_VAL_ACC, bur.ACV_VAL_AN_DS, bur.ACV_VAL_EX_DS, bur.ACV_VAL_LIB_AJU, bur.ACV_VIG_NAA, bur.ACV_VC_PROC, bur.ACV_MONEDA, bur.ACV_USUARIO_MOD, 
	bur.ACV_TERMINAL_MOD, bur.ACV_TIPO_MOD, 
	ECV."IdEventoCapital" AS ECV_EVENTO_CAPITAL,
	Instrumento."IdClasificacionTipoInstrumento",
	Instrumento."IdTipoInstrumento",
	Instrumento."IdEmisor",
	Instrumento."IdInstrumento",
	Instrumento."Nemotecnico",
	CASE 
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACV_MONEDA",'') <> '' AND IFNULL(bur."ACV_MONEDA",'') = 'US$' THEN 'USD'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = '$' THEN 'CLP'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'EURO' THEN 'EUR'
		WHEN IFNULL(bur."ACF_MONEDA",'') <> '' AND IFNULL(bur."ACF_MONEDA",'') = 'US$' THEN 'USD'
		ELSE 'CLP'
	END AS "IdMoneda"
FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" AS BUR
INNER JOIN (
	SELECT  "ACF_NEMO", "ACF_SEC_VC", MAX("ACV_INDICE") AS "ACV_INDICE"
	--FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil"
	FROM "BSCL.Custodia.BD::GBL.CargarEventoCapitalDesdeBursatil_Eventos" 
	GROUP BY "ACF_NEMO", "ACF_SEC_VC"
) AS TMP
	ON BUR."ACF_NEMO" = TMP."ACF_NEMO"
	AND BUR."ACF_SEC_VC" = TMP."ACF_SEC_VC"
	AND BUR."ACV_INDICE" = TMP."ACV_INDICE"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"	ECV 
	ON 	ECV."IdEmpresa" = :p_IdEmpresa						
	AND ECV."IdTipoEventoCapital" = BUR."ACF_TIPO_VC"					
	AND ECV."Secuencia"	= BUR."ACF_SEC_VC"					
	AND LTRIM(RTRIM(ECV."Nemo")) = LTRIM(RTRIM(BUR."ACF_NEMO"))      
	AND ECV."IdClasificacionTipoInstrumento" = CASE BUR."ACF_MERCADO"
													WHEN 'AC' THEN 'RV'																	
													WHEN 'CF' THEN 'RV' -- CFI																	
													ELSE BUR."ACF_MERCADO" 
												END														
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
ON Trim(BUR."ACF_NEMO") = Trim(Instrumento."Nemotecnico")	
WHERE 
BUR."ACF_TIPO_VC" IN ('DV','RC') AND :p_Fecha_fin < BUR."ACV_FEC_PAGO" AND  bur."ACF_FEC_LIM" < :p_Fecha_fin	-- SON EVENTOS DE DIVIDENDOS QUE NO ESTAN CARGADOS HASTA LA FECHA DE PAGO
AND NOT EXISTS (
	SELECT 1
	FROM "BSCL.Custodia.BD::CUS.EventoCapital" ECV
	WHERE ECV."IdEmpresa" = :p_IdEmpresa 
	AND ECV."IdTipoEventoCapital" = BUR."ACF_TIPO_VC"
	AND ECV."Secuencia" = BUR."ACF_SEC_VC"
	AND LTRIM(RTRIM(ECV."Nemo")) = LTRIM(RTRIM(BUR."ACF_NEMO"))
	AND ECV."IdClasificacionTipoInstrumento" = CASE BUR."ACF_MERCADO"
													WHEN 'AC' THEN 'RV'
													WHEN 'CF' THEN 'RV'
													ELSE BUR."ACF_MERCADO"
												END
)
AND (RTRIM(LTRIM(IFNULL(bur."ACF_NEMO",''))) <> '')
AND Instrumento."IdClasificacionTipoInstrumento" <> 'FM'
