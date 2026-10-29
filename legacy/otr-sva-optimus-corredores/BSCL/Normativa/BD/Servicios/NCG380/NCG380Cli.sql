SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cli";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Cli"
( 
 	IN  p_IdSolicitudCircular 		  BIGINT DEFAULT 0,
	OUT p_OUT_TablaSalida	 		"BSCL.Normativa.BD::NOR.SALIDA_NCG380Cli"   --Se debe llamar siemte p_out_TablaSalida
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	
AS BEGIN

	DECLARE v_Count BIGINT := 0;
    DECLARE v_FechaInicio DATE DEFAULT '0001-01-01';
    DECLARE v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_TodoCliente CHAR(1) DEFAULT 'S';
	DECLARE v_Solo1Cliente BIGINT := -1;
	DECLARE v_DocumentoOficial CHAR(1)  DEFAULT 'S';
    DECLARE v_IdEmpresa INT := 0;
    DECLARE v_SecuenciaTablaGlobal BIGINT DEFAULT 0;
	DECLARE v_FecSistema DATE;
	DECLARE v_SegundosZonaHoraria BIGINT;
	
	--* ------------------------------------------------------------------------------	
    --* PARAMETROS DESDE LA UI EN VARIABLES
	--* ------------------------------------------------------------------------------
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX("FechaInicio"),'0001-01-01') AS DATE),
        CAST(MAX("FechaTermino") AS DATE),
        CAST(COALESCE(MAX("Solo1Cliente"), -1) AS BIGINT),
        CAST(COALESCE(MAX("DocumentoOficial"), 'N') AS CHAR(1))
        INTO v_IdEmpresa, v_FechaInicio, v_FechaTermino, v_Solo1Cliente,v_DocumentoOficial
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino",
            CASE WHEN "TipoParametro"='IDCLI' THEN "Valor" ELSE NULL END AS "Solo1Cliente",
            CASE WHEN "TipoParametro"='CLI_OFICIAL' THEN "Valor" ELSE NULL END AS "DocumentoOficial"
            
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER','IDCLI','CLI_OFICIAL'))
    );

    SELECT 	Top 1 "Valor" 
    INTO	v_FecSistema 
    FROM	"BSCL.Plataforma.BD::PLA.Configuracion" 
    WHERE	"CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = :v_IdEmpresa;	
	
    SELECT "Valor" INTO v_SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = :v_IdEmpresa AND "CodigoParametro" = 'PLA.SegundosZonaHoraria';

	v_FechaTermino = TO_VARCHAR(ADD_SECONDS(v_FechaTermino,:v_SegundosZonaHoraria), 'YYYY-MM-DD');
	--* FILTRO POR CLIENTES (LISTA DESDE ARCHIVO COMO RUT+DV, CUANDO SELECCIONA 1 SOLO VIENE EL IDCLIENTE)
	--* DESDE LA UI LLEGAN RUT, Y TODOS LOS CLIENTES = S => PREVALECE LISTA DE RUT'S
	--* ------------------------------------------------------------------------------	
	v_TablaClientes = 
		SELECT 	NULL AS "Valor", NULL "IdSistema" 
		FROM 	DUMMY;

	IF v_Solo1Cliente > 0 THEN
		v_TodoCliente = 'N';		

	ELSE
		SELECT COUNT(1) INTO v_Count
		FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
		WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'RUT';
		
		IF v_Count > 0 THEN
			v_TodoCliente = 'N';		
			v_TablaClientes = 
				SELECT 	"Valor", "IdSistema" 
				FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
				WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'RUT';
		END IF;
	END IF;



	--* ---------------------------------------------------------------------------
	--* ( 	NUEVOS + MODIFICADOS AL DIA DE HOY + 
	--* 	HISTORICOS QUE ESTEN DENTRO DE LAS FECHAS 
	--* ) 	[ + RUT'S DESDE LA UI ]
	--* ---------------------------------------------------------------------------	
	
	IF v_DocumentoOficial = 'N'	THEN
	
	IF v_FechaTermino = v_FecSistema THEN
		
		--* QUIERE CONSULTAR LA DATA ACTUAL ------------------------------------------------------------------------------------
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline"( IdSecuenciaGlobal => :v_SecuenciaTablaGlobal );
		--* --------------------------------------------------------------------------------------------------------------------

		--* POR MIENTRAS LA VISTA SE CORRIGE (ACTUALMENTE ENTREGA 2 VECES MISMO REGUISTRO)
		v_Tabla_ONLINE = 
			SELECT DISTINCT * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" WHERE "IdClienteOnline" = v_SecuenciaTablaGlobal; 

		p_OUT_TablaSalida =
			--* ---------------------------------------------------------------------------
			WITH LatestFirma AS (
						    SELECT F."IdAnexo", MAX(F."FechaFirma") AS "FechaFirma"
						    FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" F
						    GROUP BY F."IdAnexo")
			SELECT	CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Rut" ELSE "Pasaporte" END AS "RutCodigoCliente", --Se remplaza "IdCliente"  por el Rut , se muestra en la columna Nro de cliente
			        CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Rut" ELSE '' END AS "Rut",
					CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Dig" ELSE '' END AS "Dig",
					CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'P' THEN "Pasaporte" ELSE '' END AS "Pasaporte",
					HOY."Glei", HOY."ApellidoPaterno", HOY."ApellidoMaterno", HOY."PrimerNombre", HOY."SegundoNombre" ,
					CASE WHEN   "COSD"."Tipo"  = 'N' THEN '' ELSE HOY."RazonSocial"END AS "RazonSocial",
					HOY."Telefono", HOY."Email" , HOY."DireccionTipo", HOY."DireccionNombre",   
					HOY."DireccionNumero", HOY."DireccionDepto", HOY."DireccionComuna", HOY."DireccionCiudad", HOY."DireccionEstado",   
					"DireccionPais", "CodigoPostal", "Nacionalidad", "RelacionIntermediario",  CASE WHEN VARADMCAR."ValorIndicadorADMCAR" = 'S' THEN 'S' ELSE 'N' END AS "AdministracionCartera",	 	
					RTRIM(COALESCE("RutEntidadEmpleadora",'0')) AS "RutEntidadEmpleadora", HOY."DigEntidadEmpleadora", HOY."RazonSocialEntidadEmpleadora",	HOY."EstadoCivil", RTRIM(COALESCE("ConyRut",'0')) AS "ConyRut", 
					HOY."ConyDig", HOY."ConyPasaporte", HOY."ConyApellidoPaterno", HOY."ConyApellidoMaterno", HOY."ConyPrimerNombre", 
					HOY."ConySegundoNombre", HOY."ConyRazonSocial", HOY."ConyTelefono", HOY."ConyEmail", HOY."ConyDireccionTipo",	
					HOY."ConyDireccionNombre", HOY."ConyDireccionNumero", HOY."ConyDireccionDepto", HOY."ConyDireccionComuna", HOY."ConyDireccionCiudad",
					HOY."ConyDireccionEstado", HOY."ConyDireccionPais", HOY."ConyCodigoPostal", HOY."ConyNacionalidad", HOY."ConyRelacionIntermediario",
					RTRIM(COALESCE("RepRut",'0')) AS "RepRut", HOY."RepDig", HOY."RepPasaporte", HOY."RepGlei", HOY."RepApellidoPaterno",
					HOY."RepApellidoMaterno", HOY."RepPrimerNombre", HOY."RepSegundoNombre", HOY."RepRazonSocial", HOY."RepTelefono",
					HOY."RepEmail", HOY."RepDireccionTipo", HOY."RepDireccionNombre", HOY."RepDireccionNumero", HOY."RepDireccionDepto",	
					HOY."RepDireccionComuna", HOY."RepDireccionCiudad", HOY."RepDireccionProvincia", HOY."RepDireccionPais", HOY."RepCodigoPostal",
					HOY."RepNacionalidad", HOY."RepRelacionIntermediario", HOY."RepEstadoCivil", RTRIM(COALESCE("ConyRepRut", '0')) AS "ConyRepRut", HOY."ConyRepDig",
					HOY."ConyRepPasaporte",	HOY."ConyRepApellidoPaterno", HOY."ConyRepApellidoMaterno", HOY."ConyRepPrimerNombre", HOY."ConyRepSegundoNombre",	
					HOY."ConyRepRazonSocial", HOY."ConyRepTelefono", HOY."ConyRepEmail", HOY."ConyRepDireccionTipo", HOY."ConyRepDireccionNombre",
					HOY."ConyRepDireccionNumero", HOY."ConyRepDireccionDepto", HOY."ConyRepDireccionComuna", HOY."ConyRepDireccionCiudad", HOY."ConyRepDireccionEstado",
					HOY."ConyRepDireccionPais", HOY."ConyRepCodigoPostal", HOY."ConyRepNacionalidad", HOY."ConyRepRelacionIntermediario", HOY."CuentaBancariaUno",	
					HOY."CuentaBancariaDos", HOY."CuentaBancariaTres", HOY."ClaseOrden", HOY."Perfil", HOY."PerfilAnterior", HOY."FechaInicioPerfilActual", "COSD"."NombreTipoPersona"		AS	"TipoPersona",
 					COALESCE( NULLIF ("IndicadorPersonaJuridica",'') ,"COCLI"."TipoCliente") AS "TipoCliente","COSD"."FechaIngreso" AS "FechaEnrolamiento", "COCLI"."ContratoCondicionesGenerales" AS "ContratoFirmadoNCG30",	
					"ANX"."FechaActivacion" AS "FechaSuscripcion", CASE WHEN "PERF"."Tipo" = 'J' THEN "PERF"."RazonSocial" ELSE "PERF"."Nombres" || ' ' || "PERF"."ApellidoPaterno" || ' ' || "PERF"."ApellidoMaterno" END AS "ConfirmadoPor", 
					"COSD"."NombreEstado" AS "EstadoCliente", "COSD"."ComentarioBloqueo",	"COCLI"."ContratoCustodia",	"COCLI"."ConflictoInteres",	"COCLI"."InfoPrivilegiadaArt171","COCLI"."CargoSVS171",
 					"COCLI"."RelCorredoraArt100", "COCLI"."RelCorredora_NCG380",	"COCLI"."RelCorredora_CIRC1064","COCLI"."Cargo_NCG_68_69","COCLI"."PersonaExpuestaPoliticamente",
 					(CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Rut" ELSE "Pasaporte" END) AS "NumeroContrato"
			FROM	:v_Tabla_ONLINE AS HOY
					INNER JOIN :v_TablaClientes AS CLI ON 
					    COALESCE(CLI."Valor",'') = TRIM("Pasaporte")
						OR v_TodoCliente = 'S' 
						OR v_Solo1Cliente = HOY."NumeroCliente"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" CONSULTAR_CLIENTE
					ON (TRIM("Rut")||'-'||TRIM("Dig") = CONSULTAR_CLIENTE."DocumentoIdentidad")
					OR  TRIM("Pasaporte") = CONSULTAR_CLIENTE."DocumentoIdentidad"
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
			        ON  "Pasaporte" = doc."DocumentoIdentidad"
			      	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "COSD" ON ("COSD"."IdCliente" = HOY."NumeroCliente")
			        LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteNCG380Detallado" AS "COCLI" ON ("COCLI"."IdCliente" = HOY."NumeroCliente")
					LEFT JOIN ( SELECT "IdCliente" AS "IdClientePersonaJuridica", "CodigoValorIndicador" AS "IndicadorPersonaJuridica" FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" WHERE "CodigoTipoIndicador" = 'TCLICMFJ' ) IND ON COCLI."IdCliente" = IND."IdClientePersonaJuridica"
					LEFT JOIN ( SELECT"ValorIndicador" AS "ValorIndicadorADMCAR" , "IdCliente" AS "ClienteADMCART" , ROW_NUMBER() OVER (PARTITION BY "IdCliente" ) AS rn	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"	WHERE   "CodigoTipoIndicador" = 'ADMCART'	AND "ValorIndicador" = 'S') AS VARADMCAR ON VARADMCAR."ClienteADMCART" = "COCLI"."IdCliente" AND VARADMCAR.rn = 1
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" "DOCF" ON "DOCF"."IdTipoDocumento" = 'CONTRATOCONDICIONESGRALES' AND "DOCF"."IdCliente" = HOY."NumeroCliente"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" "ANX" ON "ANX"."IdDocumento" = "DOCF"."IdDocumento" AND "ANX"."IdAnexo" = "DOCF"."IdUltimoAnexo"
					LEFT JOIN LatestFirma "LF" ON "LF"."IdAnexo" = "ANX"."IdAnexo"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" "F" ON "F"."IdAnexo" = "LF"."IdAnexo" AND "F"."FechaFirma" = "LF"."FechaFirma"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" "SF" ON "SF"."IdSolicitudFirmaAnexo" = "F"."IdSolicitudFirmaAnexo"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" "PERF" ON "PERF"."IdPersona" = "SF"."IdPersonaFirmante"					
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL'
			ORDER BY HOY."NumeroCliente" ASC;
	ELSE

		--* ULTIMO REGISTRO DE CLIENTE (ULTIMOS DATOS GUARDADOS)
		v_TablaUtlReg_x_Cli = 
			SELECT "IdCliente", MAX("FechaRegistro") AS "FechaUltimo_Registro_x_Cliente" 
			FROM	"BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" T1 
			WHERE	"FechaRegistro" <= v_FechaTermino
			GROUP BY "IdCliente";

			
	


		p_OUT_TablaSalida =
			--* ---------------------------------------------------------------------------
			WITH LatestFirma AS (
						    SELECT F."IdAnexo", MAX(F."FechaFirma") AS "FechaFirma"
						    FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" F
						    GROUP BY F."IdAnexo")
			SELECT	 CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadRut" ELSE "DocumentoIdentidadPasaporte" END AS "RutCodigoCliente",  --Se remplaza "IdCliente"  por el Rut , se muestra en la columna Nro de cliente
					 CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadRut" ELSE '' END AS "Rut",
					 CASE WHEN  DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadDig" ELSE '' END AS "Dig",
					 CASE WHEN  DOC."IdTipoDocumentoIdentidad" = 'P' THEN "DocumentoIdentidadPasaporte" ELSE '' END AS "Pasaporte",
					HIS."Glei",
					HIS."ApellidoPaterno",
					HIS."ApellidoMaterno",
					HIS."PrimerNombre", 
					HIS."SegundoNombre" ,
					CASE WHEN   "COSD"."Tipo"  = 'N' THEN '' ELSE HIS."RazonSocial" END AS "RazonSocial",
					HIS."Telefono",
					HIS."DireccionElectronica" AS "Email" ,
					HIS."TipoDireccion" AS "DireccionTipo",
					HIS."Calle" AS "DireccionNombre",
					HIS."Numero" AS "DireccionNumero",
					HIS."Departamento" AS "DireccionDepto", 
					HIS."Comuna" AS "DireccionComuna", "Ciudad" AS "DireccionCiudad", 
					HIS."Provincia" AS "DireccionEstado", "Pais" AS "DireccionPais", 
					HIS."CodigoPostal", "Nacionalidad", 
					HIS."RelacionEntidad" AS "RelacionIntermediario", CASE WHEN VARADMCAR."ValorIndicadorADMCAR" = 'S' THEN 'S' ELSE 'N' END AS "AdministracionCartera",
					RTRIM(COALESCE("EmpleadorRut", '0')) AS "RutEntidadEmpleadora", HIS."EmpleadorDig" AS "DigEntidadEmpleadora" , 
					HIS."EmpleadorRazonSocial" AS "RazonSocialEntidadEmpleadora", HIS."EstadoCivil", 
					RTRIM(COALESCE("ConyugeRut", '0')) AS "ConyRut", HIS."ConyugeDig" AS "ConyDig", 
					HIS."ConyugePasaporte" AS "ConyPasaporte", HIS."ConyugeApellPaterno" AS "ConyApellidoPaterno", 
					HIS."ConyugeApellMaterno" AS "ConyApellidoMaterno", HIS."ConyugePrimerNombre" AS "ConyPrimerNombre", 
					HIS."ConyugeSegundoNombre" AS "ConySegundoNombre", HIS."ConyugeRazonSocial" AS "ConyRazonSocial", 
					HIS."ConyugeTelefono" AS "ConyTelefono", HIS."ConyugeDireccionElectronica" AS "ConyEmail", 
					HIS."ConyugeTipoDireccion" AS "ConyDireccionTipo", HIS."ConyugeCalle" AS "ConyDireccionNombre", 
					HIS."ConyugeNumero" AS "ConyDireccionNumero", HIS."ConyugeDepartamento" AS "ConyDireccionDepto", 
					HIS."ConyugeComuna" AS "ConyDireccionComuna", HIS."ConyugeCiudad" AS "ConyDireccionCiudad", 
					HIS."ConyugeProvincia" AS "ConyDireccionEstado", HIS."ConyugePais" AS "ConyDireccionPais", 
					HIS."ConyugeCodigoPostal" AS "ConyCodigoPostal", HIS."ConyugeNacionalidad" AS "ConyNacionalidad", 
					HIS."ConyugeRelacionEntidad" AS "ConyRelacionIntermediario", RTRIM(COALESCE("RepLegalRut", '0')) AS "RepRut",
					HIS."RepLegalDig" AS "RepDig", HIS."RepLegalPasaporte" AS "RepPasaporte",
					HIS."RepLegalGlei" AS "RepGlei", HIS."RepLegalApellPaterno" AS "RepApellidoPaterno",
					HIS."RepLegalApellMaterno" AS "RepApellidoMaterno", HIS."RepLegalPrimerNombre" AS "RepPrimerNombre",
					HIS."RepLegalSegundoNombre" AS "RepSegundoNombre", HIS."RepLegalRazonSocial" AS "RepRazonSocial",
					HIS."RepLegalTelefono" AS "RepTelefono", HIS."RepLegalDireccionElectronica" AS "RepEmail",
					HIS."RepLegalTipoDireccion" AS "RepDireccionTipo", HIS."RepLegalCalle" AS "RepDireccionNombre",
					HIS."RepLegalNumero" AS "RepDireccionNumero", HIS."RepLegalDepartamento" AS "RepDireccionDepto",
					HIS."RepLegalComuna" AS "RepDireccionComuna", HIS."RepLegalCiudad" AS "RepDireccionCiudad",
					HIS."RepLegalProvincia" AS "RepDireccionProvincia", HIS."RepLegalPais" AS "RepDireccionPais",
					HIS."RepLegalCodigoPostal" AS "RepCodigoPostal", HIS."RepLegalNacionalidad" AS "RepNacionalidad",
					HIS."RepLegalRelacionEntidad" AS "RepRelacionIntermediario", HIS."RepLegalEstadoCivil" AS "RepEstadoCivil",
					RTRIM(COALESCE("ConyRepLegalRut", '0')) AS "ConyRepRut", HIS."ConyRepLegalDig" AS "ConyRepDig",
					HIS."ConyRepLegalPasaporte" AS "ConyRepPasaporte", HIS."ConyRepLegalApellPaterno" AS "ConyRepApellidoPaterno",
					HIS."ConyRepLegalApellMaterno" AS "ConyRepApellidoMaterno", HIS."ConyRepLegalPrimerNombre" AS "ConyRepPrimerNombre",
					HIS."ConyRepLegalSegundoNombre" AS "ConyRepSegundoNombre", HIS."ConyRepLegalRazonSocial" AS "ConyRepRazonSocial",
					HIS."ConyRepLegalTelefono" AS "ConyRepTelefono", HIS."ConyRepLegalDireccionElectronica" AS "ConyRepEmail",
					HIS."ConyRepLegalTipoDireccion" AS "ConyRepDireccionTipo", HIS."ConyRepLegalCalle" AS "ConyRepDireccionNombre",
					HIS."ConyRepLegalNumero" AS "ConyRepDireccionNumero", HIS."ConyRepLegalDepartamento" AS "ConyRepDireccionDepto",
					HIS."ConyRepLegalComuna" AS "ConyRepDireccionComuna", HIS."ConyRepLegalCiudad" AS "ConyRepDireccionCiudad",
					HIS."ConyRepLegalProvincia" AS "ConyRepDireccionEstado", HIS."ConyRepLegalPais" AS "ConyRepDireccionPais",
					HIS."ConyRepLegalCodigoPostal" AS "ConyRepCodigoPostal", HIS."ConyRepLegalNacionalidad" AS "ConyRepNacionalidad",
					HIS."ConyRepLegalRelacionEntidad" AS "ConyRepRelacionIntermediario", HIS."CuentaBancariaUno" AS "CuentaBancariaUno",
					HIS."CuentaBancariaDos" AS "CuentaBancariaDos", HIS."CuentaBancariaTres" AS "CuentaBancariaTres",
					HIS."ClaseOrden" AS "ClaseOrden", HIS."PerfilActual" AS "Perfil",
					HIS."PerfilAnterior" AS "PerfilAnterior", HIS."FechaRegistro" AS "FechaInicioPerfilActual",
					"COSD"."NombreTipoPersona"		AS	"TipoPersona",
					COALESCE( NULLIF ("IndicadorPersonaJuridica",'') ,"COCLI"."TipoCliente") AS "TipoCliente" ,"COSD"."FechaIngreso" AS "FechaEnrolamiento",
					"COCLI"."ContratoCondicionesGenerales" AS "ContratoFirmadoNCG30",	"ANX"."FechaActivacion" AS "FechaSuscripcion",
					CASE WHEN "PERF"."Tipo" = 'J' THEN "PERF"."RazonSocial" ELSE "PERF"."Nombres" || ' ' || "PERF"."ApellidoPaterno" || ' ' || "PERF"."ApellidoMaterno" END AS "ConfirmadoPor", 
					"COSD"."NombreEstado" AS "EstadoCliente","COSD"."ComentarioBloqueo" AS "ComentarioBloqueo",	
					"COCLI"."ContratoCustodia" AS "ContratoCustodia",	"COCLI"."ConflictoInteres" AS "ConflictoInteres",	"COCLI"."InfoPrivilegiadaArt171" AS "InfoPrivilegiadaArt171",
					"COCLI"."CargoSVS171" AS "CargoSVS171","COCLI"."RelCorredoraArt100" AS "RelCorredoraArt100", "COCLI"."RelCorredora_NCG380" AS "RelCorredora_NCG380",	
					"COCLI"."RelCorredora_CIRC1064" AS "RelCorredora_CIRC1064","COCLI"."Cargo_NCG_68_69" AS "Cargo_NCG_68_69", "COCLI"."PersonaExpuestaPoliticamente" AS "PersonaExpuestaPoliticamente",
					(CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadRut" ELSE "DocumentoIdentidadPasaporte" END )AS "NumeroContrato"
	                 FROM 	"BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" AS HIS
					--* -----------------------------------------------------------------------------------------------			
					INNER JOIN :v_TablaUtlReg_x_Cli ULT
					ON ( HIS."IdCliente" = ULT."IdCliente" AND HIS."FechaRegistro" = ULT."FechaUltimo_Registro_x_Cliente"  )
					--* -----------------------------------------------------------------------------------------------
					INNER JOIN :v_TablaClientes AS CLI 
					ON (	
					         COALESCE(CLI."Valor",'') = TRIM("DocumentoIdentidadPasaporte")
							OR v_TodoCliente = 'S' 
							OR v_Solo1Cliente = HIS."IdCliente" )
							
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" CONSULTAR_CLIENTE
					ON ( TRIM("DocumentoIdentidadRut")||'-'||TRIM("DocumentoIdentidadDig") = CONSULTAR_CLIENTE."DocumentoIdentidad")
					OR TRIM("DocumentoIdentidadPasaporte") = CONSULTAR_CLIENTE."DocumentoIdentidad"
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
			        ON  HIS."DocumentoIdentidadPasaporte" = doc."DocumentoIdentidad"
			        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "COSD" ON ("COSD"."IdCliente" = HIS."IdCliente")
 					LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteNCG380Detallado" AS "COCLI" ON ("COCLI"."IdCliente" = HIS."IdCliente")
					LEFT JOIN ( SELECT "IdCliente" AS "IdClientePersonaJuridica", "CodigoValorIndicador" AS "IndicadorPersonaJuridica" FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" WHERE "CodigoTipoIndicador" = 'TCLICMFJ' ) IND ON COCLI."IdCliente" = IND."IdClientePersonaJuridica"
					LEFT JOIN ( SELECT"ValorIndicador" AS "ValorIndicadorADMCAR" , "IdCliente" AS "ClienteADMCART" , ROW_NUMBER() OVER (PARTITION BY "IdCliente" ) AS rn	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"	WHERE   "CodigoTipoIndicador" = 'ADMCART'	AND "ValorIndicador" = 'S') AS VARADMCAR ON VARADMCAR."ClienteADMCART" = "COCLI"."IdCliente" AND VARADMCAR.rn = 1
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" "DOCF" ON "DOCF"."IdTipoDocumento" = 'CONTRATOCONDICIONESGRALES' AND "DOCF"."IdCliente" = HIS."IdCliente"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" "ANX" ON "ANX"."IdDocumento" = "DOCF"."IdDocumento" AND "ANX"."IdAnexo" = "DOCF"."IdUltimoAnexo"				
					LEFT JOIN LatestFirma "LF" ON "LF"."IdAnexo" = "ANX"."IdAnexo"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" "F" ON "F"."IdAnexo" = "LF"."IdAnexo" AND "F"."FechaFirma" = "LF"."FechaFirma"
					LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" "SF" ON "SF"."IdSolicitudFirmaAnexo" = "F"."IdSolicitudFirmaAnexo"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" "PERF" ON "PERF"."IdPersona" = "SF"."IdPersonaFirmante"
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL'
			ORDER BY HIS."IdCliente" ASC;
	END IF;
	END IF;

	IF v_DocumentoOficial = 'S' THEN
		
		IF v_FechaTermino = v_FecSistema THEN
		
		--* QUIERE CONSULTAR LA DATA ACTUAL ------------------------------------------------------------------------------------
		CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline"( IdSecuenciaGlobal => :v_SecuenciaTablaGlobal );
		--* --------------------------------------------------------------------------------------------------------------------

		--* POR MIENTRAS LA VISTA SE CORRIGE (ACTUALMENTE ENTREGA 2 VECES MISMO REGUISTRO)
		v_Tabla_ONLINE = 
			SELECT DISTINCT * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" WHERE "IdClienteOnline" = v_SecuenciaTablaGlobal; 

		p_OUT_TablaSalida =
			--* ---------------------------------------------------------------------------
			SELECT	CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Rut" ELSE "Pasaporte" END AS "RutCodigoCliente", --Se remplaza "IdCliente"  por el Rut , se muestra en la columna Nro de cliente
			        CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Rut" ELSE '' END AS "Rut",
					CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "Dig" ELSE '' END AS "Dig",
					CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'P' THEN "Pasaporte" ELSE '' END AS "Pasaporte",
					"Glei", "ApellidoPaterno", "ApellidoMaterno", "PrimerNombre", "SegundoNombre" ,
					CASE WHEN DOC."TipoPersona" = 'N' THEN '' else "RazonSocial" END AS "RazonSocial",
					"Telefono", "Email" , "DireccionTipo", "DireccionNombre",   
					"DireccionNumero", "DireccionDepto", "DireccionComuna", "DireccionCiudad", "DireccionEstado",   
					"DireccionPais", "CodigoPostal", "Nacionalidad", "RelacionIntermediario", CASE WHEN VARADMCAR."ValorIndicadorADMCAR" = 'S' THEN 'S' ELSE 'N' END AS "AdministracionCartera",	 	
					RTRIM(COALESCE("RutEntidadEmpleadora",'0')) AS "RutEntidadEmpleadora", "DigEntidadEmpleadora", "RazonSocialEntidadEmpleadora",	"EstadoCivil", RTRIM(COALESCE("ConyRut",'0')) AS "ConyRut", 
					"ConyDig", "ConyPasaporte", "ConyApellidoPaterno", "ConyApellidoMaterno", "ConyPrimerNombre", 
					"ConySegundoNombre", "ConyRazonSocial", "ConyTelefono", "ConyEmail", "ConyDireccionTipo",	
					"ConyDireccionNombre", "ConyDireccionNumero", "ConyDireccionDepto", "ConyDireccionComuna", "ConyDireccionCiudad",
					"ConyDireccionEstado", "ConyDireccionPais", "ConyCodigoPostal", "ConyNacionalidad", "ConyRelacionIntermediario",
					RTRIM(COALESCE("RepRut",'0')) AS "RepRut", "RepDig", "RepPasaporte", "RepGlei", "RepApellidoPaterno",
					"RepApellidoMaterno", "RepPrimerNombre", "RepSegundoNombre", "RepRazonSocial", "RepTelefono",
					"RepEmail", "RepDireccionTipo", "RepDireccionNombre", "RepDireccionNumero", "RepDireccionDepto",	
					"RepDireccionComuna", "RepDireccionCiudad", "RepDireccionProvincia", "RepDireccionPais", "RepCodigoPostal",
					"RepNacionalidad", "RepRelacionIntermediario", "RepEstadoCivil", RTRIM(COALESCE("ConyRepRut", '0')) AS "ConyRepRut", "ConyRepDig",
					"ConyRepPasaporte",	"ConyRepApellidoPaterno", "ConyRepApellidoMaterno", "ConyRepPrimerNombre", "ConyRepSegundoNombre",	
					"ConyRepRazonSocial", "ConyRepTelefono", "ConyRepEmail", "ConyRepDireccionTipo", "ConyRepDireccionNombre",
					"ConyRepDireccionNumero", "ConyRepDireccionDepto", "ConyRepDireccionComuna", "ConyRepDireccionCiudad", "ConyRepDireccionEstado",
					"ConyRepDireccionPais", "ConyRepCodigoPostal", "ConyRepNacionalidad", "ConyRepRelacionIntermediario", "CuentaBancariaUno",	
					"CuentaBancariaDos", "CuentaBancariaTres", "ClaseOrden", "Perfil", "PerfilAnterior", "FechaInicioPerfilActual", ''		AS	"TipoPersona",
 					'' AS"TipoCliente",''AS  "FechaEnrolamiento",'' AS "ContratoFirmadoNCG30",	'' AS "FechaSuscripcion",'' AS "ConfirmadoPor", 
 					'' AS "EstadoCliente",
 					'' AS "ComentarioBloqueo", '' AS "ContratoCustodia", '' AS "ConflictoInteres",''AS "InfoPrivilegiadaArt171",'' AS  "CargoSVS171",
 					'' AS "RelCorredoraArt100", '' AS "RelCorredora_NCG380", '' AS "RelCorredora_CIRC1064",''AS "Cargo_NCG_68_69",''AS "PersonaExpuestaPoliticamente",
 					'' AS "NumeroContrato" 
			FROM	:v_Tabla_ONLINE AS HOY
					INNER JOIN :v_TablaClientes AS CLI ON 
					    COALESCE(CLI."Valor",'') = TRIM("Pasaporte")
						OR v_TodoCliente = 'S' 
						OR v_Solo1Cliente = HOY."NumeroCliente"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" CONSULTAR_CLIENTE
					ON (TRIM("Rut")||'-'||TRIM("Dig") = CONSULTAR_CLIENTE."DocumentoIdentidad")
					OR  TRIM("Pasaporte") = CONSULTAR_CLIENTE."DocumentoIdentidad"
					LEFT JOIN ( SELECT"ValorIndicador" AS "ValorIndicadorADMCAR" , "IdCliente" AS "ClienteADMCART" , ROW_NUMBER() OVER (PARTITION BY "IdCliente" ) AS rn	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"	WHERE   "CodigoTipoIndicador" = 'ADMCART'	AND "ValorIndicador" = 'S') AS VARADMCAR ON VARADMCAR."ClienteADMCART" = CONSULTAR_CLIENTE."IdCliente" AND VARADMCAR.rn = 1
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
			        ON  "Pasaporte" = doc."DocumentoIdentidad"
			
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL'
			ORDER BY HOY."NumeroCliente" ASC;

	ELSE

		--* ULTIMO REGISTRO DE CLIENTE (ULTIMOS DATOS GUARDADOS)
		v_TablaUtlReg_x_Cli = 
			SELECT "IdCliente", MAX("FechaRegistro") AS "FechaUltimo_Registro_x_Cliente" 
			FROM	"BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" T1 
			WHERE	"FechaRegistro" <= v_FechaTermino
			GROUP BY "IdCliente";

			
	


		p_OUT_TablaSalida =
			--* ---------------------------------------------------------------------------
			SELECT	 CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadRut" ELSE "DocumentoIdentidadPasaporte" END AS "RutCodigoCliente",  --Se remplaza "IdCliente"  por el Rut , se muestra en la columna Nro de cliente
					 CASE WHEN DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadRut" ELSE '' END AS "Rut",
					 CASE WHEN  DOC."IdTipoDocumentoIdentidad" = 'R' THEN "DocumentoIdentidadDig" ELSE '' END AS "Dig",
					 CASE WHEN  DOC."IdTipoDocumentoIdentidad" = 'P' THEN "DocumentoIdentidadPasaporte" ELSE '' END AS "Pasaporte",
					"Glei",
					"ApellidoPaterno",
					"ApellidoMaterno",
					"PrimerNombre", 
					"SegundoNombre" ,
					CASE WHEN DOC."TipoPersona" = 'N' THEN '' else "RazonSocial" END AS "RazonSocial", 
					"Telefono",
					"DireccionElectronica" AS "Email" ,
					"TipoDireccion" AS "DireccionTipo",
					"Calle" AS "DireccionNombre",
					"Numero" AS "DireccionNumero",
					"Departamento" AS "DireccionDepto", 
					"Comuna" AS "DireccionComuna", "Ciudad" AS "DireccionCiudad", 
					"Provincia" AS "DireccionEstado", "Pais" AS "DireccionPais", 
					"CodigoPostal", "Nacionalidad", 
					"RelacionEntidad" AS "RelacionIntermediario", CASE WHEN VARADMCAR."ValorIndicadorADMCAR" = 'S' THEN 'S' ELSE 'N' END AS "AdministracionCartera",
					RTRIM(COALESCE("EmpleadorRut", '0')) AS "RutEntidadEmpleadora", "EmpleadorDig" AS "DigEntidadEmpleadora" , 
					"EmpleadorRazonSocial" AS "RazonSocialEntidadEmpleadora", "EstadoCivil", 
					RTRIM(COALESCE("ConyugeRut", '0')) AS "ConyRut", "ConyugeDig" AS "ConyDig", 
					"ConyugePasaporte" AS "ConyPasaporte", "ConyugeApellPaterno" AS "ConyApellidoPaterno", 
					"ConyugeApellMaterno" AS "ConyApellidoMaterno", "ConyugePrimerNombre" AS "ConyPrimerNombre", 
					"ConyugeSegundoNombre" AS "ConySegundoNombre", "ConyugeRazonSocial" AS "ConyRazonSocial", 
					"ConyugeTelefono" AS "ConyTelefono", "ConyugeDireccionElectronica" AS "ConyEmail", 
					"ConyugeTipoDireccion" AS "ConyDireccionTipo", "ConyugeCalle" AS "ConyDireccionNombre", 
					"ConyugeNumero" AS "ConyDireccionNumero", "ConyugeDepartamento" AS "ConyDireccionDepto", 
					"ConyugeComuna" AS "ConyDireccionComuna", "ConyugeCiudad" AS "ConyDireccionCiudad", 
					"ConyugeProvincia" AS "ConyDireccionEstado", "ConyugePais" AS "ConyDireccionPais", 
					"ConyugeCodigoPostal" AS "ConyCodigoPostal", "ConyugeNacionalidad" AS "ConyNacionalidad", 
					"ConyugeRelacionEntidad" AS "ConyRelacionIntermediario", RTRIM(COALESCE("RepLegalRut", '0')) AS "RepRut",
					"RepLegalDig" AS "RepDig", "RepLegalPasaporte" AS "RepPasaporte",
					"RepLegalGlei" AS "RepGlei", "RepLegalApellPaterno" AS "RepApellidoPaterno",
					"RepLegalApellMaterno" AS "RepApellidoMaterno", "RepLegalPrimerNombre" AS "RepPrimerNombre",
					"RepLegalSegundoNombre" AS "RepSegundoNombre", "RepLegalRazonSocial" AS "RepRazonSocial",
					"RepLegalTelefono" AS "RepTelefono", "RepLegalDireccionElectronica" AS "RepEmail",
					"RepLegalTipoDireccion" AS "RepDireccionTipo", "RepLegalCalle" AS "RepDireccionNombre",
					"RepLegalNumero" AS "RepDireccionNumero", "RepLegalDepartamento" AS "RepDireccionDepto",
					"RepLegalComuna" AS "RepDireccionComuna", "RepLegalCiudad" AS "RepDireccionCiudad",
					"RepLegalProvincia" AS "RepDireccionProvincia", "RepLegalPais" AS "RepDireccionPais",
					"RepLegalCodigoPostal" AS "RepCodigoPostal", "RepLegalNacionalidad" AS "RepNacionalidad",
					"RepLegalRelacionEntidad" AS "RepRelacionIntermediario", "RepLegalEstadoCivil" AS "RepEstadoCivil",
					RTRIM(COALESCE("ConyRepLegalRut", '0')) AS "ConyRepRut", "ConyRepLegalDig" AS "ConyRepDig",
					"ConyRepLegalPasaporte" AS "ConyRepPasaporte", "ConyRepLegalApellPaterno" AS "ConyRepApellidoPaterno",
					"ConyRepLegalApellMaterno" AS "ConyRepApellidoMaterno", "ConyRepLegalPrimerNombre" AS "ConyRepPrimerNombre",
					"ConyRepLegalSegundoNombre" AS "ConyRepSegundoNombre", "ConyRepLegalRazonSocial" AS "ConyRepRazonSocial",
					"ConyRepLegalTelefono" AS "ConyRepTelefono", "ConyRepLegalDireccionElectronica" AS "ConyRepEmail",
					"ConyRepLegalTipoDireccion" AS "ConyRepDireccionTipo", "ConyRepLegalCalle" AS "ConyRepDireccionNombre",
					"ConyRepLegalNumero" AS "ConyRepDireccionNumero", "ConyRepLegalDepartamento" AS "ConyRepDireccionDepto",
					"ConyRepLegalComuna" AS "ConyRepDireccionComuna", "ConyRepLegalCiudad" AS "ConyRepDireccionCiudad",
					"ConyRepLegalProvincia" AS "ConyRepDireccionEstado", "ConyRepLegalPais" AS "ConyRepDireccionPais",
					"ConyRepLegalCodigoPostal" AS "ConyRepCodigoPostal", "ConyRepLegalNacionalidad" AS "ConyRepNacionalidad",
					"ConyRepLegalRelacionEntidad" AS "ConyRepRelacionIntermediario", "CuentaBancariaUno" AS "CuentaBancariaUno",
					"CuentaBancariaDos" AS "CuentaBancariaDos", "CuentaBancariaTres" AS "CuentaBancariaTres",
					"ClaseOrden" AS "ClaseOrden", "PerfilActual" AS "Perfil",
					"PerfilAnterior" AS "PerfilAnterior", "FechaRegistro" AS "FechaInicioPerfilActual",
					 ''		AS	"TipoPersona",
 					'' AS"TipoCliente",''AS  "FechaEnrolamiento",'' AS "ContratoFirmadoNCG30",	'' AS "FechaSuscripcion",'' AS "ConfirmadoPor", 
 					'' AS "EstadoCliente",
 					'' AS "ComentarioBloqueo", '' AS "ContratoCustodia", '' AS "ConflictoInteres",''AS "InfoPrivilegiadaArt171",'' AS  "CargoSVS171",
 					'' AS "RelCorredoraArt100", '' AS "RelCorredora_NCG380", '' AS "RelCorredora_CIRC1064",''AS "Cargo_NCG_68_69",''AS "PersonaExpuestaPoliticamente",
 					'' AS "NumeroContrato" 
	                 FROM 	"BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" AS HIS
					--* -----------------------------------------------------------------------------------------------			
					INNER JOIN :v_TablaUtlReg_x_Cli ULT
					ON ( HIS."IdCliente" = ULT."IdCliente" AND HIS."FechaRegistro" = ULT."FechaUltimo_Registro_x_Cliente"  )
					--* -----------------------------------------------------------------------------------------------
					INNER JOIN :v_TablaClientes AS CLI 
					ON (	
					         COALESCE(CLI."Valor",'') = TRIM("DocumentoIdentidadPasaporte")
							OR v_TodoCliente = 'S' 
							OR v_Solo1Cliente = HIS."IdCliente" )
							
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" CONSULTAR_CLIENTE
					ON ( TRIM("DocumentoIdentidadRut")||'-'||TRIM("DocumentoIdentidadDig") = CONSULTAR_CLIENTE."DocumentoIdentidad")
					OR TRIM("DocumentoIdentidadPasaporte") = CONSULTAR_CLIENTE."DocumentoIdentidad"
					LEFT JOIN ( SELECT"ValorIndicador" AS "ValorIndicadorADMCAR" , "IdCliente" AS "ClienteADMCART" , ROW_NUMBER() OVER (PARTITION BY "IdCliente" ) AS rn	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"	WHERE   "CodigoTipoIndicador" = 'ADMCART'	AND "ValorIndicador" = 'S') AS VARADMCAR ON VARADMCAR."ClienteADMCART" = CONSULTAR_CLIENTE."IdCliente" AND VARADMCAR.rn = 1
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
			        ON  HIS."DocumentoIdentidadPasaporte" = doc."DocumentoIdentidad"
			WHERE CONSULTAR_CLIENTE."TipoEnrolamiento" = 'FULL'
			ORDER BY HIS."IdCliente" ASC;

	END IF;		
	END IF;
END;