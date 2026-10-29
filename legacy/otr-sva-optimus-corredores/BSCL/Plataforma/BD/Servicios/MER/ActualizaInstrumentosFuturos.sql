SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosFuturos";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosFuturos" ( 
	IN	p_limpiaTablas						NVARCHAR(1) DEFAULT 'N',
	
	OUT	p_out_Resultado						NVARCHAR(40),
	OUT	p_out_Mensaje						NVARCHAR(520)
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
BEGIN
	DECLARE v_maxBolsaInstrumentoPrecio BIGINT;
	DECLARE v_maxBolsaInstrumentoPrecioRegistrosNuevosBIP BIGINT;
	DECLARE v_Fecha DATE := CURRENT_DATE;
	
	DECLARE v_countInstrumentosNuevos,v_countInstrumentosActualizados BIGINT;
	
	DECLARE v_IdBolsa NVARCHAR(3) := 'BCS';
	
	--1 IDENTIFICA INSTRUMENTOS EXISTENTES Y NUEVOS
	v_Instrumentos = SELECT COALESCE(INS."IdInstrumento",0) as "IdInstrumento",TRIM(FU."NEMO") as "Nemotecnico",'' as "ISIN",0 as "CorteMinimo",
							'S' as "EstaInscrito",'S' as "EsDivisible",FU."RAZON_SOCIAL" as "RazonSocial",'0001-01-01' as "FechaEmision",
								FU."FEC_VCTO" as "FechaVencimiento",'BSX' as "IdEmisor",'' as "IdTipoReajuste",'N' as "Reajustable",
							'CLP' AS "IdMoneda",
							CASE FU."TIPO_INSTRU"
						      	--WHEN 'A' THEN 'FIC' -- Aun no estan en Bursatil
						      	--WHEN 'B' THEN 'FBC' -- Aun no estan en Bursatil
						      	WHEN 'C' THEN 'FUF' 
								WHEN 'D' THEN 'FDO'
						       	WHEN 'I' THEN 'FIP'
						       	WHEN 'V' THEN 'FBO'
						       	ELSE 'UNK'
			 			   END AS "IdTipoInstrumento",
			 			   CASE FU."TIPO_INSTRU"
						      	--WHEN 'A' THEN 'FIC' -- Aun no estan en Bursatil
						      	--WHEN 'B' THEN 'FBC' -- Aun no estan en Bursatil
						      	WHEN 'C' THEN 'FUF' 
								WHEN 'D' THEN 'FDO'
						       	WHEN 'I' THEN 'FIP'
						       	WHEN 'V' THEN 'FBO'
						       	ELSE 'UNK'
			 			   END AS "IdTipoInstrumentoOrigen",
			 			   CASE 
								WHEN FU."ESTADO" = '1' OR FU."ESTADO" = '3' then 'ACT'
								ELSE 'INA' END AS "IdEstado",FU."NUM_DEC_PRE" as "DecimalesPrecio",
							FU."LOTE_PADRON" as "Tamano",'' as "Lista",0 as "IdInstrumentoRelacionado",
							FU."FEC_INI_VIG" as "FechaInicioVigencia",0 as "TasaFiscal",'' as "IndArt104",
							'' as "IdEntidadClasificadoraRiesgo",'' as "ValorClasificacionRiesgo",'' as "TipoValorizacion",0 AS "Tasa",
							0 as "Precio",'P' as "ValorizaPor",'N' AS "TieneCupon",'S' as "IndicadorLiquidaDCV",
							pais."IdPais" as "IdPaisEmisor",'' AS "Serie",'N' AS "IndicadorBolson",
							
							FU."PRE_APE_DI_AC" AS "PrecioApertura",FU."PRE_CIE_DI_AC" AS "PrecioCierreDiaActual",FU."PRE_CIE_DI_AN" AS "PrecioCierreDiaAnterior",
							FU."PRE_MAY" AS "PrecioMayorDiaActual",FU."PRE_MEN" AS "PrecioMenorDiaActual",FU."PRE_MED" AS "PrecioMedioDiaActual",
							FU."MONTO" AS "MontoTransadoDiaActual",FU."PRE_ULT_DI_AC" AS "UltimoPrecioDiaActual"
							
					FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosFuturos" FU
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on INS."Nemotecnico" = TRIM(FU."NEMO")
					LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" pais on pais."CodigoPais" = 'CL';

	v_IdInstrumento = SELECT "Nemotecnico","BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as "IdInstrumento"
							FROM :v_Instrumentos ins
							WHERE ins."IdInstrumento" = 0 AND "IdTipoInstrumento" != 'UNK';
						
	
					
	--SELECT 'v_Instrumentos',* from :v_Instrumentos;
	
				
	INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento"("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial",
		"FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdTipoInstrumentoOrigen",
		"IdEstado","DecimalesPrecio","Tamano","FechaInicioVigencia",
		"TasaFiscal","IndArt104","TipoValorizacion","Tasa","Precio","ValorizaPor","TieneCupon",
		"IdPaisEmisor","IndicadorLiquidaDCV","Serie","IndicadorBolson")
		SELECT IDINS."IdInstrumento",INS."Nemotecnico",INS."ISIN",INS."CorteMinimo",INS."EstaInscrito",INS."EsDivisible",INS."RazonSocial",
		INS."FechaEmision",INS."FechaVencimiento",INS."IdInstrumentoRelacionado",INS."IdEmisor",INS."IdTipoReajuste",INS."Reajustable",INS."IdMoneda",INS."IdTipoInstrumento",INS."IdTipoInstrumentoOrigen",
		INS."IdEstado",INS."DecimalesPrecio",INS."Tamano",INS."FechaInicioVigencia",
		INS."TasaFiscal",INS."IndArt104",INS."TipoValorizacion",INS."Tasa",INS."Precio",INS."ValorizaPor",INS."TieneCupon",
		INS."IdPaisEmisor",INS."IndicadorLiquidaDCV",INS."Serie",INS."IndicadorBolson"
		FROM :v_Instrumentos INS
		INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
		WHERE INS."IdInstrumento" = 0 AND "IdTipoInstrumento" != 'UNK';
		
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
		"IdTipoReajuste" 			= v_INS."IdTipoReajuste",
		"Reajustable"				= v_INS."Reajustable",
		"IdMoneda"					= v_INS."IdMoneda",
		"IdTipoInstrumento"			= v_INS."IdTipoInstrumento",
		"IdTipoInstrumentoOrigen" 	= v_INS."IdTipoInstrumentoOrigen",
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
		"TieneCupon"				= v_INS."TieneCupon",
		"IdPaisEmisor"				= v_INS."IdPaisEmisor",
		"IndicadorLiquidaDCV"		= v_INS."IndicadorLiquidaDCV",
		"Serie"						= v_INS."Serie",
		"IndicadorBolson"			= v_INS."IndicadorBolson"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
	WHERE v_INS."IdInstrumento" > 0;

	--Bolsa Instrumento
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento" ("IdBolsaInstrumento", "IdInstrumento", "IdBolsa")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL,IDINS."IdInstrumento", :v_IdBolsa
	FROM :v_IdInstrumento IDINS;
