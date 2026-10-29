SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNomina";
GO
CREATE PROCEDURE
--
--  Proposito: Servicio para crear clientes desde un archivo.
--  Autor: CAPONTE - BCS
--  Fecha Creacion: 08/03/2016
--
"BSCL.SocioNegocio.BD.Servicios::CLI.CrearClienteNomina" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),  		--ID del Modulo que lo invoca
    -- Origen de datos del servicio
	--IN  p_TipoCliente,
	IN  p_ClienteBloqueado					NVARCHAR(1), -- 'S' o 'N'
	IN  p_IdCategoriaInversionistaDefecto	NVARCHAR(40),
	IN  p_IdPerfilComercialDefecto			NVARCHAR(40),
	IN  p_IdTipoClienteDefecto  			NVARCHAR(40),
	IN  p_IdEmpleadoEjecutivoDefecto		NVARCHAR(40),
	IN	p_PermiteParametrosDefault			NVARCHAR(1),
	IN  p_Archivo	TABLE (
						TIPODOCUMENTO NVARCHAR(3), 
						DOCUMENTOIDENTIDAD NVARCHAR(40), 
						TIPOPERSONA NVARCHAR(1),
					 	NOMBRES NVARCHAR(100), 
					 	APELLIDOPATERNO NVARCHAR(100), 
					 	APELLIDOMATERNO NVARCHAR(100),
					 	PAISNACIMIENTO BIGINT, 
					 	FECHANACIMIENTO NVARCHAR(10), 
					 	EDOCIVIL NVARCHAR(2), 
					 	GENERO NVARCHAR(2),
					 	RAZONSOCIAL NVARCHAR(100),
					 	CALLE NVARCHAR(100), 
					 	NUMERO NVARCHAR(20), 
					 	COMPLEMENTO NVARCHAR(100), 
					 	PAISRESIDENCIA BIGINT, 
					 	CIUDAD BIGINT,
					 	TIPOTELEFONO NVARCHAR(40), 
					 	TELEFONO NVARCHAR(100),
					 	EMAIL NVARCHAR(100), 
					 	EJECUTIVO NVARCHAR(40),
					 	CATINV NVARCHAR(40), 
					 	PERCOMER NVARCHAR(40), 
					 	TIPOCLIENTE NVARCHAR(40),
					 	MENSAJEVALIDACION NVARCHAR(200)
					 	),
	
	OUT p_out_Mensaje TABLE (
	                        "IDPERSONA" BIGINT,
	                        "IDCLIENTE" BIGINT,
	                        "TIPODOCUMENTO" NVARCHAR(3),
							"DOCUMENTOIDENTIDAD" NVARCHAR(40),
							"NOMBRES" NVARCHAR(100) ,
							"APELLIDOPATERNO" NVARCHAR(100),
							"APELLIDOMATERNO" NVARCHAR(100),
							"RAZONSOCIAL"	NVARCHAR(100),
							"MENSAJE" NVARCHAR(200)),				 
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        declare v_errorExt nvarchar(40);
        IF ::SQL_ERROR_CODE > 10000 THEN
            --error pseudo-controlado pero desde un Sub-SP
            call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
            p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
                                   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
            select count(1) into p_out_ErroresContador
            from :p_out_ErroresDetalle;
        ELSE
            --Error interno no manejado  (El log será captado por la capa de XS control)
            RESIGNAL;
        END IF;
    END;


	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	--p_out_RefId					:= 0;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_count								BIGINT;
		DECLARE v_IdEnrolamiento					BIGINT;
		DECLARE v_ExistePersona						NVARCHAR(1);
		DECLARE v_ExisteCliente						NVARCHAR(1);
		DECLARE v_IdSocioNegocio					BIGINT;
		DECLARE v_IdPersona							BIGINT;
		DECLARE v_IdDocIdentidad					BIGINT;
		DECLARE v_IdCliente							BIGINT;
		DECLARE v_IdSegmentoCliente					BIGINT;
		DECLARE v_IdDireccionFisicaPrincipal		BIGINT;
		DECLARE v_IdDireccionElectronicaPrincipal	BIGINT;
		DECLARE v_IdTelefonoPrincipal				BIGINT;
		DECLARE v_IdPaisCiudad  					BIGINT;
		DECLARE v_RequiereAutorizacion				NVARCHAR(1);
		DECLARE v_EsEnrolado						NVARCHAR(1);
		DECLARE	v_IdTipoEntidad						NVARCHAR(100);
		DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(1);
		DECLARE v_DocumentoIdentidad				NVARCHAR(40);
		DECLARE v_POR_IdPerfilComercial 			NVARCHAR(40);
		DECLARE v_IdEmpleadoEjecutivo				BIGINT;
		
		
		declare v_fechaHoraSistema NVARCHAR(40);
		--------------------
		--Validaciones
		--------------------
		BEGIN
			IF p_PermiteParametrosDefault = 'N' THEN
				--si no permite parametros Default, NO PROCESAR los clientes a los cuales les falte informacion
                p_out_AdvertenciasDetalle = select 'PERSONA_INCOMPLETA' as "Codigo",'N' as "RequiereAutorizacion", AR."DOCUMENTOIDENTIDAD" || ',' || AR."CATINV" || ',' || AR."PERCOMER" || ',' || AR."TIPOCLIENTE" as "Descripcion" 
							                from :p_Archivo AR
							                WHERE IFNULL(AR."CATINV",'') = '' AND IFNULL(AR."PERCOMER",'') = '' AND IFNULL(AR."TIPOCLIENTE",'') = '';
							                
				p_Archivo = SELECT 
	                "TIPODOCUMENTO",
					"DOCUMENTOIDENTIDAD",
					"TIPOPERSONA",
					"NOMBRES",
					"APELLIDOPATERNO",
					"APELLIDOMATERNO",
					"PAISNACIMIENTO",
					"FECHANACIMIENTO",
					"EDOCIVIL",
					"GENERO",
					"RAZONSOCIAL",
					"CALLE",
					"NUMERO",
					"COMPLEMENTO",
					"PAISRESIDENCIA",
					"CIUDAD",
					"TIPOTELEFONO",
					"TELEFONO",
					"EMAIL",
					"EJECUTIVO",
					"CATINV",
					"PERCOMER",
					"TIPOCLIENTE",
					CASE WHEN IFNULL(AR."CATINV",'') = '' THEN 'Categoria Inversionista Invalida'  
					     WHEN IFNULL(AR."PERCOMER",'') = '' THEN 'Perfil Comercial Invalido'
					     WHEN IFNULL(AR."TIPOCLIENTE",'') = '' THEN 'Tipo Cliente Invalido'
					     WHEN IFNULL(AR."CIUDAD",0) = 0 THEN 'Ciudad Invalida'
					     ELSE 'OK' 
					END AS "MENSAJEVALIDACION"
				FROM :p_Archivo AR;
			END IF;
			
			-- valida en tabla cliente
			p_out_Archivo = SELECT "TIPODOCUMENTO",
				"DOCUMENTOIDENTIDAD",
				"TIPOPERSONA",
				"NOMBRES",
				"APELLIDOPATERNO",
				"APELLIDOMATERNO",
				"PAISNACIMIENTO",
				"FECHANACIMIENTO", 
				"EDOCIVIL",
				"GENERO",
				"RAZONSOCIAL",
				"CALLE", IFNULL("NUMERO",'0') as "NUMERO", "COMPLEMENTO", "PAISRESIDENCIA", "CIUDAD","TIPOTELEFONO", "TELEFONO", "EMAIL",
				CASE WHEN IFNULL(AR."EJECUTIVO",'') = '' THEN p_IdEmpleadoEjecutivoDefecto ELSE AR."EJECUTIVO" END AS "EJECUTIVO",
				CASE WHEN IFNULL(AR."CATINV",'') = '' THEN p_IdCategoriaInversionistaDefecto ELSE AR."CATINV" END AS "CATINV",
				CASE WHEN IFNULL(AR."PERCOMER",'') = '' THEN p_IdPerfilComercialDefecto ELSE AR."PERCOMER" END AS "PERCOMER",
				CASE WHEN IFNULL(AR."TIPOCLIENTE",'') = '' THEN p_IdTipoClienteDefecto ELSE AR."TIPOCLIENTE" END AS "TIPOCLIENTE",
				
				 "MENSAJEVALIDACION", 
				CASE WHEN IFNULL(CLI."DocumentoIdentidad",'N') = 'N' THEN 'N' ELSE 'S' END AS "EXISTECLIENTE",
				CASE WHEN IFNULL(CLI."DocumentoIdentidad",'N') = 'N' THEN "BSCL.SocioNegocio.BD.Secuencias::Cliente".NEXTVAL ELSE CLI."IdCliente" END AS "IDCLIENTE",
				'' AS "EXISTEPERSONA", 0 AS "IDPERSONA" , '' as "EXISTEENROLAMIENTO", 0 as "IDENROLAMIENTO",
				'' as "EXISTESOCIONEGOCIO", 0 as "IDSOCIONEGOCIO" 
				FROM :p_Archivo AR
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."DocumentoIdentidad" = AR."DOCUMENTOIDENTIDAD"  AND CLI."TipoDocumentoIdentidad" = AR."TIPODOCUMENTO";

			-- valida en tabla enrolamiento
			p_out_Archivo = select "TIPODOCUMENTO",
				"DOCUMENTOIDENTIDAD",
				"TIPOPERSONA",
				"NOMBRES",
				"APELLIDOPATERNO",
				"APELLIDOMATERNO",
				"PAISNACIMIENTO",
				"FECHANACIMIENTO", 
				"EDOCIVIL",
				"GENERO",
				"RAZONSOCIAL",
				"CALLE", "NUMERO", "COMPLEMENTO",
				"PAISRESIDENCIA",
				"CIUDAD", 
				"TIPOTELEFONO",
				"TELEFONO", 
				"EMAIL",
				"EJECUTIVO",
				"CATINV",
				"PERCOMER",
				"TIPOCLIENTE",
				"MENSAJEVALIDACION",
				"EXISTECLIENTE",
				"IDCLIENTE",
				"EXISTEPERSONA",
				"IDPERSONA",
				CASE WHEN IFNULL(ENR."DocumentoIdentidad",'N') = 'N' THEN 'N' ELSE 'S' END AS "EXISTEENROLAMIENTO",
				CASE WHEN IFNULL(ENR."DocumentoIdentidad",'N') = 'N' THEN "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento".NEXTVAL  ELSE ENR."IdEnrolamiento" END AS "IDENROLAMIENTO",
				"EXISTESOCIONEGOCIO",
				"IDSOCIONEGOCIO"
			FROM :p_out_Archivo AR
			LEFT JOIN "BSCL.SocioNegocio.BD::SOC.Enrolamiento" as ENR ON ENR."IdTipoEntidad" = 'CLIENTE' AND AR."EXISTECLIENTE" = 'N' AND ENR."IdTipoDocumentoIdentidad" =  AR."TIPODOCUMENTO" AND ENR."DocumentoIdentidad" =  AR."DOCUMENTOIDENTIDAD";

			--insert tabla enrolamiento
			INSERT INTO "BSCL.SocioNegocio.BD::SOC.Enrolamiento" (
			                   "IdEnrolamiento",
			                   "IdEmpresa",
			                   "IdUsuario",
			                   "IdTipoEntidad",
			                   "IdEntidadEnrolada",
			                   "IdTipoDocumentoIdentidad",
			                   "DocumentoIdentidad",
			                   "Tipo",
			                   "Nombres",
			                   "ApellidoPaterno",
			                   "ApellidoMaterno",
			                   "RazonSocial",
			                   "IdPaisNacimiento",
			                   "FechaNacimiento",
			                   "Sexo",
			                   "EstadoCivil",
			                   "IdCiudad",
			                   "IdTipoTelefono",
			                   "Telefono",
			                   "Email",
			                   "DireccionCalle",
			                   "FechaCreacion",
			                   "FechaModificacion",
			                   "CLI_IdCategoriaInversionista",
			                   "POR_IdPerfilComercial",
			                   "CLI_IdTipoCliente" )
			                SELECT
			                    AR."IDENROLAMIENTO",
			                    p_IdEmpresa,
			                    p_IdUsuario,
			                    'CLIENTE',
			                    0,
			                    AR."TIPODOCUMENTO",
			                    AR."DOCUMENTOIDENTIDAD",
			                    AR."TIPOPERSONA",
			                    AR."NOMBRES",
			                    AR."APELLIDOPATERNO",
			                    AR."APELLIDOMATERNO",
			                    AR."RAZONSOCIAL",               
			                    CASE WHEN AR."TIPODOCUMENTO" = 'N' THEN AR."PAISNACIMIENTO" ELSE 0 END,
			                    TO_DATE(AR."FECHANACIMIENTO",'DD-MM-YYYY'),
			                    AR."GENERO",
			                    AR."EDOCIVIL",
			                    AR."CIUDAD",
			                    AR."TIPOTELEFONO",
			                    AR."TELEFONO",
			                    AR."EMAIL",
			                    AR."CALLE",
			                    CURRENT_UTCTIMESTAMP as "FechaCreacion",
			                    CURRENT_UTCTIMESTAMP as "FechaModificacion",
			                    AR."CATINV",
			                    AR."PERCOMER",
								AR."TIPOCLIENTE"
			                FROM
			                    :p_out_Archivo AR
			                WHERE AR."EXISTEENROLAMIENTO" = 'N' AND AR."EXISTECLIENTE" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
							
			-- valida en tabla persona
			v_archivo = SELECT "TIPODOCUMENTO",
							"DOCUMENTOIDENTIDAD",
							"TIPOPERSONA",
							"NOMBRES",
							"APELLIDOPATERNO",
							"APELLIDOMATERNO",
							"PAISNACIMIENTO",
							"FECHANACIMIENTO", 
							"EDOCIVIL",
							"GENERO",
							"RAZONSOCIAL",
							"CALLE", "NUMERO", "COMPLEMENTO", "PAISRESIDENCIA", "CIUDAD","TIPOTELEFONO", "TELEFONO", "EMAIL",
							"IdEmpleadoEjecutivo" AS "EJECUTIVO",
							CASE WHEN IFNULL(E."IdPersonaEjecutivo",'0')='0' THEN '0' ELSE E."IdPersonaEjecutivo" END AS "IDPERSONAEJECUTIVO",
							"CATINV","PERCOMER","TIPOCLIENTE", 
							CASE WHEN IFNULL(E."IdPersonaEjecutivo",'0')='0' THEN 'EL EMPLEADO NO ES EJECUTIVO' ELSE AR."MENSAJEVALIDACION" END AS "MENSAJEVALIDACION",
							"EXISTECLIENTE", 
							"IDCLIENTE",
							CASE WHEN IFNULL(PER."DocumentoIdentidad",'N') = 'N' THEN 'N' ELSE 'S' END AS "EXISTEPERSONA",
							CASE WHEN COALESCE(SOC."IdPersona",'0') = '0' THEN "BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL ELSE PER."IdPersona" END AS "IDPERSONA",
							"EXISTEENROLAMIENTO",
							"IDENROLAMIENTO",
							CASE WHEN IFNULL(SOC."IdSocioDeNegocio",0) = 0 THEN 'N' ELSE 'S' END AS "EXISTESOCIONEGOCIO",
							CASE WHEN IFNULL(SOC."IdSocioDeNegocio",0) = 0 THEN "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL ELSE SOC."IdSocioDeNegocio" END AS "IDSOCIONEGOCIO",
							CASE WHEN IFNULL(PER."IdDocumentoIdentidad",0) = 0 THEN "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL ELSE PER."IdDocumentoIdentidad" END AS "IDDOCUMENTOIDENTIDAD",
							CASE WHEN IFNULL(PER."IdDireccionFisicaPrincipal",0) = 0 THEN "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".NEXTVAL ELSE PER."IdDireccionFisicaPrincipal" END AS "IDDIRECCIONFISICA",
							CASE WHEN IFNULL(PER."IdDireccionElectronicaPrincipal",0) = 0 THEN "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".NEXTVAL ELSE PER."IdDireccionElectronicaPrincipal" END AS "IDDIRECCIONELECTRONICA",
							CASE WHEN IFNULL(PER."IdTelefonoPrincipal",0) = 0 THEN "BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL ELSE PER."IdTelefonoPrincipal" END AS "IDTELEFONO"
			FROM :p_out_Archivo AR
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON 
				PER."DocumentoIdentidad" = AR."DOCUMENTOIDENTIDAD" AND PER."TipoDocumentoIdentidad" = AR."TIPODOCUMENTO"
				AND AR."EXISTEENROLAMIENTO" = 'N' AND AR."EXISTECLIENTE" = 'N'
			LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON PER."IdPersona" = SOC."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" E ON E."CodigoEmpleado" = AR."EJECUTIVO" AND E."IdEstado" = 'ACT';

            --Inserta Persona
            INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona"  (
                    "IdPersona" ,
                    "IdEstado" ,
                    "IdDocumentoIdentidad" ,
                    "Tipo" ,
                    "Nombres" ,
                    "ApellidoPaterno" ,
                    "ApellidoMaterno" ,
                    "RazonSocial" ,
                    "IdPaisNacimiento" ,
                    "IdPaisResidencia" ,
                    "FechaNacimiento" ,
                    "Sexo" ,
                    "EstadoCivil" ,
                    "IdDireccionFisicaPrincipal" ,
                    "IdDireccionElectronicaPrincipal" ,
                    "IdTelefonoPrincipal" ,
                    "FechaIngreso" ,
                    "FechaCreacion" ,
                    "FechaModificacion" ,
                    "EsEnrolado",
                    "IdUsuarioCreador"
             ) select
                    "IDPERSONA", --IdPersona
                    'ACT',                                --REVISAR SI PCLIENTE_BLOQUEADO APLICA ACA IdEstado
                    NULL, --IdDocumentoIdentidad
                    "TIPOPERSONA", --Tipo
                    "NOMBRES", --Nombres
                    "APELLIDOPATERNO", --ApellidoPaterno
                    "APELLIDOMATERNO", --ApellidoMaterno
                    "RAZONSOCIAL",                                                               --TODO RazonSocial
                    CASE "PAISNACIMIENTO" WHEN 0 THEN NULL ELSE "PAISNACIMIENTO" END, --IdPaisNacimiento
                    CASE "PAISRESIDENCIA" WHEN 0 THEN NULL ELSE "PAISRESIDENCIA" END, --IdPaisResidencia
                    CASE Length("FECHANACIMIENTO") WHEN 0 THEN NULL ELSE TO_DATE("FECHANACIMIENTO",'DD-MM-YYYY') END, --FechaNacimiento
                    "GENERO", --Sexo
                    CASE Length("EDOCIVIL") WHEN 0 THEN NULL ELSE "EDOCIVIL" END,
                    NULL, --IdDireccionFisicaPrincipal
                    NULL, --IdDireccionElectronicaPrincipal
                    NULL, --IdTelefonoPrincipal
                    CURRENT_UTCTIMESTAMP, --FechaIngreso
                    CURRENT_UTCTIMESTAMP, --FechaCreacion
                    CURRENT_UTCTIMESTAMP, --FechaModificacion
                    'S', --EsEnrolado
                    p_IdUsuario   --IdUsuarioCreador
             from  :v_archivo AR
             WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTEPERSONA" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			
			--Inserta Socio Negocio
			INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  (
			        "IdSocioDeNegocio",
			        "IdPersona",
			        "IdEmpresa"
			 ) SELECT AR."IDSOCIONEGOCIO",
			 		AR."IDPERSONA",
			 		p_IdEmpresa
			   FROM :v_archivo AR
			   WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTESOCIONEGOCIO" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			   
			
			 
			 --Inserta Documento Identidad
			 insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
					"IdDocumentoIdentidad",
					"IdPersona",
					"IdTipoDocumentoIdentidad",
					"DocumentoIdentidad",
					"IdEstadoAutorizacion",
					"FechaIngreso",
					"FechaCreacion",
					"FechaModificacion",
					"IdUsuarioCreador")
			select 	IDDOCUMENTOIDENTIDAD,
					IDPERSONA,
					"TIPODOCUMENTO",
					"DOCUMENTOIDENTIDAD",
					'ING',
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					p_IdUsuario --IdUsuarioCreador					
			from   :v_archivo AR
			WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTEPERSONA" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			   
			--Inserta Direccion
			INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionFisica"  (
			        "IdDireccionFisica" ,
			        "Calle" ,
			        "Numero" ,
			        "Departamento" ,
			        "Sector" ,
			        "CodigoPostal" ,
			        "IdCiudad" ,
			        "CodigoProposito" ,
			        "Comentario" ,
			        "IdEstado" ,
			        "FechaIngreso" ,
			        "FechaCreacion" ,
			        "FechaModificacion" ,
			        "IdPersona" ,
			        "IdEstadoAutorizacion",
			        "IdUsuarioCreador",
			        "Predeterminado"
			 ) select
			        "IDDIRECCIONFISICA", --IdDireccionFisica
			        "CALLE", --Calle
			        "NUMERO", --Numero
			        "COMPLEMENTO", --Departamento
			        '', --Sector
			        '', --CodigoPostal
			        "CIUDAD", --IdCiudad
			        'COME', --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        "IDPERSONA", --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario, --IdUsuarioCreador
					'S'
			from   :v_archivo AR
			WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTEPERSONA" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			
			--Inserta Telefono
			INSERT INTO "BSCL.SocioNegocio.BD::PER.Telefono"  (
			        "IdTelefono" ,
			        "IdPais" ,
			        "IdTipoTelefono",
			        "Numero" ,
			        "Anexo" ,
			        "CodigoProposito" ,
			        "Comentario" ,
			        "IdEstado" ,
			        "FechaIngreso" ,
			        "FechaCreacion" ,
			        "FechaModificacion" ,
			        "IdPersona" ,
			        "IdEstadoAutorizacion",
					"IdUsuarioCreador",
					"Predeterminado"
			 ) select
			        "IDTELEFONO", --IdTelefono
			        "PAISRESIDENCIA", --IdPais
					"TIPOTELEFONO",
			        "TELEFONO", --Numero
			        '', --Anexo
			        'COME', --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        "IDPERSONA", --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario,                      --IdUsuarioCreador
					'S'
			 from  :v_archivo AR
			WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTEPERSONA" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			
			--Inserta Email
			INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionElectronica"  (
			        "IdDireccionElectronica" ,
			        "Direccion" ,
			        "CodigoProposito" ,
			        "Comentario" ,
			        "IdEstado" ,
			        "FechaIngreso" ,
			        "FechaCreacion" ,
			        "FechaModificacion" ,
			        "IdPersona" ,
			        "IdEstadoAutorizacion",
					"IdUsuarioCreador",
			        "Predeterminado"
			 ) select
			        "IDDIRECCIONELECTRONICA", --IdDireccionElectronica
			        "EMAIL", --Direccion
			        'COME', --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        "IDPERSONA", --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario, --IdUsuarioCreador
					'S'
			  from  :v_archivo AR
			WHERE AR."EXISTECLIENTE" = 'N' AND AR."EXISTEPERSONA" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			
			
			v_IdSegmentoCliente := 2; --TODO: Obtener desde una configuracion
			
			--Inserta Cliente
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente"  (
		        "IdCliente" ,
		        "IdPersona" ,
		        "IdEmpresa" ,
		        "IdEstado" ,
		        "IdSegmentoCliente" ,
		        "IdCategoriaInversionista" ,
		        "IdUsuarioCreador" ,
		        "FechaIngreso" ,
		        "FechaCreacion" ,
		        "FechaModificacion" ,
		        "EsEnrolado",
		        "IdTipoCliente",
		        "IdPersonaEjecutivo"
		 ) select
		        "IDCLIENTE", --IdCliente
		        "IDPERSONA", --IdPersona
		        p_IdEmpresa, --IdEmpresa
				CASE WHEN p_ClienteBloqueado = 'N' THEN 'ACT' ELSE 'BLO' END , --IdEstado
		        v_IdSegmentoCliente, --IdSegmentoCliente
		        AR."CATINV", 
		        p_IdUsuario, --IdUsuarioCreador
		        CURRENT_UTCTIMESTAMP, --FechaIngreso
		        CURRENT_UTCTIMESTAMP, --FechaCreacion
		        CURRENT_UTCTIMESTAMP, --FechaModificacion
		        'S', --EsEnrolado
				"TIPOCLIENTE",
				"IDPERSONAEJECUTIVO"
		 from  :v_archivo AR
		 WHERE AR."EXISTECLIENTE" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
		 
		 --ACTUALIZAR IDS RELACIONADOS PERSONA
        UPDATE P
        SET P."IdDocumentoIdentidad" = A."IDDOCUMENTOIDENTIDAD",
            P."IdDireccionFisicaPrincipal" = A."IDDIRECCIONFISICA",
            P."IdDireccionElectronicaPrincipal" = A."IDDIRECCIONELECTRONICA" ,
            P."IdTelefonoPrincipal" = A."IDTELEFONO"
        FROM "BSCL.SocioNegocio.BD::PER.Persona" P , :v_archivo A
        WHERE P."IdPersona" = A."IDPERSONA" AND A."MENSAJEVALIDACION" = 'OK' AND A."EXISTEPERSONA" = 'N';
			
		
		------------------------------------------
		--Actualiza Socio Negocio con ID Cliente
		------------------------------------------
		update  SOC
		set  SOC."IdCliente" = AR."IDCLIENTE"
		FROM :v_archivo AR 
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
		ON  SOC."IdSocioDeNegocio" = AR."IDSOCIONEGOCIO"
		WHERE AR."EXISTECLIENTE" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';

		------------------------------------------
		--Actualiza Enrolamiento con IdEntidadEnrolada
		------------------------------------------
		update  ENR
		  set  ENR."IdEntidadEnrolada" = AR."IDCLIENTE"
		  FROM :v_archivo AR
		  INNER JOIN "BSCL.SocioNegocio.BD::SOC.Enrolamiento" ENR ON
		  ENR."IdEnrolamiento" = AR."IDENROLAMIENTO"
		WHERE AR."EXISTECLIENTE" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
			
		------------------------------------------
		--Agregar Portafolio
		------------------------------------------
		BEGIN
			DECLARE v_IdPerfilProductos					NVARCHAR(40);
			DECLARE v_IdPerfilCobros					NVARCHAR(40);	
			DECLARE v_IdPerfilLimites					NVARCHAR(40);
			DECLARE v_IdPerfilCondicionesOperativas		NVARCHAR(40);
			DECLARE v_IdPerfilFacturacion				NVARCHAR(40);
			DECLARE v_IdPerfilFormasPago				NVARCHAR(40);
			DECLARE v_IdMoneda                 			NVARCHAR(3);
			DECLARE v_IdMonedaSist             			NVARCHAR(3);
			DECLARE v_FechaSist                         NVARCHAR(40);
			DECLARE v_CuentaInversionPrincipal			NVARCHAR(1);
			
			BEGIN
			
			    v_tabla_con_perfiles =
				SELECT AR.*, "IdPerfilProductos","IdPerfilCobros","IdPerfilLimites","IdPerfilCondicionesOperativas",
				--"IdPerfilFacturacion",
				"BSCL.Portafolio.BD.Secuencias::Portafolio".NEXTVAL AS "IDPORTAFOLIO"
				FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" CON
				JOIN :v_archivo AR ON CON."IdPerfilComercialBase" = AR."PERCOMER" AND AR."EXISTECLIENTE" = 'N' AND AR."MENSAJEVALIDACION" = 'OK';
				
				SELECT "Valor" 
				INTO v_FechaSist 
				FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
				WHERE "CodigoParametro" = 'PLA.FechaSistema' 
				  AND "IdEmpresa" = p_IdEmpresa;
				
				-- INSERT PORTAFOLIOS
				insert into "BSCL.Portafolio.BD::POR.Portafolio" (
					"IdPortafolio",
					"CodigoPortafolio",
					"DescripcionPortafolio",
					"Proposito", 
					"IdCliente", 
					"IdEstado",
					"FechaBloqueo",
					"ComentarioBloqueo", 
					--Perfil Comercial
					"IdPerfilComercialBaseOrigen",
					"IdPerfilProductos",
					"IdPerfilCobros",
					"IdPerfilLimites",
					"IdPerfilCondicionesOperativas",
					--"IdPerfilFacturacion",
					--Autorizacion
					"IdEstadoAutorizacion",
					"IdEmpleadoEjecutivo",
					--
					"FechaIngreso", 
					"FechaCreacion", 
					"FechaModificacion"
				)
				SELECT 
					"IDPORTAFOLIO",
					0, -- POR DEFECTO
					'Portafolio Default',  
					'PROPGENERALES',
					"IDCLIENTE", 
					'ACT',
					'0001-01-01', 
					'', 
					"PERCOMER",
					"IdPerfilProductos",
					"IdPerfilCobros",
					"IdPerfilLimites",
					"IdPerfilCondicionesOperativas",
					--"IdPerfilFacturacion",
					'CON',
					"EJECUTIVO",
					TO_TIMESTAMP(:v_FechaSist), --:p_FechaIngreso, 
					TO_TIMESTAMP(:v_FechaSist), --:p_FechaCreacion, 
					TO_TIMESTAMP(:v_FechaSist)  --:p_FechaModificacion
				FROM :v_tabla_con_perfiles 
				WHERE "EXISTECLIENTE" = 'N' ;
				
				v_IdMoneda := 'CLP';
				SELECT "Valor" INTO v_IdMonedaSist FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
				WHERE "CodigoParametro" = 'PLA.MonedaSistema' and "IdEmpresa" = p_IdEmpresa;
				v_CuentaInversionPrincipal := CASE WHEN v_IdMoneda = v_IdMonedaSist THEN 'S' ELSE 'N' END;
				
				--- CUENTA INVERSION PESO
				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion"(
				    "IdCuentaInversion",
				    "IdEmpresa",
				    "IdCliente", 
					"IdPortafolio",
					"IdMoneda",
					"Codigo",
					"Nombre",
					"CuentaInversionPrincipal",
					"FechaIngresoRegistro",
					"IdUsuario",
					"IdEstado"	 
				) 
				SELECT 
					"BSCL.CuentaInversion.BD.Secuencias::CuentaInversion".NEXTVAL,
					p_IdEmpresa,
					"IDCLIENTE",
				    "IDPORTAFOLIO",
				    :v_IdMoneda,
				    'CCLP',
				    'Cuenta de Inversión en Pesos',
				    :v_CuentaInversionPrincipal,
				    CURRENT_UTCTIMESTAMP,
				    p_IdUsuario,
				    'ACT'--:v_IdEstado
				FROM :v_tabla_con_perfiles
				WHERE "EXISTECLIENTE" = 'N';

				--- CUENTA INVERSION DOLAR
				v_IdMoneda := 'USD';
				SELECT "Valor" INTO v_IdMonedaSist FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
				WHERE "CodigoParametro" = 'PLA.MonedaSistema' and "IdEmpresa" = p_IdEmpresa;
				v_CuentaInversionPrincipal := CASE WHEN v_IdMoneda = v_IdMonedaSist THEN 'S' ELSE 'N' END;

				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion"(
				    "IdCuentaInversion",
				    "IdEmpresa",
				    "IdCliente", 
					"IdPortafolio",
					"IdMoneda",
					"Codigo",
					"Nombre",
					"CuentaInversionPrincipal",
					"FechaIngresoRegistro",
					"IdUsuario",
					"IdEstado"	 
				) 
				SELECT 
					"BSCL.CuentaInversion.BD.Secuencias::CuentaInversion".NEXTVAL,
					p_IdEmpresa,
					"IDCLIENTE",
				    "IDPORTAFOLIO",
				    :v_IdMoneda,
				    'CDOL',
				    'Cuenta de Inversión en Dólares',
				    :v_CuentaInversionPrincipal,
				    CURRENT_UTCTIMESTAMP,
				    p_IdUsuario,
				    'ACT'--:v_IdEstado
				FROM :v_tabla_con_perfiles
				WHERE "EXISTECLIENTE" = 'N';

				--- CUENTA INVERSION EURO
				v_IdMoneda := 'EUR';
				SELECT "Valor" INTO v_IdMonedaSist FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
				WHERE "CodigoParametro" = 'PLA.MonedaSistema' and "IdEmpresa" = p_IdEmpresa;
				
				v_CuentaInversionPrincipal := CASE WHEN v_IdMoneda = v_IdMonedaSist THEN 'S' ELSE 'N' END;

				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion"(
				    "IdCuentaInversion",
				    "IdEmpresa",
				    "IdCliente", 
					"IdPortafolio",
					"IdMoneda",
					"Codigo",
					"Nombre",
					"CuentaInversionPrincipal",
					"FechaIngresoRegistro",
					"IdUsuario",
					"IdEstado"	 
				) 
				SELECT 
					"BSCL.CuentaInversion.BD.Secuencias::CuentaInversion".NEXTVAL,
					p_IdEmpresa,
					"IDCLIENTE",
				    "IDPORTAFOLIO",
				    :v_IdMoneda,
				    'CEUR',
				    'Cuenta de Inversión en Euros',
				    :v_CuentaInversionPrincipal,
				    CURRENT_UTCTIMESTAMP,
				    p_IdUsuario,
				    'ACT'--:v_IdEstado
				FROM :v_tabla_con_perfiles
				WHERE "EXISTECLIENTE" = 'N';
				
				
				
			END;
		END;
		
		
		
		
		------------------------------------------
		--FIN AGREGAR PORTAFOLIO
		------------------------------------------
		
		p_out_Mensaje = SELECT 
		                    "IDPERSONA",
		                    "IDCLIENTE",
		                    "TIPODOCUMENTO",
							"DOCUMENTOIDENTIDAD",
							"NOMBRES",
							"APELLIDOPATERNO",
							"APELLIDOMATERNO",
							"RAZONSOCIAL",
							CASE WHEN "MENSAJEVALIDACION" <> 'OK' THEN "MENSAJEVALIDACION"
							 ELSE
							 CASE WHEN "EXISTECLIENTE" = 'N' THEN 'Ingresado' ELSE 'Cliente ya existe' END
							END AS "MENSAJE"
					   FROM :v_archivo;
		
			
		END;

	 
	END;
END;



