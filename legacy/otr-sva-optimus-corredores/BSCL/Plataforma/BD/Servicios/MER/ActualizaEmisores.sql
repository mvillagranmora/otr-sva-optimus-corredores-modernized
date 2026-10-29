SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaEmisores";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaEmisores" ( 

	IN	p_limpiaTablas						NVARCHAR(1) DEFAULT 'N',
	
	OUT	p_out_Resultado						NVARCHAR(40),
	OUT	p_out_Mensaje						NVARCHAR(520)
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
BEGIN
	
	DECLARE v_countTBUTEMIS BIGINT;
	DECLARE v_IdPaisUSA 	BIGINT;
	DECLARE v_countEmisoresNuevos,v_countEmisoresActualizados BIGINT;
	SELECT "IdPais" INTO v_IdPaisUSA FROM "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'US';
	
	/*v_Emisores = 	SELECT	utemis."EMISOR" AS "IdEmisorUTEMIS",COALESCE(emi."IdEmisor",'') as "IdEmisorOptimus",utemis."COD_EMI" as "CodigoEmisor",
							utemis."COD_SVS" as "CodigoRegulador",'SVS' as "Regulador",utemis."IND_IIF" as "EmiteIIF",utemis."IND_IRF" as "EmiteIRF",
							utemis."IND_FUNG" as "EmiteFungible",utemis."CLA_RIE_IIF" AS "ValorClasificacionRiesgoIIF", 'N' as "IndicadorAGF",COALESCE(per."IdPersona",0) AS "IdPersona",'ACT' AS "IdEstado",
							utemis."RAZON_SOCIAL" as "RazonSocial",utemis."RUT" as "Rut"
					FROM "BSCL.Plataforma.BD::MER.CargaEmisores" utemis
					LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on utemis."EMISOR" = emi."IdEmisor"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."DocumentoIdentidad" = utemis."RUT" AND "TipoDocumentoIdentidad" = 'R';*/
	
	--SELECT 'v_Emisores',* FROM :v_Emisores;
	select '"BSCL.Plataforma.BD::MER.CargaEmisores"',* from "BSCL.Plataforma.BD::MER.CargaEmisores";
	v_EmisoresSinPersona2 = SELECT TRIM(utemis."EMISOR") AS "IdEmisorUTEMIS",TRIM(utemis."RAZON_SOCIAL") as "RazonSocial",TRIM(utemis."RUT") AS "Rut",
							"BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL as "IdPersona",
							"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL as "IdDocumentoIdentidad"
							FROM "BSCL.Plataforma.BD::MER.CargaEmisores" utemis
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."DocumentoIdentidad" = trim(utemis."RUT") AND "TipoDocumentoIdentidad" = 'R'
							WHERE per."IdPersona" IS NULL;
	
	v_EmisoresSinPersona = SELECT *
							FROM :v_EmisoresSinPersona2 
							WHERE trim("Rut") != '' 
                            and   trim("Rut") != '-0'
                            and   "IdEmisorUTEMIS" not in ('BBMORGAN','BBESPANOL','BBPACIFICO');
	
	
	select 'v_EmisoresSinPersona',* from :v_EmisoresSinPersona;
	
	v_NuevasPersonas = 	SELECT "IdEmisorUTEMIS","IdPersona","Rut" as "DocumentoIdentidad",'ACT' as "IdEstado","IdDocumentoIdentidad",'J' AS "Tipo",'' AS "Nombres",'' AS "ApellidoPaterno",'' AS "ApellidoMaterno","RazonSocial",44 as "IdPaisNacimiento",
						44 as "IdPaisNacionalidadPrincipal",44 as "IdPaisResidencia",'0001-01-01' as "FechaNacimiento",'' as "Sexo",NULL as "EstadoCivil",null as "IdDireccionFisicaPrincipal",null as "IdDireccionElectronicaPrincipal",
						null AS "IdTelefonoPrincipal",CURRENT_UTCTIMESTAMP as "FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",CURRENT_UTCTIMESTAMP as "FechaModificacion",'S' AS "EsEnrolado",0 as "IdUsuarioCreador",
						ROW_NUMBER() OVER (PARTITION BY "Rut" ORDER BY "IdPersona") AS "Prioridad"
						FROM :v_EmisoresSinPersona emi;
	
	select 'v_NuevasPersonas',* from :v_NuevasPersonas;
	
	INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona"("IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial","IdPaisNacimiento",
				"IdPaisNacionalidadPrincipal","IdPaisResidencia" ,"FechaNacimiento","Sexo","EstadoCivil","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal",
				"IdTelefonoPrincipal","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador")
	SELECT "IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno",COALESCE("RazonSocial",'') as "RazonSocial","IdPaisNacimiento",
				"IdPaisNacionalidadPrincipal","IdPaisResidencia" ,"FechaNacimiento","Sexo","EstadoCivil","IdDireccionFisicaPrincipal","IdDireccionElectronicaPrincipal",
				"IdTelefonoPrincipal","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador"
	FROM :v_NuevasPersonas
	where "Prioridad" = 1;
	
	
	
	INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"("IdDocumentoIdentidad","IdPersona","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdEstadoAutorizacion","FechaIngreso",
					"FechaCreacion","FechaModificacion","IdUsuarioCreador", "Predeterminado")
	SELECT "IdDocumentoIdentidad","IdPersona",'R' as "IdTipoDocumentoIdentidad","DocumentoIdentidad",'ING',CURRENT_UTCTIMESTAMP as "FechaIngreso",
			CURRENT_UTCTIMESTAMP as "FechaCreacion",CURRENT_UTCTIMESTAMP as "FechaModificacion",0 as "IdUsuarioCreador", 'S' AS "Predeterminado"				
	FROM	:v_NuevasPersonas
	where "Prioridad" = 1;
	
	
	
	INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio" ,"IdPersona" ,"IdEmpresa") 
	SELECT	"BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL as "IdSocioDeNegocio",per."IdPersona",emp."IdEmpresa"
	FROM	:v_NuevasPersonas per,"BSCL.Plataforma.BD::PLA.Empresa" emp
	where "Prioridad" = 1;
	
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
	FROM :v_NuevasPersonas as Per
	where "Prioridad" = 1;
	
	
	--POR SI HAY MAS DE UN RUT PARA EL EMISOR
	v_EmisorPersona =	SELECT utemis."EMISOR",per."IdPersona", ROW_NUMBER() OVER (PARTITION BY "RUT" ORDER BY per."IdPersona") AS "Prioridad"
						FROM "BSCL.Plataforma.BD::MER.CargaEmisores" utemis
						
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER on PER."DocumentoIdentidad" = TRIM(utemis."RUT") AND "TipoDocumentoIdentidad" = 'R';
	
	
	SELECT 'v_EmisorPersona',* FROM :v_EmisorPersona order by "EMISOR","Prioridad";
	
	v_Emisores = SELECT	trim(utemis."EMISOR") AS "IdEmisorUTEMIS",COALESCE(emi."IdEmisor",'') as "IdEmisorOptimus",trim(utemis."COD_EMI") as "CodigoEmisor",
						trim(utemis."COD_SVS") as "CodigoRegulador",'SVS' as "Regulador",trim(utemis."IND_IIF") as "EmiteIIF",trim(utemis."IND_IRF") as "EmiteIRF",
						trim(utemis."IND_FUNG") as "EmiteFungible",utemis."CLA_RIE_IIF" AS "ValorClasificacionRiesgoIIF", 'N' as "IndicadorAGF",PER."IdPersona" AS "IdPersona",'ACT' AS "IdEstado",
						trim(utemis."RAZON_SOCIAL") as "RazonSocial",trim(utemis."RUT") as "Rut",trim(utemis."TIP_ENT") AS "TipoEntidad"
				FROM "BSCL.Plataforma.BD::MER.CargaEmisores" utemis
				LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on TRIM(utemis."EMISOR") = emi."IdEmisor"
				INNER JOIN :v_EmisorPersona AS PER on trim(PER."EMISOR") = TRIM(utemis."EMISOR")
				where "Prioridad" = 1;		
	
	SELECT 'v_Emisores',* FROM :v_Emisores order by "IdEmisorUTEMIS";
	
	INSERT INTO "BSCL.Plataforma.BD::MER.Emisor"("IdEmisor","CodigoEmisor","CodigoRegulador","Regulador","EmiteIIF","EmiteIRF","EmiteFungible","ValorClasificacionRiesgoIIF",
	"IndicadorAGF","IdPersona","IdEstado","FechaActualizacion","TipoEntidad")
	SELECT "IdEmisorUTEMIS" AS "IdEmisor","CodigoEmisor","CodigoRegulador","Regulador","EmiteIIF","EmiteIRF","EmiteFungible","ValorClasificacionRiesgoIIF",
	"IndicadorAGF","IdPersona","IdEstado",CURRENT_TIMESTAMP AS "FechaActualizacion","TipoEntidad"
	FROM :v_Emisores
	WHERE "IdEmisorOptimus" = '';
	
	UPDATE EMI
	SET "CodigoEmisor" 					= emitemp."CodigoEmisor",
		"CodigoRegulador" 				= emitemp."CodigoRegulador",
		"EmiteIIF" 						= emitemp."EmiteIIF",
		"EmiteIRF" 						= emitemp."EmiteIRF",
		"EmiteFungible" 				= emitemp."EmiteFungible",
		"ValorClasificacionRiesgoIIF" 	= emitemp."ValorClasificacionRiesgoIIF",
		"IdPersona" 					= emitemp."IdPersona",
		"TipoEntidad"					= emitemp."TipoEntidad",
		"FechaActualizacion" 			= CURRENT_TIMESTAMP
	FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
	INNER JOIN :v_Emisores emitemp on EMI."IdEmisor" = emitemp."IdEmisorOptimus"
	WHERE "IdEmisorOptimus" != ''; 
	
	IF (:p_limpiaTablas = 'S') THEN
		DELETE FROM "BSCL.Plataforma.BD::MER.CargaEmisores";
	END IF;
	
	SELECT COUNT(1) INTO v_countEmisoresNuevos FROM :v_Emisores emi WHERE emi."IdEmisorOptimus" = '';
	SELECT COUNT(1) INTO v_countEmisoresActualizados FROM :v_Emisores emi WHERE emi."IdEmisorOptimus" != '';
	
	p_out_Resultado = 'EXITO';
	p_out_Mensaje = 'EMISORES NUEVOS: ' || :v_countEmisoresNuevos || ' EMISORES ACTUALIZADOS: ' || :v_countEmisoresActualizados;
		
END;