/*
 * INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento" ("IdBolsaInstrumento", "IdInstrumento", "IdBolsa")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL,IDINS."IdInstrumento", :v_IdBolsa
	FROM :v_Instrumentos INS
		INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
	WHERE INS."IdInstrumento" = 0 AND INS."IdTipoInstrumento" != 'UNK';
 

	-- Actualizar la bolsa para aquellos instrumentos que ya fueron cargados
	UPDATE BI
	SET	BI."IdBolsa" = :v_IdBolsa 
	FROM "BSCL.Plataforma.BD::INS.BolsaInstrumento" BI
		INNER JOIN :v_Instrumentos v_INS ON BI."IdInstrumento" = v_INS."IdInstrumento"
	WHERE v_INS."IdInstrumento" > 0;
*/		
	--PRECIO PARA INSTRUMENTOS NUEVOS
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ("IdInstrumento","IdBolsa","Fecha","FechaActualizacionArchivo","PrecioApertura","PrecioCierre",
	"PrecioMayor","PrecioMenor","PrecioMedio","PrecioUltimaTransaccion","UnidadesTransadas","MontoTransado")
	SELECT IDINS."IdInstrumento",:v_IdBolsa AS "IdBolsa",v_Fecha as "Fecha",CURRENT_TIMESTAMP AS "FechaActualizacionArchivo",v_INS."PrecioApertura",v_INS."PrecioCierreDiaActual" AS "PrecioCierre",
	v_INS."PrecioMayorDiaActual" AS "PrecioMayor",v_INS."PrecioMenorDiaActual" AS "PrecioMenor",
	v_INS."PrecioMedioDiaActual" AS "PrecioMedio",v_INS."UltimoPrecioDiaActual" AS "PrecioUltimaTransaccion",
	0 AS "UnidadesTransadas",v_INS."MontoTransadoDiaActual" AS "MontoTransado"
	FROM :v_Instrumentos v_INS
	INNER JOIN :v_IdInstrumento IDINS ON v_INS."Nemotecnico" = IDINS."Nemotecnico"
	WHERE v_INS."IdInstrumento" = 0;
	
	--PRECIO PARA INSTRUMENTOS EXISTENTES, PERO SIN REGISTRO DE PRECIO
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ("IdInstrumento","IdBolsa","Fecha","FechaActualizacionArchivo","PrecioApertura","PrecioCierre",
	"PrecioMayor","PrecioMenor","PrecioMedio","PrecioUltimaTransaccion","UnidadesTransadas","MontoTransado")
	SELECT v_INS."IdInstrumento",:v_IdBolsa AS "IdBolsa",v_Fecha AS "Fecha",CURRENT_TIMESTAMP AS "FechaActualizacionArchivo",v_INS."PrecioApertura",v_INS."PrecioCierreDiaActual" AS "PrecioCierre",
	v_INS."PrecioMayorDiaActual" AS "PrecioMayor",v_INS."PrecioMenorDiaActual" AS "PrecioMenor",
	v_INS."PrecioMedioDiaActual" AS "PrecioMedio",v_INS."UltimoPrecioDiaActual" AS "PrecioUltimaTransaccion",
	0 AS "UnidadesTransadas",v_INS."MontoTransadoDiaActual" AS "MontoTransado"
	FROM :v_Instrumentos v_INS
	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" BIP ON BIP."IdInstrumento" = v_INS."IdInstrumento" and BIP."IdBolsa" = :v_IdBolsa AND "Fecha" = :v_Fecha
	WHERE v_INS."IdInstrumento" > 0 AND BIP."IdInstrumento" IS NULL;
		
		
	--ACTUALIZA PRECIOS PARA INSTRUMENTOS EXISTENTES
	UPDATE BIP
	SET	"FechaActualizacionArchivo" = CURRENT_TIMESTAMP,
		"PrecioApertura"			= v_INS."PrecioApertura",
		"PrecioCierre"				= v_INS."PrecioCierreDiaActual",
		"PrecioMayor"				= v_INS."PrecioMayorDiaActual",
		"PrecioMenor"				= v_INS."PrecioMenorDiaActual",
		"PrecioMedio"				= v_INS."PrecioMedioDiaActual",
		"PrecioUltimaTransaccion"	= v_INS."UltimoPrecioDiaActual",
		"MontoTransado" 			= v_INS."MontoTransadoDiaActual"
	FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" BIP
	INNER JOIN :v_Instrumentos v_INS ON BIP."IdInstrumento" = v_INS."IdInstrumento" AND "IdBolsa" = :v_IdBolsa AND "Fecha" = :v_Fecha AND v_INS."IdInstrumento" > 0;

	--6 GUARDA PRECIOS EN HISTORICO
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosHistoricos"(
	"IdInstrumento","IdBolsa",
	"MontoTransadoAgnoActual","MontoTransadoDiaActual","MontoTransadoDiaAnterior","MontoTransadoMesActual","PrecioCierreDiaActual","PrecioCierreDiaAnterior",
	"PrecioMayorAgnoActual","PrecioMayorDiaActual","PrecioMayorDiaAnterior","PrecioMayorMesActual",
	"PrecioMedioAgnoActual","PrecioMedioDiaActual","PrecioMedioDiaAnterior","PrecioMedioMesActual",
	"PrecioMenorAgnoActual","PrecioMenorDiaActual","PrecioMenorDiaAnterior","PrecioMenorMesActual",				
	"UltimoPrecioDiaActual","UnidadesTransadasDiaActual","UnidadesTransadasDiaAnterior","VariacionDiaria")
	SELECT COALESCE(pins."IdInstrumento",IDINS."IdInstrumento") as "IdInstrumento",:v_IdBolsa as "IdBolsa",
	0 AS "MontoTransadoAgnoActual","MontoTransadoDiaActual",0 AS "MontoTransadoDiaAnterior",0 AS "MontoTransadoMesActual","PrecioCierreDiaActual","PrecioCierreDiaAnterior",
	0 AS "PrecioMayorAgnoActual","PrecioMayorDiaActual",0 AS "PrecioMayorDiaAnterior",0 AS "PrecioMayorMesActual",
	0 AS "PrecioMedioAgnoActual","PrecioMedioDiaActual",0 AS "PrecioMedioDiaAnterior",0 AS "PrecioMedioMesActual",
	0 AS "PrecioMenorAgnoActual","PrecioMenorDiaActual",0 AS "PrecioMenorDiaAnterior",0 AS "PrecioMenorMesActual",				
	"UltimoPrecioDiaActual",0 AS "UnidadesTransadasDiaActual",0 AS "UnidadesTransadasDiaAnterior",0 AS "VariacionDiaria"
	FROM :v_Instrumentos pins
	LEFT JOIN :v_IdInstrumento IDINS ON pins."Nemotecnico" = IDINS."Nemotecnico";		
	
	IF (:p_limpiaTablas = 'S') THEN
		DELETE FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosFuturos";
	END IF;
	
	SELECT COUNT(1) INTO v_countInstrumentosNuevos FROM :v_Instrumentos INS WHERE INS."IdInstrumento" = 0;
	SELECT COUNT(1) INTO v_countInstrumentosActualizados FROM :v_Instrumentos INS WHERE INS."IdInstrumento" > 0;
	
	p_out_Resultado = 'EXITO';
	p_out_Mensaje = 'INSTRUMENTOS NUEVOS: ' || :v_countInstrumentosNuevos || ' INSTRUMENTO ACTUALIZADOS: ' || :v_countInstrumentosActualizados;
  
END;