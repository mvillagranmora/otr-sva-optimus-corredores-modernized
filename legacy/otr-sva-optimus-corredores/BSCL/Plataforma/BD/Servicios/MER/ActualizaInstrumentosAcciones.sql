SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosAcciones";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosAcciones" ( 

	
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
	DECLARE v_IdPaisChile 	BIGINT;
	DECLARE v_IdPaisUSA 	BIGINT;
	DECLARE v_countInstrumentosNuevos,v_countInstrumentosActualizados BIGINT;
	DECLARE v_Fecha DATE := CURRENT_DATE;
	
	DECLARE v_IdBolsa NVARCHAR(3) := 'BCS';
	SELECT "IdPais" INTO v_IdPaisChile FROM "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL';
	SELECT "IdPais" INTO v_IdPaisUSA FROM "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'US';
	
	--EMISORES QUE NO TIENEN NI PERSONA
	v_PersonasNuevas =  SELECT 	"RazonSocial","Rut",
								"BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL as "IdPersona",
								"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL as "IdDocumentoIdentidad",
								'ACT' as "IdEstado",'J' AS "Tipo",'' AS "Nombres",'' AS "ApellidoPaterno",'' AS "ApellidoMaterno",:v_IdPaisChile as "IdPaisNacimiento",
						:v_IdPaisChile as "IdPaisNacionalidadPrincipal",:v_IdPaisChile as "IdPaisResidencia",'0001-01-01' as "FechaNacimiento",'' as "Sexo",NULL as "EstadoCivil",null as "IdDireccionFisicaPrincipal",null as "IdDireccionElectronicaPrincipal",
						null AS "IdTelefonoPrincipal",CURRENT_UTCTIMESTAMP as "FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",CURRENT_UTCTIMESTAMP as "FechaModificacion",'S' AS "EsEnrolado",0 as "IdUsuarioCreador"
						FROM (
							SELECT TRIM(AC."SOC_RUT") as "Rut",CASE TRIM(AC."EMI_RAZON_SOCIAL") WHEN '' THEN TRIM(AC."AC_NEMO_SASA") ELSE TRIM(AC."EMI_RAZON_SOCIAL") END AS "RazonSocial",ROW_NUMBER() OVER (PARTITION BY TRIM(AC."SOC_RUT") ORDER BY TRIM(AC."AC_NEMO_SASA")) as "Number"
							FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosAcciones" AC
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."DocumentoIdentidad" = TRIM(AC."SOC_RUT") AND PER."TipoDocumentoIdentidad" = 'R' AND TRIM(AC."SOC_RUT") != ''
							WHERE PER."IdPersona" IS NULL AND TRIM(AC."SOC_RUT") != ''
						) 
						
						WHERE "Number" = 1;
	
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
						
								SELECT CASE TRIM("EMI_EMISOR") WHEN '' THEN TRIM("AC_NEMO_SASA") ELSE TRIM("EMI_EMISOR") END AS "IdEmisor",COALESCE(TRIM("EMI_COD_EMI"),'') as "CodigoEmisor",COALESCE(TRIM("EMI_COD_SVS"),'') AS "CodigoRegulador",
									'SVS' AS "Regulador",COALESCE(TRIM("EMI_IND_IIF"),'N') AS "EmiteIIF",COALESCE(TRIM("EMI_IND_IRF"),'N') AS "EmiteIRF",COALESCE(TRIM("EMI_IND_FUNG"),'N') AS "EmiteFungible",
									COALESCE(TRIM("EMI_CLA_RIE_IIF"),'N') AS "ValorClasificacionRiesgoIIF",'N' as "IndicadorAGF",'ACT' AS "IdEstado",TRIM(AC."SOC_RUT") as "Rut"
								
									FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosAcciones" AC
									LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON EMI."IdEmisor" = CASE TRIM("EMI_EMISOR") WHEN '' THEN TRIM("AC_NEMO_SASA") ELSE TRIM("EMI_EMISOR") END
									WHERE EMI."IdEmisor" IS NULL AND TRIM(AC."AC_NEMO_SASA") != '' AND TRIM(AC."SOC_RUT") != ''
									ORDER BY "AC_NEMO_SASA"
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
	v_Instrumentos = SELECT COALESCE(INS."IdInstrumento",0) as "IdInstrumento",TRIM(AC."RV_NEMO") as "Nemotecnico",
						TRIM(AC."RV_CODIGO_ISIN") as "ISIN",'S' AS "EstaInscrito",0 as "CorteMinimo",'N' as "EsDivisible",'' as "Lista",
						'0001-01-01' AS "FechaVencimiento",0 AS "IdInstrumentoRelacionado",'' AS "IdTipoReajuste",
						'N' AS "Reajustable",
						0 as "Tamano",'0001-01-01' as "FechaInicioVigencia",0 as "TasaFiscal",'N' as "IndArt104",'' as "TipoValorizacion",0 as "Tasa",
						0 as "Precio",'P' as "ValorizaPor",'N' as "TieneCupon",'' as "Serie",'N' as "IndicadorBolson",
						CASE AC."RV_MONEDA"
						WHEN '0' THEN 'CLP'
						WHEN '1' THEN 'USD'
						WHEN '2' THEN 'USD'
						WHEN '9' THEN CASE TRIM(AC."RV_MONEDA_EX") WHEN 'MXM' THEN 'MXN' ELSE TRIM(AC."RV_MONEDA_EX") END --mxn esta malo en sebra
						ELSE 'CLP' END AS "IdMoneda",
						
						CASE 
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND LEFT(TRIM(AC."AC_NUM_MEN_FONOB"),1) = 'O' THEN 'OSA'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_BOLSA") = 'O' AND TRIM(AC."RV_BOLSA_EX") = 'BCS' AND (TRIM(AC."RV_TIPO_INSTRU") = 'A' OR TRIM(AC."RV_TIPO_INSTRU") = '') THEN 'ACX'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'A' AND TRIM(AC."RV_BOLSA") = 'O' AND TRIM(AC."RV_BOLSA_EX") != 'BCS' THEN 'MIL'

						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'PE' THEN 'MPE'
						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'MX' THEN 'MMX'
						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'CO' THEN 'MCO'

						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'E' AND AC."RV_BOLSA" = 'O' THEN 'ETX'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'E' AND TRIM(AC."RV_BOLSA") = '' THEN 'ETN'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND (TRIM(AC."RV_TIPO_INSTRU") = 'A' OR TRIM(AC."RV_TIPO_INSTRU") = '') AND TRIM(AC."RV_BOLSA") = '' AND TRIM(AC."AC_NUM_MEN_FONOB") != 'O'  THEN 'ACN'


						
						WHEN TRIM(AC."RV_MERCADO") = 'CF' AND LEFT(TRIM(AC."AC_NUM_MEN_FONOB"),1) = 'O' THEN 'OSA'

						WHEN TRIM(AC."RV_MERCADO") = 'CF' AND TRIM(AC."RV_BOLSA") = 'O' THEN 'CFX'
						WHEN TRIM(AC."RV_MERCADO") = 'CF' AND TRIM(AC."RV_BOLSA") = '' THEN 'CFI'
						
						
						ELSE 'ORV'		
						END AS "IdTipoInstrumento",
						
						CASE 
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_BOLSA") = 'O' AND TRIM(AC."RV_BOLSA_EX") = 'BCS' AND (TRIM(AC."RV_TIPO_INSTRU") = 'A' OR TRIM(AC."RV_TIPO_INSTRU") = '') THEN 'ACX'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'A' AND TRIM(AC."RV_BOLSA") = 'O' AND TRIM(AC."RV_BOLSA_EX") != 'BCS' THEN 'MIL'

						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'PE' THEN 'MPE'
						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'MX' THEN 'MMX'
						--WHEN AC."RV_MERCADO" = 'AC' AND AC."RV_TIPO_INSTRU" = 'A' AND AC."RV_BOLSA" = 'O' AND AC."RV_BOLSA_EX" != 'BCS' AND AC."RV_PAIS_EMISOR" = 'CO' THEN 'MCO'

						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'E' AND AC."RV_BOLSA" = 'O' THEN 'ETX'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND TRIM(AC."RV_TIPO_INSTRU") = 'E' AND TRIM(AC."RV_BOLSA") = '' THEN 'ETN'
						WHEN TRIM(AC."RV_MERCADO") = 'AC' AND (TRIM(AC."RV_TIPO_INSTRU") = 'A' OR TRIM(AC."RV_TIPO_INSTRU") = '') AND TRIM(AC."RV_BOLSA") = ''  THEN 'ACN'

						WHEN TRIM(AC."RV_MERCADO") = 'CF' AND TRIM(AC."RV_BOLSA") = 'O' THEN 'CFX'
						WHEN TRIM(AC."RV_MERCADO") = 'CF' AND TRIM(AC."RV_BOLSA") = '' THEN 'CFI'
						
						
						ELSE 'ORV'		
						END AS "IdTipoInstrumentoOrigen",
						
						COALESCE(pais."IdPais", 0) as "IdPaisEmisor",
						
						COALESCE(EMI."IdEmisor",'OPTIMUS') AS "IdEmisor",
						
						
						CASE AC."RV_IND_LIQ_DCV" WHEN 'S' THEN 'S' ELSE 'N' END AS "IndicadorLiquidaDCV",
						AC."RV_LISTA" AS "ValorClasificacionRiesgo",'BCS' AS "IdEntidadClasificadoraRiesgo",
						CASE TRIM(AC."RV_AUT_VT_CORTO") WHEN 'S' THEN 'S' ELSE 'N' END AS "AutorizaVentaCorta",
						CASE TRIM(AC."AC_AUT_OPE_SIMUL") WHEN 'S' THEN 'S' ELSE 'N' END AS "AutorizaSimultanea",
						AC."RV_PRE_APE_DI_AC" as "PrecioAperturaDiaActual",
						
						CASE AC."AC_ESTADO" WHEN 1 THEN 'ACT' WHEN 3 THEN 'ACT' WHEN NULL THEN 'INA' ELSE 'INA' END as "IdEstado",
						AC."AC_FEC_INSCRIP" as "FechaEmision",
						TRIM(AC."AC_RAZON_SOCIAL") AS "RazonSocial",
						AC."AC_NUM_DEC_PRE" AS "DecimalesPrecio",
						AC."AC_NUM_ACC_CIR" AS "NumeroAccionesCirculacion",
						
						AC."AC_PRE_CIE_DI_AC" AS "PrecioCierreDiaActual",AC."AC_PRE_MAY_DI_AC" AS "PrecioMayorDiaActual",
						AC."AC_PRE_MEN_DI_AC" AS "PrecioMenorDiaActual",AC."AC_PRE_MED_DI_AC" AS "PrecioMedioDiaActual",
						AC."AC_PRE_ULT_DI_AC" AS "UltimoPrecioDiaActual",AC."AC_UNI_TRA_DI_AC" AS "UnidadesTransadasDiaActual",
						AC."AC_MONTO_DI_AC" AS "MontoTransadoDiaActual",AC."AC_MONTO_ME_AC" AS "MontoTransadoMesActual",
						AC."AC_PRE_CIE_DI_AN" AS "PrecioCierreDiaAnterior",
						
						AC."AC_MONTO_AN_AC" AS "MontoTransadoAgnoActual",AC."AC_MONTO_DI_AN" AS "MontoTransadoDiaAnterior",
						AC."AC_PRE_MAY_AN_AC" AS "PrecioMayorAgnoActual",AC."AC_PRE_MAY_DI_AN" AS "PrecioMayorDiaAnterior",
						AC."AC_PRE_MAY_ME_AC" AS "PrecioMayorMesActual",AC."AC_PRE_MED_AN_AC" AS "PrecioMedioAgnoActual",
						AC."AC_PRE_MED_DI_AN" AS "PrecioMedioDiaAnterior",AC."AC_PRE_MED_ME_AC" AS "PrecioMedioMesActual",
						AC."AC_PRE_MEN_AN_AC" AS "PrecioMenorAgnoActual",AC."AC_PRE_MEN_DI_AN" AS "PrecioMenorDiaAnterior",
						AC."AC_PRE_MEN_ME_AC" AS "PrecioMenorMesActual",AC."AC_UNI_TRA_DI_AN" AS "UnidadesTransadasDiaAnterior",
						AC."AC_VAR_PRE" as "VariacionDiaria",
						
						AC."AC_PRESEN_AJU" as "PresenciaBursatilAjustada",
						
						AC."RV_RENT_7DIAS" AS "Rentabilidad7Dias",
						AC."RV_RENT_30DIAS" AS "Rentabilidad30Dias",
						AC."RV_RENT_7DIAS" AS "Rentabilidad52Sem"

				FROM "BSCL.Plataforma.BD::MER.CargaInstrumentosAcciones" AC
				LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" pais on pais."CodigoPais" = CASE WHEN AC."RV_PAIS_EMISOR" = '' THEN 'CL' ELSE AC."RV_PAIS_EMISOR" END
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on INS."Nemotecnico" = TRIM(AC."RV_NEMO")
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS on TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON EMI."IdEmisor" = TRIM(AC."AC_NEMO_SASA")
				WHERE COALESCE(TINS."IdClasificacionTipoInstrumento", '') = CASE WHEN COALESCE(TINS."IdClasificacionTipoInstrumento", '') <> '' THEN 'RV' ELSE '' END;
	
	
	v_IdInstrumento = SELECT "Nemotecnico","BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as "IdInstrumento"
							FROM :v_Instrumentos ins
							WHERE ins."IdInstrumento" = 0;
	
	
	INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento"("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial",
	"FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdTipoInstrumentoOrigen",
	"IdEstado","DecimalesPrecio","Tamano","FechaInicioVigencia",
	"TasaFiscal","IndArt104","TipoValorizacion","Tasa","Precio","ValorizaPor","TieneCupon",
	"IdPaisEmisor","IndicadorLiquidaDCV","Serie","IndicadorBolson","PresenciaBursatilAjustada","Rentabilidad7Dias","Rentabilidad30Dias","Rentabilidad52Sem")
	SELECT IDINS."IdInstrumento",INS."Nemotecnico",INS."ISIN",INS."CorteMinimo",INS."EstaInscrito",INS."EsDivisible",INS."RazonSocial",
	INS."FechaEmision",INS."FechaVencimiento",INS."IdInstrumentoRelacionado",INS."IdEmisor",INS."IdTipoReajuste",INS."Reajustable",INS."IdMoneda",INS."IdTipoInstrumento",INS."IdTipoInstrumentoOrigen",
	INS."IdEstado",INS."DecimalesPrecio",INS."Tamano",INS."FechaInicioVigencia",
	INS."TasaFiscal",INS."IndArt104",INS."TipoValorizacion",INS."Tasa",INS."Precio",INS."ValorizaPor",INS."TieneCupon",
	INS."IdPaisEmisor",INS."IndicadorLiquidaDCV",INS."Serie",INS."IndicadorBolson", INS."PresenciaBursatilAjustada",
	INS."Rentabilidad7Dias",INS."Rentabilidad30Dias",INS."Rentabilidad52Sem"
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
	
	-- QUERY PARA CORREGIR INSTRUMENTOS EN SU TOTALIDAD
	UPDATE INS
	SET
		"IdPaisEmisor" 						= v_INS."IdPaisEmisor",
		"IdTipoInstrumento" 				= v_INS."IdTipoInstrumento",
		"IdTipoInstrumentoOrigen" 			= v_INS."IdTipoInstrumentoOrigen",
		"IdMoneda" 							= v_INS."IdMoneda",
		"ISIN" 								= v_INS."ISIN",
		"IndicadorLiquidaDCV"				= v_INS."IndicadorLiquidaDCV",
		"CorteMinimo" 						= v_INS."CorteMinimo",
		"RazonSocial"						= v_INS."RazonSocial",
		--"IdEmisor"							= v_INS."IdEmisor",
		"FechaEmision"						= v_INS."FechaEmision",
		"IndArt104"							= v_INS."IndArt104",
		"IdEstado" 							= v_INS."IdEstado",
		"DecimalesPrecio"					= v_INS."DecimalesPrecio",
		"NumeroAccionesCirculacion"			= v_INS."NumeroAccionesCirculacion",
		"PresenciaBursatilAjustada"			= v_INS."PresenciaBursatilAjustada",
		"Rentabilidad7Dias"					= CASE WHEN v_INS."Rentabilidad7Dias" = 0 THEN INS."Rentabilidad7Dias" ELSE v_INS."Rentabilidad7Dias" END,
		"Rentabilidad30Dias"				= CASE WHEN v_INS."Rentabilidad30Dias" = 0 THEN INS."Rentabilidad30Dias" ELSE v_INS."Rentabilidad30Dias" END,
		"Rentabilidad52Sem"					= CASE WHEN v_INS."Rentabilidad52Sem" = 0 THEN INS."Rentabilidad52Sem" ELSE v_INS."Rentabilidad52Sem" END
		--"ValorClasificacionRiesgo"			= v_INS."ValorClasificacionRiesgo"
		--"AutorizaVentaCorta"				= v_INS."AutorizaVentaCorta",
		--"AutorizaSimultanea"				= v_INS."AutorizaSimultanea"
		
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS on TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TINS."IdClasificacionTipoInstrumento" = 'RV'
	WHERE v_INS."IdInstrumento" > 0;
	
	
	/*UPDATE INS
	SET
		"IdEstado" 							= v_INS."IdEstado",
		"NumeroAccionesCirculacion"			= v_INS."NumeroAccionesCirculacion",
		"ValorClasificacionRiesgo"			= v_INS."ValorClasificacionRiesgo"
	FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
	INNER JOIN :v_Instrumentos v_INS ON INS."Nemotecnico" = v_INS."Nemotecnico"
	WHERE v_INS."IdInstrumento" > 0;
	*/
	
	UPSERT "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo"("IdInstrumento","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT ins."IdInstrumento",ins."IdEntidadClasificadoraRiesgo",trim(ins."ValorClasificacionRiesgo")
	FROM :v_instrumentos ins
	WHERE ins."ValorClasificacionRiesgo" != '';
	
	UPDATE "BSCL.Plataforma.BD::INS.BolsaInstrumento"
	SET "IdBolsa" = 'BCS'
	WHERE "IdBolsa" = null or "IdBolsa" = '';
	
	
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
	INNER JOIN :v_Instrumentos v_INS ON BIP."IdInstrumento" = v_INS."IdInstrumento" AND "IdBolsa" = :v_IdBolsa AND BIP."Fecha" = v_Fecha AND v_INS."IdInstrumento" > 0;
	
	
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

