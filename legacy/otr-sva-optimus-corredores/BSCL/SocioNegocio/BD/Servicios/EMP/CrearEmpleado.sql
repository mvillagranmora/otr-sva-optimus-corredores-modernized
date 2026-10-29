SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::EMP.CrearEmpleado";

CREATE PROCEDURE
--
--  Proposito: Servicio para crear empleados con los datos utilizados del enrolamiento
--  Autor: CFLORES - BCS
--  Fecha Creacion: 11/01/2016
--
--  Proposito: Se incluye Fatca
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 26/04/2016
--
--  Proposito: Se incorporan definiciones Optimus Perú
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 28/02/2017
--
"BSCL.SocioNegocio.BD.Servicios::EMP.CrearEmpleado" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),  	--ID del Modulo que lo invoca
    -- Origen de datos del servicio
	IN  p_ConsultarEnrolamiento				"BSCL.SocioNegocio.BD.Tipos::SOC.ConsultarEnrolamiento",

	-- Parametros salida del servicio publico
	OUT p_out_RefId  	 	 	 	 	 	bigint,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
	p_out_RefId					:= 0;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_count								BIGINT:=0;
		DECLARE v_count_CodigoEmpleadoExiste		BIGINT:=0;
		DECLARE v_count_Existe						BIGINT:=0;
		DECLARE v_count_ExisteAdicional				BIGINT:=0;
		DECLARE v_IdEnrolamiento					BIGINT;
		DECLARE v_ExistePersona						NVARCHAR(1);
		DECLARE v_ExisteEmpleado					NVARCHAR(1);
		DECLARE v_IdSocioNegocio					BIGINT;
		DECLARE v_IdPersona							BIGINT;
		DECLARE v_IdDocIdentidad					BIGINT;
		DECLARE v_IdDocIdentidadAdicional			BIGINT;
		DECLARE v_IdEmpleado						BIGINT;
		DECLARE v_IdDireccionFisicaPrincipal		BIGINT;
		DECLARE v_IdDireccionElectronicaPrincipal	BIGINT;
		DECLARE v_IdTelefonoPrincipal				BIGINT;
		DECLARE v_IdPaisCiudad  					BIGINT;
		DECLARE v_RequiereAutorizacion				NVARCHAR(1);
		DECLARE v_EsEnrolado						NVARCHAR(1);
		DECLARE	v_IdTipoEntidad						NVARCHAR(100);
		DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(3);
		DECLARE v_DocumentoIdentidad				NVARCHAR(40);
		DECLARE	v_IdTipoDocumentoIdentidadAdicional	NVARCHAR(3);
		DECLARE v_DocumentoIdentidadAdicional		NVARCHAR(40);
		DECLARE v_POR_IdPerfilComercial 			NVARCHAR(40);
		DECLARE v_EMP_CodigoEmpleado 				NVARCHAR(40);
		
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		
		DECLARE v_DocumentoEsValido					NVARCHAR(1);
		DECLARE v_IdPais							BIGINT;
		DECLARE v_Tipo								NVARCHAR(1);
		DECLARE V_MaximoCodigoEmpleado			 	BIGINT:=0;
		--------------------
		--Validaciones
		--------------------
		BEGIN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);		
			-- Un registro de datos de enrolamiento
			select count(1) into v_count  from :p_ConsultarEnrolamiento as ENR;
			if v_count != 1 then
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('EMPLEADO_NO_PUEDE_SER_ENROLADO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'EMPLEADO_NO_PUEDE_SER_ENROLADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			end if;

			SELECT "IdTipoEntidad","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdTipoDocumentoIdentidadAdicional","DocumentoIdentidadAdicional","POR_IdPerfilComercial","EMP_CodigoEmpleado", "Tipo"
			INTO   v_IdTipoEntidad,v_IdTipoDocumentoIdentidad,v_DocumentoIdentidad,v_IdTipoDocumentoIdentidadAdicional,v_DocumentoIdentidadAdicional,v_POR_IdPerfilComercial,v_EMP_CodigoEmpleado, v_Tipo
			FROM  :p_ConsultarEnrolamiento;

			--Entidad a enrolar debe ser EMPLEADO
			if v_IdTipoEntidad != 'EMPLEADO' then
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('EMPLEADO_NO_PUEDE_SER_ENROLADO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'EMPLEADO_NO_PUEDE_SER_ENROLADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			end if;

			--TODO: MAS VALIDACIONES!!

			-- Empleado Existe => Error
			CALL "BSCL.SocioNegocio.BD.Servicios::SOC.ValidarDocumentoIdentidad"(
				p_IdEmpresa,
				'EMPLEADO',
				v_IdTipoDocumentoIdentidad,
				v_DocumentoIdentidad,
				v_Tipo,
				:v_IdPais,
				:v_DocumentoEsValido
			);
			/*
			SELECT COUNT(1) INTO v_count_Existe
			FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 
			WHERE "DocumentoIdentidad" = :v_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :v_IdTipoDocumentoIdentidad
			AND "IdEmpresa" = :p_IdEmpresa;
						
			if v_count_Existe>0 then
			*/
			IF v_DocumentoEsValido = 'N' THEN
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('EMPLEADO_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'EMPLEADO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			end if;
			
			SELECT COUNT(1) INTO v_count_CodigoEmpleadoExiste
			FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 
			WHERE "CodigoEmpleado" = :v_EMP_CodigoEmpleado AND "IdEmpresa" = :p_IdEmpresa;				
				
			IF v_count_CodigoEmpleadoExiste>0 THEN
				v_hash= SELECT '' AS "Codigo",'' AS "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CODIGO_EMPLEADO_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  SELECT 'CODIGO_EMPLEADO_EXISTE' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle;
			END IF;

			SELECT CAST("Valor" AS INTEGER) INTO V_MaximoCodigoEmpleado 
			FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
			WHERE "CodigoParametro" = 'EMP.MaximoCodigoEmpleado' AND "IdEmpresa" = :p_IdEmpresa;
			
			IF V_MaximoCodigoEmpleado IS NULL THEN
				V_MaximoCodigoEmpleado := 40;
			END IF;
				
			IF LENGTH(v_EMP_CodigoEmpleado) > V_MaximoCodigoEmpleado THEN
				v_hash = SELECT 'Numero' AS "Codigo", CAST(V_MaximoCodigoEmpleado AS NVARCHAR(40)) AS "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('EMPLEADO_CODIGO_MAXIMO_LONGITUD_EXCEDE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle = SELECT 'EMPLEADO_CODIGO_MAXIMO_LONGITUD_EXCEDE' AS "Codigo", :v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL 
				SELECT * FROM :p_out_ErroresDetalle;
			END IF;
			
			--Resultados de validaciones
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;


		--------------------------------
		--Registro de Enrolamiento
		--En caso que no exista se agrega
		---------------------------------
		select count(1) into v_count
		from "BSCL.SocioNegocio.BD::SOC.Enrolamiento" as ENR
		join :p_ConsultarEnrolamiento as INP ON ENR."IdTipoEntidad" =  INP."IdTipoEntidad"  AND ENR."IdTipoDocumentoIdentidad" =  INP."IdTipoDocumentoIdentidad"   AND ENR."DocumentoIdentidad" =  INP."DocumentoIdentidad"
		AND ENR."Tipo" = (CASE WHEN v_IdPais = 44 THEN INP."Tipo" ELSE v_Tipo END);
		-- Borrador No Existe => Creamos
		if v_count = 0 then
			SELECT "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento".NEXTVAL INTO v_IdEnrolamiento FROM DUMMY;
			INSERT INTO "BSCL.SocioNegocio.BD::SOC.Enrolamiento" (
			                   "IdEnrolamiento",
			                   "IdEmpresa",
			                   "IdUsuario",
			                   "IdTipoEntidad",
			                   "IdEntidadEnrolada",
			                   "IdTipoDocumentoIdentidad",
			                   "IdTipoDocumentoIdentidadAsociado",
			                   "DocumentoIdentidad",
			                   "IdTipoDocumentoIdentidadAdicional",
			                   	"DocumentoIdentidadAdicional",
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
			                   "IdDivisionPais",
			                   "IdTipoTelefono",
			                   "Telefono",
			                   "Email",
			                   "DireccionCalle",
			                   "DireccionNumero",
			                   "DireccionDepartamento",
			                   "FechaCreacion",
			                   "FechaModificacion",
			                   "CLI_IdCategoriaInversionista",
			                   "POR_IdPerfilComercial",
			                   "CLI_IdTipoCliente",
			                   "CLI_IdClaseCliente",
			                   "EMP_CargoIndefinido" )
			                SELECT
			                    :v_IdEnrolamiento,
			                    "IdEmpresa",
			                    "IdUsuario",
			                    "IdTipoEntidad",
			                    "IdEntidadEnrolada",
			                    "IdTipoDocumentoIdentidad",
			                    "IdTipoDocumentoIdentidadAsociado",
			                    "DocumentoIdentidad",
			                    "IdTipoDocumentoIdentidadAdicional",
			                   	"DocumentoIdentidadAdicional",
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
			                    "IdDivisionPais",
			                    "IdTipoTelefono",
			                    "Telefono",
			                    "Email",
			                    "DireccionCalle",
			                    "DireccionNumero",
			                    "DireccionDepartamento",
			                    CURRENT_UTCTIMESTAMP as "FechaCreacion",
			                    CURRENT_UTCTIMESTAMP as "FechaModificacion",
			                    "CLI_IdCategoriaInversionista",
			                    "POR_IdPerfilComercial",
			                    "CLI_IdTipoCliente",
			                    "CLI_IdClaseCliente",
			                    "EMP_CargoIndefinido"
			                FROM
			                    :p_ConsultarEnrolamiento;

		 else
			select ENR."IdEnrolamiento" into v_IdEnrolamiento
			from "BSCL.SocioNegocio.BD::SOC.Enrolamiento" as ENR
			join :p_ConsultarEnrolamiento as INP ON ENR."IdTipoEntidad" =  INP."IdTipoEntidad"  AND ENR."IdTipoDocumentoIdentidad" =  INP."IdTipoDocumentoIdentidad"   AND ENR."DocumentoIdentidad" =  INP."DocumentoIdentidad";
         end if;

		------------------------------------------
		--Pregunta Si requiere autorizacion
		------------------------------------------
		
		v_RegistroSolicitudCambio =SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
		
		call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
			:p_IdEmpresa,
			:p_IdUsuario,
			:p_IdModulo,
			:v_RegistroSolicitudCambio,
			'ENROLE',
			:v_RequiereAutorizacion
			);

		/*call "BSCL.SocioNegocio.BD.Servicios::CLI.RequiereAutorizacionEnrolamiento" (
			:p_IdEmpresa,
			:p_IdUsuario,
			:p_IdModulo,
			:p_ConsultarEnrolamiento,
			:v_RequiereAutorizacion
		);*/
		if v_RequiereAutorizacion='S' then
			v_EsEnrolado		:=  'P';
		else
			v_EsEnrolado		:=  'S';
		end if;


		------------------------------------------
		--Inserta a la persona solo si no existe
		------------------------------------------
		--CALL "BSCL.SocioNegocio.BD.Servicios::CLI.EntidadEnrolamientoExiste"(p_IdEmpresa,p_IdUsuario,p_IdModulo,
		--	'PERSONA',v_IdTipoDocumentoIdentidad,v_DocumentoIdentidad,v_ExistePersona,v_IdPersona);
		v_count_Existe:=0;
		SELECT COUNT(1) INTO v_count_Existe
						FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
						WHERE "DocumentoIdentidad" = :v_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :v_IdTipoDocumentoIdentidad;
		
		--Documento identidad adicional
		SELECT COUNT(1) INTO v_count_ExisteAdicional
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" 
			WHERE "DocumentoIdentidad" = :v_DocumentoIdentidadAdicional AND "IdTipoDocumentoIdentidad" = :v_IdTipoDocumentoIdentidadAdicional AND :v_DocumentoIdentidadAdicional <> ''
			AND "IdEmpresa" = :p_IdEmpresa;					
					
		if v_count_Existe=0 then
			SELECT "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL INTO v_IdSocioNegocio FROM DUMMY;
			SELECT "BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL INTO v_IdPersona FROM DUMMY;
			SELECT "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL INTO v_IdDocIdentidad FROM DUMMY;
			SELECT "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".NEXTVAL INTO v_IdDireccionFisicaPrincipal FROM DUMMY;
			SELECT "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".NEXTVAL INTO v_IdDireccionElectronicaPrincipal FROM DUMMY;
			SELECT "BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL INTO v_IdTelefonoPrincipal FROM DUMMY;
			
			SELECT top 1 "IdPais" into v_IdPaisCiudad
			FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad" CIU
			JOIN :p_ConsultarEnrolamiento ENR ON CIU."IdCiudad"=ENR."IdCiudad";

			--proy peru
			if(v_IdPaisCiudad!=44 and v_DocumentoIdentidadAdicional!='' and v_count_ExisteAdicional=0) then
				SELECT "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL INTO v_IdDocIdentidadAdicional FROM DUMMY;
			end if;
	
			--Inserta Persona
			INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona"  (
			        "IdPersona" ,
			        "IdEstado" ,
			        "IdDocumentoIdentidad" ,
			        "IdDocumentoIdentidadAdicional" ,
			        "Tipo" ,
			        "Nombres" ,
			        "ApellidoPaterno" ,
			        "ApellidoMaterno" ,
			        "RazonSocial" ,
			        "IdPaisNacimiento" ,
			        "IdPaisNacionalidadPrincipal",
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
			        v_IdPersona, --IdPersona
			        'ACT', --IdEstado
			        null, --IdDocumentoIdentidad
					null, --IdDocumentoIdentidadAdicional
			        CENR."Tipo", --Tipo
			        CENR."Nombres", --Nombres
			        CENR."ApellidoPaterno", --ApellidoPaterno
			        CENR."ApellidoMaterno", --ApellidoMaterno
			        CENR."RazonSocial", --RazonSocial
			        CASE CENR."IdPaisNacimiento" WHEN 0 THEN NULL ELSE CENR."IdPaisNacimiento" END, --IdPaisNacimiento
					CASE CENR."IdPaisNacimiento" WHEN 0 THEN NULL ELSE CENR."IdPaisNacimiento" END, --IdPaisNacionalidadPrincipal
			        CASE CIU."IdPais" WHEN 0 THEN NULL ELSE CIU."IdPais" END, --IdPaisResidencia
			        CASE Length(CENR."FechaNacimiento") WHEN 0 THEN NULL ELSE CENR."FechaNacimiento" END, --FechaNacimiento
			        "Sexo", --Sexo
			        CASE Length(CENR."EstadoCivil") WHEN 0 THEN NULL ELSE  CENR."EstadoCivil" END,
			        null, --IdDireccionFisicaPrincipal
			        null, --IdDireccionElectronicaPrincipal
			        null, --IdTelefonoPrincipal
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        v_EsEnrolado, --EsEnrolado
					p_IdUsuario --IdUsuarioCreador
			 from  :p_ConsultarEnrolamiento as CENR
			 left join "BSCL.Plataforma.BD::REG.Ciudad" CIU on (CIU."IdCiudad" = CENR."IdCiudad");
			 
			--Inserta Socio Negocio
			INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  (
			        "IdSocioDeNegocio" ,
			        "IdPersona" ,
			        "IdEmpresa"
			 ) VALUES (
			        v_IdSocioNegocio, --IdSocioDeNegocio
			        v_IdPersona, --IdPersona
			        p_IdEmpresa --IdEmpresa
			 );

			 --Inserta Persona Fatca
			INSERT INTO "BSCL.SocioNegocio.BD::PER.Fatca"  (
			        "IdPersona",
				 	"IdEstado",
				 	"IdClasificacionFatca",
				 	"IdClasificacionEmpresaFatca",
				 	"NumeroGIIN",
				 	"AutorizacionInformarOrganismo",
				 	"FechaIngreso",
				 	"FechaCreacion",
				 	"FechaModificacion",
				 	"IdUsuarioCreador",
				 	"IdUsuarioModificador"
			 ) select
			 		v_IdPersona,--"IdPersona"
				 	'ACT',--"IdEstado"
				 	'NOUSENTITY',--"IdClasificacionFatca"
				 	CASE CENR."Tipo" WHEN 'J' THEN 'PENDIENTE' ELSE '' END as "IdClasificacionEmpresaFatca",--"IdClasificacionEmpresaFatca"
				 	'',--"NumeroGIIN"
				 	'N',--"AutorizacionInformarOrganismo"
				 	v_fechaHoraSistema,--"FechaIngreso"
				 	CURRENT_UTCTIMESTAMP,--"FechaCreacion"
				 	CURRENT_UTCTIMESTAMP,--"FechaModificacion"
				 	p_IdUsuario,--"IdUsuarioCreador"
				 	p_IdUsuario--"IdUsuarioModificador"
			 from  :p_ConsultarEnrolamiento as CENR;
			
			--Inserta Documento Identidad
			insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
					"IdDocumentoIdentidad",
					"IdPersona",
					"IdTipoDocumentoIdentidad",
					"IdTipoDocumentoIdentidadAsociado",
					"DocumentoIdentidad",
					"IdEstadoAutorizacion",
					"FechaIngreso",
					"FechaCreacion",
					"FechaModificacion",
					"IdUsuarioCreador",
					"Predeterminado",
					"TipoPersona"  )
			select 	v_IdDocIdentidad,
					v_IdPersona,
					"IdTipoDocumentoIdentidad",
					"IdTipoDocumentoIdentidadAsociado",
					"DocumentoIdentidad",
					'ING',
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					p_IdUsuario, --IdUsuarioCreador
					'S',
					CASE WHEN v_IdPais = 44 THEN "Tipo" ELSE v_Tipo END
			from  :p_ConsultarEnrolamiento;

			if(v_IdDocIdentidadAdicional is not null and v_DocumentoIdentidadAdicional!='') then
				--Inserta Documento Identidad Adicional
				insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"(
						"IdDocumentoIdentidad",
						"IdPersona",
						"IdTipoDocumentoIdentidad",
						"DocumentoIdentidad",
						"IdEstadoAutorizacion",
						"FechaIngreso",
						"FechaCreacion",
						"FechaModificacion",
						"IdUsuarioCreador",
						"TipoPersona")
				select 	v_IdDocIdentidadAdicional,
						v_IdPersona,
						"IdTipoDocumentoIdentidadAdicional",
						"DocumentoIdentidadAdicional",
						'ING',
						CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						p_IdUsuario, --IdUsuarioCreador
						CASE WHEN v_IdPais = 44 THEN "Tipo" ELSE v_Tipo END					
				from  :p_ConsultarEnrolamiento;
			end if;
			
			--Inserta Direccion
			INSERT INTO "BSCL.SocioNegocio.BD::PER.DireccionFisica"  (
			        "IdDireccionFisica" ,
			        "Calle" ,
			        "Numero" ,
			        "Departamento" ,
			        "Sector" ,
			        "CodigoPostal" ,
			        "IdCiudad" ,
			        "IdDivisionPais",
			        "CodigoProposito" ,
			        "Comentario" ,
			        "IdEstado" ,
			        "FechaIngreso" ,
			        "FechaCreacion" ,
			        "FechaModificacion" ,
			        "IdPersona" ,
			        "IdEstadoAutorizacion",
			        "IdUsuarioCreador" 
			 ) select
			        v_IdDireccionFisicaPrincipal, --IdDireccionFisica
			        "DireccionCalle", --Calle
			        "DireccionNumero", --Numero
			        "DireccionDepartamento", --Departamento
			        '', --Sector
			        '', --CodigoPostal
			        "IdCiudad", --IdCiudad
					"IdDivisionPais", --IdDivisionPais
			        CASE "Tipo" WHEN 'J' THEN 'COME' ELSE 'PART' END as "CodigoProposito", --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        v_IdPersona, --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario --IdUsuarioCreador
			from  :p_ConsultarEnrolamiento;

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
			        "IdUsuarioCreador"
			 ) select
			        v_IdTelefonoPrincipal, --IdTelefono
			        v_IdPaisCiudad, --IdPais
					"IdTipoTelefono",
			        "Telefono", --Numero
			        '', --Anexo
			        CASE "Tipo" WHEN 'J' THEN 'COME' ELSE 'PART' END as "CodigoProposito", --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        v_IdPersona, --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario --IdUsuarioCreador
			 from  :p_ConsultarEnrolamiento;

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
			        "IdUsuarioCreador"
			 ) select
			        v_IdDireccionElectronicaPrincipal, --IdDireccionElectronica
			        "Email", --Direccion
			        CASE "Tipo" WHEN 'J' THEN 'COME' ELSE 'PART' END as "CodigoProposito", --CodigoProposito
			        '', --Comentario
			        'ACT', --IdEstado
			        CURRENT_UTCTIMESTAMP, --FechaIngreso
			        CURRENT_UTCTIMESTAMP, --FechaCreacion
			        CURRENT_UTCTIMESTAMP, --FechaModificacion
			        v_IdPersona, --IdPersona
			        'ING', --IdEstadoAutorizacion
					p_IdUsuario --IdUsuarioCreador
			  from  :p_ConsultarEnrolamiento;
			  
			  --Asignar telefono, direccion y correo a la persona (por llaves)
			  UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
			  SET 
			  	"IdDocumentoIdentidad" = v_IdDocIdentidad,
			  	"IdDocumentoIdentidadAdicional" = coalesce(v_IdDocIdentidadAdicional,0),
			  	"IdDireccionElectronicaPrincipal" = v_IdDireccionElectronicaPrincipal,
			  	"IdDireccionFisicaPrincipal" = v_IdDireccionFisicaPrincipal,
			  	"IdTelefonoPrincipal" = v_IdTelefonoPrincipal
			  WHERE "IdPersona" = v_IdPersona;
			  
		else
			--Persona ya existe
		--	select "IdSocioDeNegocio" into v_IdSocioNegocio
		--	from "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" where  "IdEmpresa" = p_IdEmpresa and "IdPersona" = v_IdPersona;
			
		select SOC."IdSocioDeNegocio",PER."IdPersona", PER."IdDireccionElectronicaPrincipal" into v_IdSocioNegocio,v_IdPersona,v_IdDireccionElectronicaPrincipal 
			from "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC INNER JOIN 
			"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (SOC."IdPersona"=PER."IdPersona")
			WHERE PER."DocumentoIdentidad" = :v_DocumentoIdentidad
			AND PER."TipoDocumentoIdentidad" = :v_IdTipoDocumentoIdentidad
			AND PER."Tipo" = (CASE WHEN v_IdPais = 44 THEN PER."Tipo" ELSE v_Tipo END)
			AND SOC."IdEmpresa" = p_IdEmpresa;
						
			--Verifica y advierte para el caso en que la persona ya sea Empleado de otra persona.
			v_count:=0;
			SELECT COUNT("IdPersonaEmpleador") INTO v_count FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
					 WHERE "IdPersona"=v_IdPersona AND "IdPersonaEmpleador">0;
						 
			IF(v_count>0) THEN --Establece advertencia
					p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
				    v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('TIENE_EMPLEADOR_RELACIONADO',:v_hash,:v_erroroutmsg);
					p_out_AdvertenciasDetalle =  select 'TIENE_EMPLEADOR_RELACIONADO' as "Codigo",'' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
				    select * from :p_out_AdvertenciasDetalle;
			END IF;

		end if;

		------------------------------------------
		--Inserta empleado
		------------------------------------------
		SELECT "BSCL.SocioNegocio.BD.Secuencias::Empleado".NEXTVAL INTO v_IdEmpleado FROM DUMMY;
	   
		INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado"  (
		        "IdEmpleado" ,
		        "IdPersona" ,
		        "IdEmpresa" ,
		        "IdEstado" ,
		 		"CodigoEmpleado",
				"CodigoTipoCargoEmpleado",
				"IdEmpleadoSupervisor",
				"IdSucursal",
				"FechaInicioCargo",
				"FechaTerminoCargo",
		        "FechaIngreso" ,
		        "FechaCreacion" ,
		        "FechaModificacion",
				"EsEnrolado",
				"IdUsuarioCreador",
				"CargoIndefinido"
				,"IdDireccionElectronica"
		 ) select
		        v_IdEmpleado, --IdEmpleado
		        v_IdPersona, --IdPersona
		        p_IdEmpresa, --IdEmpresa
		        'ACT', --IdEstado
		 		"EMP_CodigoEmpleado",
				"EMP_CodigoTipoCargoEmpleado",
				"EMP_IdEmpleadoSupervisor",
				"EMP_IdSucursal",
				"EMP_FechaInicioCargo",
				"EMP_FechaTerminoCargo",
		        CURRENT_UTCTIMESTAMP, --FechaIngreso
		        CURRENT_UTCTIMESTAMP, --FechaCreacion
		        CURRENT_UTCTIMESTAMP, --FechaModificacion
			    v_EsEnrolado, --EsEnrolado
				p_IdUsuario, --IdUsuarioCreador
				"EMP_CargoIndefinido"
				,v_IdDireccionElectronicaPrincipal
		 from  :p_ConsultarEnrolamiento;
		 
		------------------------------------------
		--Actualiza Socio Negocio con ID Empleado
		------------------------------------------
		update "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
		  set  "IdEmpleado" = v_IdEmpleado
		where  "IdSocioDeNegocio" = v_IdSocioNegocio;

		------------------------------------------
		--Actualiza Enrolamiento con IdEntidadEnrolada
		------------------------------------------
		update "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
		  set  "IdEntidadEnrolada" = v_IdEmpleado
		where "IdEnrolamiento" = v_IdEnrolamiento;

		--------------------------------
		--Empleado Creado
		--------------------------------
		p_out_RefId := v_IdEmpleado;

		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------

		if v_RequiereAutorizacion='S' then
				/*
				declare v_GlosaPers 						nvarchar(520) :='';
				declare v_out_idAuthPers 					bigint;
				declare v_out_AuthAdvertenciasContadorPers 	int;
				declare v_out_AuthErroresContadorPers 		int;
				*/

				declare v_Glosa 							nvarchar(520) :='';
				declare v_out_idAuth 						bigint;
				declare v_out_AuthAdvertenciasContador 		int;
				declare v_out_AuthErroresContador 			int;
				
				v_out_AuthAdvertenciasDetalle				= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle					= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			---------------------------------------------
			--   AUTORIZACION PARA CREACION DE PERSONA
			---------------------------------------------
			/*
			if v_count_Existe=0 then
				select CASE "Tipo" 
					WHEN 'J' THEN "DocumentoIdentidad" || ' / ' || "RazonSocial" 
					ELSE "DocumentoIdentidad" || ' / ' || "Nombres" || ' ' || "ApellidoPaterno" || ' ' || "ApellidoMaterno" END as "Nom" INTO v_GlosaPers
				from :p_ConsultarEnrolamiento;
				
				
				CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
					p_IdEmpresa					=> p_IdEmpresa,
					p_IdUsuario 				=> p_IdUsuario,
					p_IdModulo 					=> p_IdModulo,
					p_UsaReglasNegocio 			=> 'N',								
					p_IdTipoEvento 				=> 'ENRPER',
					p_IdModuloSolicita 			=> p_IdModulo,
					p_IdUsuarioSolicita 		=> p_IdUsuario,
					p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
					p_IdConcepto 				=>  'ENRPER',
					p_NumeroOperacionConcepto 	=>  CAST (v_IdPersona AS NVARCHAR(40)),
					p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
					p_IdCliente 				=> v_IdPersona,
					p_Glosa 					=> v_GlosaPers,
					--salida
					p_out_IdEvento 				=>  v_out_idAuthPers,
					p_out_AdvertenciasContador 	=>  v_out_AuthAdvertenciasContadorPers,
					p_out_AdvertenciasDetalle 	=>  v_out_AuthAdvertenciasDetalle,
					p_out_ErroresContador 		=>  v_out_AuthErroresContadorPers,
					p_out_ErroresDetalle 		=>  v_out_AuthErroresDetalle);				
				--adjunta mensajes
				p_out_AdvertenciasDetalle =  select * from :v_out_AuthAdvertenciasDetalle UNION ALL
										     select * from :p_out_AdvertenciasDetalle;
				p_out_ErroresDetalle      =  select * from :v_out_AuthErroresDetalle UNION ALL
										     select * from :p_out_ErroresDetalle;
				--No se pudo crear portafolio
				if 	p_out_ErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
				end if;		
			end if;	*/					

			---------------------------------------------
			--   AUTORIZACION PARA CREACION DE EMPLEADO
			---------------------------------------------
			select CASE "Tipo" 
				WHEN 'J' THEN "DocumentoIdentidad" || ' / ' || "RazonSocial" 
				ELSE "DocumentoIdentidad" || ' / ' || "Nombres" || ' ' || "ApellidoPaterno" || ' ' || "ApellidoMaterno" END as "Nom" INTO v_Glosa 
			from :p_ConsultarEnrolamiento;
			
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> 'N',								
				p_IdTipoEvento 				=> 'ENROLE',
				p_IdModuloSolicita 			=> p_IdModulo,
				p_IdUsuarioSolicita 		=> p_IdUsuario,
				p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
				p_IdConcepto 				=>  'ENROLE',
				p_NumeroOperacionConcepto 	=>  CAST (v_IdEmpleado AS NVARCHAR(40)),
				p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
				p_IdFuncionario             => v_IdEmpleado,
				p_IdCliente 				=> v_IdEmpleado,
				p_Glosa 					=> v_Glosa,
				--salida
				p_out_IdEvento 				=> v_out_idAuth,
				p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador 		=> v_out_AuthErroresContador,
				p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);				
			--adjunta mensajes
			p_out_AdvertenciasDetalle =  select * from :v_out_AuthAdvertenciasDetalle UNION ALL
									     select * from :p_out_AdvertenciasDetalle;
			p_out_ErroresDetalle      =  select * from :v_out_AuthErroresDetalle UNION ALL
									     select * from :p_out_ErroresDetalle;
			--No se pudo crear 
			if 	p_out_ErroresContador > 0 then
				SIGNAL SALIDA_CONTROLADA;
			end if;								
			--Mensaje de Salida
			v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
			p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									     select * from :p_out_AdvertenciasDetalle;
			SIGNAL SALIDA_CONTROLADA;
		end if;
	END;
END;