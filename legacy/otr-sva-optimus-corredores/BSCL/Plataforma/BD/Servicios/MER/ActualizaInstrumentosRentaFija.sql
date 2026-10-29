SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosRentaFija";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosRentaFija" ( 

	IN	p_limpiaTablas						NVARCHAR(1) DEFAULT 'N',
	
	OUT	p_out_Resultado						NVARCHAR(40),
	OUT	p_out_Mensaje						NVARCHAR(520)
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
BEGIN
	DECLARE v_countInstrumentosNuevos,v_countInstrumentosActualizados BIGINT;
	
	v_Instrumentos = SELECT COALESCE(INS."IdInstrumento",0) as "IdInstrumento",TRIM(RF."RF_NEMO") as "Nemotecnico",TRIM(RF."RF_CODIGO_ISIN") as "ISIN",RF."SE_CORTE_MIN" as "CorteMinimo",
							'S' as "EstaInscrito",RF."SE_IND_DIVISI" as "EsDivisible",
							REPLACE(REPLACE(TRIM(RF."RF_RAZON_SOCIAL"),'%¤','N'),'O¤','''') as "RazonSocial",RF."RF_FEC_EMI" as "FechaEmision",
								RF."RF_FEC_VCTO" as "FechaVencimiento",COALESCE(TRIM(RF."SE_EMISOR"),'OPTIMUS') as "IdEmisor",
							CASE 
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ Promedio' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'IPC' 				then 	'CLP' 
								WHEN TRIM(RF."SE_TIPO_REAJ") = '$' 				then 	'CLP'
								WHEN TRIM(RF."SE_TIPO_REAJ") = '$ Nominales' 		then 	'CLP'
								WHEN TRIM(REPLACE(RF."SE_TIPO_REAJ",'.','')) = 'UF' THEN 'UF'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ Acuerdo' 		then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.CH. 12' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.CH. Ap' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.Ch.12H' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'I.V.P.' 			then 	'IVP'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B. Estad' 	then 	'USD'
								ELSE '' END AS "IdTipoReajuste",
							CASE 
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ Promedio' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'IPC' 			then 	'CLP' 
								WHEN TRIM(RF."SE_TIPO_REAJ") = '$' 				then 	'CLP'
								WHEN TRIM(RF."SE_TIPO_REAJ") = '$ Nominales' 	then 	'CLP'
								WHEN TRIM(REPLACE(RF."SE_TIPO_REAJ",'.','')) = 'UF' THEN 'CLP'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ Acuerdo' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.CH. 12' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.CH. Ap' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B.Ch.12H' 	then 	'USD'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'I.V.P.' 		then 	'CLP'
								WHEN TRIM(RF."SE_TIPO_REAJ") = 'US$ B. Estad' 	then 	'USD'
								ELSE 'CLP' END AS "IdMonedaEmision",
							CASE WHEN TRIM(RF."SE_TIPO_REAJ") = '$' THEN 'N' WHEN TRIM(RF."SE_TIPO_REAJ") = '$ Nominales' THEN 'N' ELSE 'N' END AS "Reajustable",
							CASE 
								WHEN TRIM(RF."SE_TIPO_MONEDA_PAGO") = '$' THEN 'CLP'
								WHEN TRIM(RF."SE_TIPO_MONEDA_PAGO") = 'US$' THEN 'USD'
							    WHEN TRIM(RF."SE_TIPO_MONEDA_PAGO") = 'EUR' THEN 'EUR'
							    ELSE 'CLP' END AS "IdMoneda",
							       
							CASE 
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BB' 	THEN  'BBC'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BCD' 	THEN  'BCD'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BCP' 	THEN  'BCP'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BCU' 	THEN  'BCU'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BE' 	THEN  'BEM'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'CERO' 	THEN  'CER'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'LH' 	THEN  'LTH'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'PRC' 	THEN 'PRC'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'ZERO' 	THEN  'ZER'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BC' 	THEN  'BCA'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BCX' 	THEN  'BCX'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BTP' 	THEN  'BTP'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BTU' 	THEN  'BTU'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BU' 	THEN  'BUS'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BVL' 	THEN  'BVL'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'CORA' 	THEN  'COR'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'PCX' 	THEN  'PCX'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'PRD' 	THEN  'PRD'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'XERO' 	THEN  'XER'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BR' 	THEN 'BR'
								WHEN TRIM(RF."SE_TIPO_INSTRU") = 'BH' 	THEN 'BH'
							ELSE 'ORF' END AS "IdTipoInstrumento",
							CASE 
								WHEN RF."RF_ESTADO" = '1' OR RF."RF_ESTADO" = '3' then 'ACT'
								ELSE 'INA' END AS "IdEstado",2 as "DecimalesPrecio",
							SUBSTR_BEFORE (RF."SE_CLASIF_RIESGO",' ;') as "IdClasificacionRiesgo",1 as "Tamano",0 as "IdInstrumentoRelacionado",
							'0001-01-01' as "FechaInicioVigencia",RF."SE_TASA_FISCAL" as "TasaFiscal",
							CASE RF."SE_IND_ART104" WHEN '' THEN 'N' ELSE RF."SE_IND_ART104" END as "IndArt104",
							'' as "TipoValorizacion",RF."SE_TASA_NOMI_EMI" AS "Tasa",
							0 as "Precio",'T' as "ValorizaPor",CASE WHEN RF."SE_NUM_CUPON" > 1 THEN 'S' ELSE 'N' END AS "TieneCupon",RF."SE_NUM_CUPON" as "NumeroCupones",'S' as "IndicadorLiquidaDCV",
							pais."IdPais" as "IdPaisEmisor",RF."SE_COD_SERIE" AS "Serie",RF."SE_IND_BOLSON" AS "IndicadorBolson",RF."SE_PERI_PAGO_CUPON" AS "FrecuenciaCupon",
							RF."SE_ORDEN" aS "IdOrdenSerie",RF."SE_TERA" aS "Tera",RF."SE_NUM_AMORT" AS "NumeroAmortizaciones", RF."SE_IND_VENTA_CORTA" AS "IndicadorVentaCorta"
					FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosRentaFija" RF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on INS."Nemotecnico" = TRIM(RF."RF_NEMO")
					LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI on EMI."IdEmisor" = TRIM(RF."SE_EMISOR")
					LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" pais on pais."CodigoPais" = 'CL';
		
		v_IdInstrumento = SELECT "Nemotecnico","BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as "IdInstrumento"
							FROM :v_Instrumentos ins
							WHERE ins."IdInstrumento" = 0;
		
		--SELECT * FROM "BSCL.Plataforma.BD::INS.Instrumento" where "IndicadorBolson" = 'S'
		INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento"("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial",
		"FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","IdMonedaEmision","Reajustable","IdMoneda","IdTipoInstrumento",
		"IdEstado","DecimalesPrecio","Tamano","FechaInicioVigencia",
		"TasaFiscal","IndArt104","TipoValorizacion","Tasa","Precio","ValorizaPor","TieneCupon",
		"IdPaisEmisor","IndicadorLiquidaDCV","Serie","IndicadorBolson","FrecuenciaCupon","NumeroCupones","Tera","NumeroAmortizaciones","IdOrdenSerie")
		SELECT IDINS."IdInstrumento",INS."Nemotecnico",INS."ISIN",INS."CorteMinimo",INS."EstaInscrito",INS."EsDivisible",INS."RazonSocial",
		INS."FechaEmision",INS."FechaVencimiento",INS."IdInstrumentoRelacionado",INS."IdEmisor",INS."IdTipoReajuste",INS."IdMonedaEmision",INS."Reajustable",INS."IdMoneda",INS."IdTipoInstrumento",
		INS."IdEstado",INS."DecimalesPrecio",INS."Tamano",INS."FechaInicioVigencia",
		INS."TasaFiscal",INS."IndArt104",INS."TipoValorizacion",INS."Tasa",INS."Precio",INS."ValorizaPor",INS."TieneCupon",
		INS."IdPaisEmisor",INS."IndicadorLiquidaDCV",INS."Serie",INS."IndicadorBolson",INS."FrecuenciaCupon",INS."NumeroCupones",
		INS."Tera",INS."NumeroAmortizaciones",INS."IdOrdenSerie"
		FROM :v_Instrumentos INS
		INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
		WHERE INS."IdInstrumento" = 0;
		
		INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento"("IdBolsaInstrumento", "IdInstrumento","IdBolsa")
		SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL AS "IdBolsaInstrumento",IDINS."IdInstrumento",'BCS'
		FROM :v_Instrumentos INS
		INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
		WHERE INS."IdInstrumento" = 0;
	
		INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento" ("IdBolsaInstrumento", "IdInstrumento","IdBolsa")
		SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL AS "IdBolsaInstrumento", IDINS."IdInstrumento",'BEC'
		FROM :v_Instrumentos INS
		INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
		WHERE INS."IdInstrumento" = 0;
		
			
		UPDATE "BSCL.Plataforma.BD::INS.BolsaInstrumento"
		SET "IdBolsa" = 'BCS'
		WHERE "IdBolsa" = null or "IdBolsa" = '';
		
		
		UPDATE INS
		SET
			"ISIN" 						= v_INS."ISIN",
			"CorteMinimo"				= v_INS."CorteMinimo",
			"EsDivisible"				= v_INS."EsDivisible",
			"RazonSocial"				= v_INS."RazonSocial",
			"FechaEmision"				= v_INS."FechaEmision",
			"FechaVencimiento"			= v_INS."FechaVencimiento",
			"IdInstrumentoRelacionado"	= v_INS."IdInstrumentoRelacionado",
			"IdEmisor" 					= v_INS."IdEmisor",
			--"IdTipoReajuste" 			= v_INS."IdTipoReajuste",
			"IdMonedaEmision"			= v_INS."IdMonedaEmision",
			"Reajustable"				= v_INS."Reajustable",
			"IdMoneda"					= v_INS."IdMoneda",
			"IdTipoInstrumento"			= v_INS."IdTipoInstrumento",
			"IdEstado" 					= v_INS."IdEstado",
			"DecimalesPrecio"			= v_INS."DecimalesPrecio",
			"Tamano"					= v_INS."Tamano",
			"FechaInicioVigencia"		= v_INS."FechaInicioVigencia",
			"TasaFiscal"				= v_INS."TasaFiscal",
			"IndArt104"					= v_INS."IndArt104",
			"TipoValorizacion"			= v_INS."TipoValorizacion",
			"Tasa"						= v_INS."Tasa",
			"Precio"					= v_INS."Precio",
			"ValorizaPor"				= v_INS."ValorizaPor",
			"Tera"						= v_INS."Tera",
			"NumeroAmortizaciones"		= v_INS."NumeroAmortizaciones",
			"IdOrdenSerie"				= v_INS."IdOrdenSerie",
			"TieneCupon"				= v_INS."TieneCupon",
			"IdPaisEmisor"				= v_INS."IdPaisEmisor",
			"IndicadorLiquidaDCV"		= v_INS."IndicadorLiquidaDCV",
			"Serie"						= v_INS."Serie",
			"IndicadorBolson"			= v_INS."IndicadorBolson",
			"FrecuenciaCupon"			= v_INS."FrecuenciaCupon",
			"NumeroCupones"				= v_INS."NumeroCupones"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
		INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
		WHERE v_INS."IdInstrumento" > 0;
		
		INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"("IdInstrumentoSubyacente", "IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente", "IdInstrumento")
		SELECT "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente".NEXTVAL, 'PRF' AS "IdTipoInstrumento", 'BCS' AS "IdBolsa", 
		 		INS."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente", INS."IdInstrumento" as "IdInstrumento"
		FROM :v_Instrumentos INS
		LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" sub on INS."IdInstrumento" = sub."IdInstrumento" and sub."IdBolsa" = 'BCS' AND sub."IdTipoInstrumento" = 'PRF'
		WHERE INS."IndicadorVentaCorta" = 'S' AND sub."IdInstrumento" IS NULL;
		
		IF (:p_limpiaTablas = 'S') THEN
			DELETE FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosRentaFija";
		END IF;
		
		SELECT COUNT(1) INTO v_countInstrumentosNuevos FROM :v_Instrumentos INS WHERE INS."IdInstrumento" = 0;
		SELECT COUNT(1) INTO v_countInstrumentosActualizados FROM :v_Instrumentos INS WHERE INS."IdInstrumento" > 0;
		
		p_out_Resultado = 'EXITO';
		p_out_Mensaje = 'INSTRUMENTOS NUEVOS: ' || :v_countInstrumentosNuevos || ' INSTRUMENTO ACTUALIZADOS: ' || :v_countInstrumentosActualizados;
		
END;
