SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosCuotasFondo";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosCuotasFondo" ( 

	
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
	
	DECLARE v_countInstrumentosNuevos,v_countInstrumentosActualizados BIGINT;
	DECLARE v_IdPaisChile 	BIGINT;
	DECLARE v_IdPaisUSA 	BIGINT;
	DECLARE v_IdBolsa NVARCHAR(3) := 'BCS';
	DECLARE v_Fecha DATE := CURRENT_DATE;
	SELECT "IdPais" INTO v_IdPaisChile FROM "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL';
	SELECT "IdPais" INTO v_IdPaisUSA FROM "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'US';
	
	v_PersonasNuevas =  SELECT 	"RazonSocial","Rut",
								"BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL as "IdPersona",
								"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL as "IdDocumentoIdentidad",
								'ACT' as "IdEstado",'J' AS "Tipo",'' AS "Nombres",'' AS "ApellidoPaterno",'' AS "ApellidoMaterno",:v_IdPaisChile as "IdPaisNacimiento",
						:v_IdPaisChile as "IdPaisNacionalidadPrincipal",:v_IdPaisChile as "IdPaisResidencia",'0001-01-01' as "FechaNacimiento",'' as "Sexo",NULL as "EstadoCivil",null as "IdDireccionFisicaPrincipal",null as "IdDireccionElectronicaPrincipal",
						null AS "IdTelefonoPrincipal",CURRENT_UTCTIMESTAMP as "FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",CURRENT_UTCTIMESTAMP as "FechaModificacion",'S' AS "EsEnrolado",0 as "IdUsuarioCreador"
						FROM (
							SELECT TRIM(CF."SOC_RUT_ADMIN") as "Rut",CASE TRIM(CF."SOC_NOM_ADMIN") WHEN '' THEN TRIM(CF."CF_NEMO_SASA") ELSE TRIM(CF."SOC_NOM_ADMIN") END AS "RazonSocial",ROW_NUMBER() OVER (PARTITION BY TRIM(CF."SOC_RUT_ADMIN") ORDER BY TRIM(CF."CF_NEMO_SASA")) as "Number"
							FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosCuotasFondo" CF
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON LEFT(PER."DocumentoIdentidad",LENGTH(PER."DocumentoIdentidad")-2) = left(TRIM(CF."SOC_RUT_ADMIN"),length(TRIM(CF."SOC_RUT_ADMIN"))-2) AND PER."TipoDocumentoIdentidad" = 'R' AND TRIM(CF."SOC_RUT_ADMIN") != ''
							WHERE PER."IdPersona" IS NULL AND TRIM(CF."SOC_RUT_ADMIN") != ''
						) 
						
						WHERE "Number" = 1;
	
	SELECT 'v_PersonasNuevas0',* FROM :v_PersonasNuevas;
	
	INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona"("IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial","IdPaisNacimiento",
				"IdPaisNacionalidadPrincipal","IdPaisResidencia" ,"FechaNacimiento","Sexo","EstadoCivil","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal",
				"IdTelefonoPrincipal","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador")
	SELECT "IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial","IdPaisNacimiento",
				"IdPaisNacionalidadPrincipal","IdPaisResidencia" ,"FechaNacimiento","Sexo","EstadoCivil","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal",
				"IdTelefonoPrincipal","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador"
	FROM :v_PersonasNuevas;
	
	INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"("IdDocumentoIdentidad","IdPersona","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdEstadoAutorizacion","FechaIngreso",
					"FechaCreacion","FechaModificacion","IdUsuarioCreador", "Predeterminado")
	SELECT "IdDocumentoIdentidad","IdPersona",'R' as "IdTipoDocumentoIdentidad","Rut" AS "DocumentoIdentidad",'ING',CURRENT_UTCTIMESTAMP as "FechaIngreso",
			CURRENT_UTCTIMESTAMP as "FechaCreacion",CURRENT_UTCTIMESTAMP as "FechaModificacion",0 as "IdUsuarioCreador", 'S' AS "Predeterminado"					
	FROM	:v_PersonasNuevas;
	
	INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio" ,"IdPersona" ,"IdEmpresa") 
	SELECT	"BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL as "IdSocioDeNegocio",per."IdPersona",emp."IdEmpresa"
	FROM	:v_PersonasNuevas per,"BSCL.Plataforma.BD::PLA.Empresa" emp;
	
	INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca"( "IdPersona", "IdEstado", "IdClasificacionFatca", "IdClasificacionEmpresaFatca", "NumeroGIIN", "FechaFirmaFormulario",
	"AutorizacionInformarOrganismo", "ExcepcionResidente", "ExcepcionNacimiento", "ExcepcionDomicilio", "ExcepcionTelefono", "ExcepcionOrdenPermanente", "ExcepcionFirma",
	"FechaIngreso", "FechaCreacion", "FechaModificacion", "Borrado", "IdUsuarioCreador", "IdUsuarioModificador", "ParticipacionSociedadesUSA", "OrdenPermanente", "IdCarga", 
	"NACEEUU", "NACEEUUExplicacion", "EntidadFinanciera", "TipoInstitucionFinanciera", "EntidadGestionada", "IngresosPasivos", "GenerarIngresosPasivos", "CategoriaEntidad",
	"GIINEntidad", "RazonSocial", "FechaLiquidacion", "NombreMercado", "FechaConstitucion", "ObligacionesFiscales", "ObligacionesFiscalesExplicacion", "IndividuoControl")
	SELECT Per."IdPersona",'ACT' AS "IdEstado", 'PENDIENTE' AS "IdClasificacionFatca", CASE WHEN Per."Tipo" = 'J' THEN 'PENDIENTE' ELSE '' END "IdClasificacionEmpresaFatca", '' AS "NumeroGIIN", 
	TO_TIMESTAMP('0001-01-01 00:00:00') AS "FechaFirmaFormulario", 'N' AS "AutorizacionInformarOrganismo", CASE WHEN :v_IdPaisUSA = Per."IdPaisResidencia" THEN 'S' ELSE 'N' END "ExcepcionResidente", 
	CASE WHEN :v_IdPaisUSA = Per."IdPaisNacimiento" THEN 'S' ELSE 'N' END "ExcepcionNacimiento",'N' AS "ExcepcionDomicilio", 'N' AS "ExcepcionTelefono", 'N' AS "ExcepcionOrdenPermanente", 
	'N' AS "ExcepcionFirma", CURRENT_UTCTIMESTAMP AS "FechaIngreso", CURRENT_UTCTIMESTAMP AS "FechaCreacion", '0001-01-01' AS  "FechaModificacion", 'N' AS "Borrado",
	0 AS "IdUsuarioCreador", 0 AS "IdUsuarioModificador", 'N' AS "ParticipacionSociedadesUSA", 'N' AS "OrdenPermanente",0 AS "IdCarga", '' AS "NACEEUU", '' AS "NACEEUUExplicacion", 
	'' AS "EntidadFinanciera", '' AS "TipoInstitucionFinanciera",'' AS "EntidadGestionada", '' AS "IngresosPasivos", '' AS "GenerarIngresosPasivos", '' AS "CategoriaEntidad",
	'' AS "GIINEntidad", '' AS "RazonSocial",'0001-01-01' AS "FechaLiquidacion", '' AS "NombreMercado",'0001-01-01' AS "FechaConstitucion", '' AS "ObligacionesFiscales", 
	'' AS "ObligacionesFiscalesExplicacion", '' as "IndividuoControl"
	FROM :v_PersonasNuevas as Per;
	
	--------------------------------------------------------------------------------------------------------------------------------------------------
	--EMISORES NUEVOS

	v_EmisoresNuevos1 = 	SELECT "IdEmisor","CodigoEmisor","CodigoRegulador","Regulador","EmiteIIF","EmiteIRF","EmiteFungible","ValorClasificacionRiesgoIIF","IndicadorAGF","IdEstado","Rut"
							FROM (
								SELECT ROW_NUMBER() OVER (PARTITION BY "IdEmisor" ORDER BY "IdEmisor","CodigoEmisor" DESC) as "Number",*
								FROM (
						
									SELECT 
										CASE 
											WHEN COALESCE(RTRIM(LTRIM(CF."SOC_NOM_ADMIN")), '') <> '' 
												THEN RTRIM(LTRIM(CF."SOC_NOM_ADMIN"))
											ELSE TRIM(CF."CF_NEMO_SASA")
										END AS "IdEmisor",
										'' as "CodigoEmisor",
										'' AS "CodigoRegulador",
										'SVS' AS "Regulador",
										'N' AS "EmiteIIF",
										'N' AS "EmiteIRF",
										'N' AS "EmiteFungible",
										'N' AS "ValorClasificacionRiesgoIIF",
										'N' as "IndicadorAGF",
										'ACT' AS "IdEstado",
										TRIM(CF."SOC_RUT_ADMIN") as "Rut"
								
										FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosCuotasFondo" CF
										LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI 
										ON EMI."IdEmisor" = CASE 
																WHEN COALESCE(RTRIM(LTRIM(CF."SOC_NOM_ADMIN")), '') <> '' 
																	THEN RTRIM(LTRIM(CF."SOC_NOM_ADMIN"))
																ELSE TRIM(CF."CF_NEMO_SASA")
															END
										WHERE EMI."IdEmisor" IS NULL 
								  		AND CASE 
											WHEN COALESCE(RTRIM(LTRIM(CF."SOC_NOM_ADMIN")), '') <> '' 
												THEN RTRIM(LTRIM(CF."SOC_NOM_ADMIN"))
											ELSE TRIM(CF."CF_NEMO_SASA")
									  	END <> '' AND TRIM(CF."SOC_RUT_ADMIN") != ''
										ORDER BY 
											CASE 
												WHEN COALESCE(RTRIM(LTRIM(CF."SOC_NOM_ADMIN")), '') <> '' 
													THEN RTRIM(LTRIM(CF."SOC_NOM_ADMIN"))
												ELSE TRIM(CF."CF_NEMO_SASA")
											END
								)
							) 
						WHERE "Number" = 1 order by "IdEmisor";
							
	--CALCULAMOS PERSONA QUE LE CORRESPONDE AL EMISOR					
	v_EmisoresNuevos2 = SELECT "Number","Rut",EMI."IdEmisor",EMI."CodigoEmisor",EMI."CodigoRegulador",EMI."Regulador",EMI."EmiteIIF",EMI."EmiteIRF",EMI."EmiteFungible",EMI."ValorClasificacionRiesgoIIF",EMI."IndicadorAGF",EMI."IdEstado",EMI."IdPersona"
						FROM (
							SELECT EMI.*,PER."IdPersona",ROW_NUMBER() OVER (PARTITION BY "IdEmisor" ORDER BY "Rut" DESC) as "Number"
							FROM :v_EmisoresNuevos1 EMI
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON 
							PER."DocumentoIdentidad" = EMI."Rut" AND PER."TipoDocumentoIdentidad" = 'R'
						) AS EMI WHERE "Number" = 1
						;

	INSERT INTO "BSCL.Plataforma.BD::MER.Emisor"("IdEmisor","CodigoEmisor","CodigoRegulador","Regulador","EmiteIIF","EmiteIRF","EmiteFungible","ValorClasificacionRiesgoIIF",
	"IndicadorAGF","IdPersona","IdEstado","FechaActualizacion")
	SELECT "IdEmisor","CodigoEmisor","CodigoRegulador","Regulador","EmiteIIF","EmiteIRF","EmiteFungible","ValorClasificacionRiesgoIIF",
	"IndicadorAGF","IdPersona","IdEstado",CURRENT_TIMESTAMP AS "FechaActualizacion"
	FROM :v_EmisoresNuevos2;
	

	--1 IDENTIFICA INSTRUMENTOS EXISTENTES Y NUEVOS
	v_Instrumentos = SELECT DISTINCT COALESCE(INS."IdInstrumento",0) as "IdInstrumento",TRIM(CF."RV_NEMO") as "Nemotecnico",
						TRIM(CF."RV_CODIGO_ISIN") as "ISIN",'S' AS "EstaInscrito",0 as "CorteMinimo",'N' as "EsDivisible",'' as "Lista",
						'0001-01-01' AS "FechaVencimiento",0 AS "IdInstrumentoRelacionado",'' AS "IdTipoReajuste",
						'N' AS "Reajustable",
						0 as "Tamano",'0001-01-01' as "FechaInicioVigencia",0 as "TasaFiscal",'N' as "IndArt104",'' as "TipoValorizacion",0 as "Tasa",
						0 as "Precio",'P' as "ValorizaPor",'N' as "TieneCupon",'' as "Serie",'N' as "IndicadorBolson",
						CASE CF."RV_MONEDA"
						WHEN '0' THEN 'CLP'
						WHEN '1' THEN 'USD'
						WHEN '2' THEN 'USD'
						WHEN '9' THEN TRIM(CF."RV_MONEDA_EX")
						ELSE 'CLP' END AS "IdMoneda",
						
						CASE 

						WHEN TRIM(CF."RV_MERCADO") = 'CF' AND LEFT(TRIM(CF."CF_IND_INSTRU"),1) = 'O' THEN 'OSA'

						WHEN TRIM(CF."RV_MERCADO") = 'CF' AND TRIM(CF."RV_BOLSA") = 'O' THEN 'CFX'
						WHEN TRIM(CF."RV_MERCADO") = 'CF' AND TRIM(CF."RV_BOLSA") = '' THEN 'CFI'
						
						ELSE 'ORV'		
						END AS "IdTipoInstrumento",
						
						CASE 

						WHEN TRIM(CF."RV_MERCADO") = 'CF' AND TRIM(CF."RV_BOLSA") = 'O' THEN 'CFX'
						WHEN TRIM(CF."RV_MERCADO") = 'CF' AND TRIM(CF."RV_BOLSA") = '' THEN 'CFI'
						
						ELSE 'ORV'		
						END AS "IdTipoInstrumentoOrigen",
						
						COALESCE(pais."IdPais",0) as "IdPaisEmisor",
						
						COALESCE(EMI."IdEmisor",'OPTIMUS') as "IdEmisor",
						
						CASE CF."RV_IND_LIQ_DCV" WHEN 'S' THEN 'S' ELSE 'N' END AS "IndicadorLiquidaDCV",
						CF."RV_LISTA" AS "ValorClasificacionRiesgo",'BCS' AS "IdEntidadClasificadoraRiesgo",
						CASE TRIM(CF."RV_AUT_VT_CORTO") WHEN 'S' THEN 'S' ELSE 'N' END AS "AutorizaVentaCorta",
						CASE TRIM(CF."CF_AUT_OPE_SIMUL") WHEN 'S' THEN 'S' ELSE 'N' END AS "AutorizaSimultanea",
						CF."RV_PRE_APE_DI_AC" as "PrecioAperturaDiaActual",
						
						CASE CF."CF_ESTADO" WHEN 1 THEN 'ACT' WHEN 3 THEN 'ACT' WHEN NULL THEN 'INA' ELSE 'INA' END as "IdEstado",
						CF."CF_FEC_INSCRIP" as "FechaEmision",
						CF."CF_RAZON_SOCIAL" AS "RazonSocial",
						CF."CF_NUM_DEC_PRE" AS "DecimalesPrecio",
						CF."CF_CUO_CIR_ME_AC" AS "NumeroCuotasCirculacionMesActual",
						CF."CF_PRE_CIE_DI_AC" AS "PrecioCierreDiaActual",CF."CF_PRE_MAY_DI_AC" AS "PrecioMayorDiaActual",
						CF."CF_PRE_MEN_DI_AC" AS "PrecioMenorDiaActual",CF."CF_PRE_MED_DI_AC" AS "PrecioMedioDiaActual",
						CF."CF_PRE_ULT_DI_AC" AS "UltimoPrecioDiaActual",CF."CF_UNI_TRA_DI_AC" AS "UnidadesTransadasDiaActual",
						CF."CF_MONTO_DI_AC" AS "MontoTransadoDiaActual",CF."CF_MONTO_ME_AC" AS "MontoTransadoMesActual",
						CF."CF_PRE_CIE_DI_AN" AS "PrecioCierreDiaAnterior",
						
						CF."CF_MONTO_AN_AC" AS "MontoTransadoAgnoActual",CF."CF_MONTO_DI_AN" AS "MontoTransadoDiaAnterior",
						CF."CF_PRE_MAY_AN_AC" AS "PrecioMayorAgnoActual",CF."CF_PRE_MAY_DI_AN" AS "PrecioMayorDiaAnterior",
						CF."CF_PRE_MAY_ME_AC" AS "PrecioMayorMesActual",CF."CF_PRE_MED_AN_AC" AS "PrecioMedioAgnoActual",
						CF."CF_PRE_MED_DI_AN" AS "PrecioMedioDiaAnterior",CF."CF_PRE_MED_ME_AC" AS "PrecioMedioMesActual",
						CF."CF_PRE_MEN_AN_AC" AS "PrecioMenorAgnoActual",CF."CF_PRE_MEN_DI_AN" AS "PrecioMenorDiaAnterior",
						CF."CF_PRE_MEN_ME_AC" AS "PrecioMenorMesActual",CF."CF_UNI_TRA_DI_AN" AS "UnidadesTransadasDiaAnterior",
						0 as "VariacionDiaria",
						
						CF."CF_PRESEN_AJU" AS "PresenciaBursatilAjustada",
						
						CF."RV_RENT_7DIAS" AS "Rentabilidad7Dias",
						CF."RV_RENT_30DIAS" AS "Rentabilidad30Dias",
						CF."RV_RENT_7DIAS" AS "Rentabilidad52Sem",
						TRIM(CF."SOC_RUT") AS "DocumentoIdentidadFondo"
					FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosCuotasFondo" CF
					LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" pais on pais."CodigoPais" = CASE WHEN TRIM(CF."RV_PAIS_EMISOR") = '' THEN 'CL' ELSE TRIM(CF."RV_PAIS_EMISOR") END
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on INS."Nemotecnico" = TRIM(CF."RV_NEMO")
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS on TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON EMI."IdEmisor" =  CASE WHEN COALESCE(RTRIM(LTRIM(CF."SOC_NOM_ADMIN")), '') <> '' THEN RTRIM(LTRIM(CF."SOC_NOM_ADMIN")) ELSE TRIM(CF."CF_NEMO_SASA") END
					WHERE COALESCE(TINS."IdClasificacionTipoInstrumento", '') = CASE WHEN COALESCE(TINS."IdClasificacionTipoInstrumento", '') <> '' THEN 'RV' ELSE '' END;
	
	v_IdInstrumento = SELECT "Nemotecnico","BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as "IdInstrumento"
							FROM :v_Instrumentos ins
							WHERE ins."IdInstrumento" = 0;
	

	
	INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento"("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial",
	"FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdTipoInstrumentoOrigen",
	"IdEstado","DecimalesPrecio","Tamano","FechaInicioVigencia",
	"TasaFiscal","IndArt104","TipoValorizacion","Tasa","Precio","ValorizaPor","TieneCupon",
	"IdPaisEmisor","IndicadorLiquidaDCV","Serie","IndicadorBolson", "PresenciaBursatilAjustada","Rentabilidad7Dias","Rentabilidad30Dias","Rentabilidad52Sem", "DocumentoIdentidadFondo")
	SELECT IDINS."IdInstrumento",INS."Nemotecnico",INS."ISIN",INS."CorteMinimo",INS."EstaInscrito",INS."EsDivisible",INS."RazonSocial",
	INS."FechaEmision",INS."FechaVencimiento",INS."IdInstrumentoRelacionado",INS."IdEmisor",INS."IdTipoReajuste",INS."Reajustable",INS."IdMoneda",INS."IdTipoInstrumento",INS."IdTipoInstrumentoOrigen",
	INS."IdEstado",INS."DecimalesPrecio",INS."Tamano",INS."FechaInicioVigencia",
	INS."TasaFiscal",INS."IndArt104",INS."TipoValorizacion",INS."Tasa",INS."Precio",INS."ValorizaPor",INS."TieneCupon",
	INS."IdPaisEmisor",INS."IndicadorLiquidaDCV",INS."Serie",INS."IndicadorBolson",INS."PresenciaBursatilAjustada",
	INS."Rentabilidad7Dias",INS."Rentabilidad30Dias",INS."Rentabilidad52Sem", INS."DocumentoIdentidadFondo"
	FROM :v_Instrumentos INS
	INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
	WHERE INS."IdInstrumento" = 0;

	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumento"("IdBolsaInstrumento", "IdInstrumento","IdBolsa")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento".NEXTVAL AS "IdBolsaInstrumento",IDINS."IdInstrumento",'BCS'
	FROM :v_Instrumentos INS
	INNER JOIN :v_IdInstrumento IDINS ON INS."Nemotecnico" = IDINS."Nemotecnico"
	WHERE INS."IdInstrumento" = 0;
	
	-- QUERY PARA CORREGIR INSTRUMENTOS EN SU TOTALIDAD
	UPDATE INS
	SET
		"IdPaisEmisor" 						= v_INS."IdPaisEmisor",
		"IdTipoInstrumentoOrigen" 			= v_INS."IdTipoInstrumentoOrigen",
		--"IdMoneda" 							= v_INS."IdMoneda",
		"ISIN" 								= v_INS."ISIN",
		"IndicadorLiquidaDCV"				= v_INS."IndicadorLiquidaDCV",
		"CorteMinimo" 						= v_INS."CorteMinimo",
		"IndArt104"	 						= v_INS."IndArt104",
		"RazonSocial"						= v_INS."RazonSocial",
		"FechaEmision"						= v_INS."FechaEmision",
		"IdEstado" 							= v_INS."IdEstado",
		"DecimalesPrecio"					= v_INS."DecimalesPrecio",
		"PresenciaBursatilAjustada"			= v_INS."PresenciaBursatilAjustada",
		"Rentabilidad7Dias"					= CASE WHEN v_INS."Rentabilidad7Dias" = 0 THEN INS."Rentabilidad7Dias" ELSE v_INS."Rentabilidad7Dias" END,
		"Rentabilidad30Dias"				= CASE WHEN v_INS."Rentabilidad30Dias" = 0 THEN INS."Rentabilidad30Dias" ELSE v_INS."Rentabilidad30Dias" END,
		"Rentabilidad52Sem"					= CASE WHEN v_INS."Rentabilidad52Sem" = 0 THEN INS."Rentabilidad52Sem" ELSE v_INS."Rentabilidad52Sem" END
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS on TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TINS."IdClasificacionTipoInstrumento" = 'RV'
	WHERE v_INS."IdInstrumento" > 0;
	
	
	/*UPDATE INS
	SET
		"IdEstado" 							= v_INS."IdEstado"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
	WHERE v_INS."IdInstrumento" > 0;*/
	
	UPSERT "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"("IdInstrumento","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT ins."IdInstrumento",ins."IdEntidadClasificadoraRiesgo",ins."ValorClasificacionRiesgo" 
	FROM :v_instrumentos ins
	WHERE ins."ValorClasificacionRiesgo" != '';
	
	--PRECIO PARA INSTRUMENTOS EXISTENTES, PERO SIN REGISTRO DE PRECIO
	INSERT INTO "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ("IdInstrumento","IdBolsa","Fecha","FechaActualizacionArchivo","PrecioApertura","PrecioCierre",
	"PrecioMayor","PrecioMenor","PrecioMedio","PrecioUltimaTransaccion","UnidadesTransadas","MontoTransado")
	SELECT v_INS."IdInstrumento",:v_IdBolsa AS "IdBolsa",
	:v_Fecha AS "Fecha",
	CURRENT_TIMESTAMP AS "FechaActualizacionArchivo",
	v_INS."PrecioAperturaDiaActual" as "PrecioApertura",
	v_INS."PrecioCierreDiaActual" AS "PrecioCierre",
	v_INS."PrecioMayorDiaActual" AS "PrecioMayor",
	v_INS."PrecioMenorDiaActual" AS "PrecioMenor",
	v_INS."PrecioMedioDiaActual" AS "PrecioMedio",
	v_INS."UltimoPrecioDiaActual" AS "PrecioUltimaTransaccion",
	v_INS."UnidadesTransadasDiaActual" AS "UnidadesTransadas" ,
	v_INS."MontoTransadoDiaActual" AS "MontoTransado"
	FROM :v_Instrumentos v_INS
	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" BIP ON BIP."IdInstrumento" = v_INS."IdInstrumento" and BIP."IdBolsa" = :v_IdBolsa and BIP."Fecha" = :v_Fecha
	WHERE v_INS."IdInstrumento" > 0 AND BIP."IdInstrumento" IS NULL;
	
	UPDATE BIP
	SET	"FechaActualizacionArchivo" = CURRENT_TIMESTAMP,
		"PrecioApertura"			= v_INS."PrecioAperturaDiaActual",
		"PrecioCierre"				= v_INS."PrecioCierreDiaActual",
		"PrecioMayor"				= v_INS."PrecioMayorDiaActual",
		"PrecioMenor"				= v_INS."PrecioMenorDiaActual",
		"PrecioMedio"				= v_INS."PrecioMedioDiaActual",
		"PrecioUltimaTransaccion"	= v_INS."UltimoPrecioDiaActual",
		"MontoTransado" 			= v_INS."MontoTransadoDiaActual",
		"UnidadesTransadas" 		= v_INS."UnidadesTransadasDiaActual"
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
	SELECT pins."IdInstrumento",:v_IdBolsa as "IdBolsa",
	"MontoTransadoAgnoActual","MontoTransadoDiaActual","MontoTransadoDiaAnterior","MontoTransadoMesActual","PrecioCierreDiaActual","PrecioCierreDiaAnterior",
	"PrecioMayorAgnoActual","PrecioMayorDiaActual","PrecioMayorDiaAnterior","PrecioMayorMesActual",
	"PrecioMedioAgnoActual","PrecioMedioDiaActual","PrecioMedioDiaAnterior","PrecioMedioMesActual",
	"PrecioMenorAgnoActual","PrecioMenorDiaActual","PrecioMenorDiaAnterior","PrecioMenorMesActual",				
	"UltimoPrecioDiaActual","UnidadesTransadasDiaActual","UnidadesTransadasDiaAnterior","VariacionDiaria"
	FROM :v_Instrumentos pins
	WHERE pins."IdInstrumento" > 0;		
	
	--AUTORIZACION VENTA CORTA
	--BORRAR INSTRUMENTOS QUE YA NO ESTAN AUTORIZADOS A OPERAR VENTA CORTA
	DELETE FROM "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"
	WHERE "IdTipoInstrumento"=  'PRT' AND "IdBolsa" = 'BCS' AND "IdInstrumento" IN
	(
		SELECT "IdInstrumento" 
		FROM :v_Instrumentos INS
		WHERE "AutorizaVentaCorta" = 'N'
	);
	
	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdInstrumento")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente".NEXTVAL,'PRT' AS "IdTipoInstrumento",'BCS' AS "IdBolsa",INS."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",INS."IdInstrumento" as "IdInstrumento"
	FROM :v_Instrumentos INS
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" sub on INS."IdInstrumento" = sub."IdInstrumento" and sub."IdBolsa" = 'BCS' AND sub."IdTipoInstrumento" = 'PRT'
	WHERE sub."IdInstrumento" IS NULL AND "AutorizaVentaCorta" = 'S';
	
	--AUTORIZACION SIMULTANEA
	--BORRAR INSTRUMENTOS QUE YA NO ESTAN AUTORIZADOS A OPERAR SIMULTANEA
	DELETE FROM "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"
	WHERE "IdTipoInstrumento"=  'SIM' AND "IdBolsa" = 'BCS' AND "IdInstrumento" IN
	(
		SELECT "IdInstrumento" 
		FROM :v_Instrumentos INS
		WHERE "AutorizaSimultanea" = 'N'
	);
	
	INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdInstrumento")
	SELECT "BSCL.Plataforma.BD.Secuencias::INS.InstrumentoSubyacente".NEXTVAL,'SIM' AS "IdTipoInstrumento",'BCS' AS "IdBolsa",INS."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",INS."IdInstrumento" as "IdInstrumento"
	FROM :v_Instrumentos INS
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" sub on INS."IdInstrumento" = sub."IdInstrumento" and sub."IdBolsa" = 'BCS' AND sub."IdTipoInstrumento" = 'SIM'
	WHERE sub."IdInstrumento" IS NULL AND "AutorizaSimultanea" = 'S';
	
	IF (:p_limpiaTablas = 'S') THEN
		DELETE FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosAcciones";
	END IF;
	
	SELECT COUNT(1) INTO v_countInstrumentosNuevos FROM :v_Instrumentos INS WHERE INS."IdInstrumento" = 0;
	SELECT COUNT(1) INTO v_countInstrumentosActualizados FROM :v_Instrumentos INS WHERE INS."IdInstrumento" > 0;
	
	p_out_Resultado = 'EXITO';
	p_out_Mensaje = 'INSTRUMENTOS NUEVOS: ' || :v_countInstrumentosNuevos || ' INSTRUMENTO ACTUALIZADOS: ' || :v_countInstrumentosActualizados;
	
	
END;
