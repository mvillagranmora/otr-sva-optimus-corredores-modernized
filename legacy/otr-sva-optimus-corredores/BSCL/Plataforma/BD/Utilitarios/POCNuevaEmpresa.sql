SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::POCNuevaEmpresa";
CREATE PROCEDURE 
-- JG crear nueva empresa basica (usuario,empleado,perfilamiento básico)
"BSCL.Plataforma.BD.Utilitarios::POCNuevaEmpresa" (
	in p_IdPais					bigint,
	in p_IdEmpresa				bigint,
	in p_CodigoEmpresa	   	 	nvarchar(40),
	in p_NombreEmpresa			nvarchar(100),
	in p_RutEmpleadoMaster 		nvarchar(40),
	in p_IdEmpresaCopiarConfiguracion bigint,
	in p_NombreRol				nvarchar(100) default 'ROL INICIAL',
	in p_NombrePerfil			nvarchar(100) default 'PERFIL INICIAL',
	in p_SobreescribirEmpresa	nvarchar(1) default 'N',
	--
	out p_out_Usuario 			nvarchar(100),
	out p_out_Clave 			nvarchar(10),
	out	p_out_IdSocioNegocio	bigint,
	out p_out_IdPersona 		bigint,
	out p_out_IdEmpleado 		bigint,
	out p_out_Mensaje			nvarchar(5000)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
--READS SQL DATA
AS
BEGIN
	DECLARE EMPRESA_EXISTE CONDITION; 
	DECLARE EXIT HANDLER FOR EMPRESA_EXISTE
	BEGIN
		p_out_Mensaje := 'Empresa existe. Para sobreescribir utilice parametro p_SobreescribirEmpresa';
	END;
	BEGIN --PROCEDURE
		/*******************
		VARIABLES
		********************/
		declare v_IdUsuario 		bigint;
		declare v_IdRol				bigint;
		declare v_IdPerfil			bigint;
		declare v_IdRolUsuario 		bigint;
		declare v_IdRolPerfil		bigint;
		declare v_IdSocioNegocio 	bigint;
		declare v_IdPersona			bigint;
		declare v_IdDocIdentidad	bigint;
		declare v_IdDireccionFisicaPrincipal bigint:=0;
		declare v_IdDireccionElectronicaPrincipal bigint:=0;
		declare v_IdTelefonoPrincipal bigint:=0;
		declare v_IdCiudad			bigint:=0;
		declare v_IdEmpleado		bigint:=0;
		declare v_NombreEmpleadoMaster 	nvarchar(100):='EMPLEADO MASTER';
		declare v_CodigoEmpleadoMaster 	nvarchar(40):='MAMASTER';
		declare v_NombreUsuarioMaster 	nvarchar(100):='MAMASTER';
		declare v_countEmpresaExiste bigint:=0;
		/*******************
		  VALIDACION INICIAL
		********************/
		select count(1) into v_countEmpresaExiste from "BSCL.Plataforma.BD::PLA.Configuracion" where "IdEmpresa" = :p_IdEmpresa;
		if(p_SobreescribirEmpresa='N' and v_countEmpresaExiste>0) then
			SIGNAL EMPRESA_EXISTE;
		end if;
		
		/*******************
		  OBTENCION DE IDs
		********************/
		select  
			"BSCL.Plataforma.BD.Secuencias::Usuario".nextval,
			"BSCL.Plataforma.BD.Secuencias::Rol".nextval,
			"BSCL.Plataforma.BD.Secuencias::Perfil".nextval,
			"BSCL.Plataforma.BD.Secuencias::RolUsuario".nextval,
			"BSCL.Plataforma.BD.Secuencias::RolPerfil".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::Persona".nextval,
		 	"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::DireccionFisica".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::Telefono".nextval,
			"BSCL.SocioNegocio.BD.Secuencias::Empleado".nextval
		into v_IdUsuario,v_IdRol,v_IdPerfil,v_IdRolUsuario,v_IdRolPerfil,
			v_IdSocioNegocio,v_IdPersona,v_IdDocIdentidad,v_IdDireccionFisicaPrincipal,v_IdDireccionElectronicaPrincipal,v_IdTelefonoPrincipal,v_IdEmpleado
		from dummy;
		select top 1 "IdCiudad" into v_IdCiudad from "BSCL.Plataforma.BD::REG.Ciudad" where "IdPais"=:p_IdPais and "NombreCiudad">'';
		
		/***************************
		 QUITAR REGISTROS DE EXISTIR
		***************************/
		delete from "BSCL.Plataforma.BD::PLA.Configuracion" where "IdEmpresa" = :p_IdEmpresa;
		delete from "BSCL.Plataforma.BD::ACC.Sesion" where "IdUsuario" in (
			select "IdUsuario" from "BSCL.Plataforma.BD::ACC.Usuario"  
			where "CodigoUsuario" = :v_NombreUsuarioMaster and "IdEmpresa"=:p_IdEmpresa);
		delete from "BSCL.Plataforma.BD::ACC.RolPerfil" where "IdPerfil" in 
		(select "IdPerfil" from  "BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = :p_NombrePerfil);
		delete from "BSCL.Plataforma.BD::ACC.RolUsuario" where "IdUsuario" in (
			select "IdUsuario" from "BSCL.Plataforma.BD::ACC.Usuario"  
			where "CodigoUsuario" = :v_NombreUsuarioMaster and "IdEmpresa"=:p_IdEmpresa);
		delete from "BSCL.Plataforma.BD::ACC.Rol" where "Nombre" = :p_NombreRol;
		delete from "BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = :p_NombrePerfil;
		delete from "BSCL.Plataforma.BD::ACC.Usuario"  where "CodigoUsuario" = :v_NombreUsuarioMaster and "IdEmpresa"=:p_IdEmpresa;
		delete from "BSCL.Plataforma.BD::PLA.Empresa" where "IdEmpresa" = :p_IdEmpresa;
				
		/*******************
		 INSERCION DATOS
		********************/
		-- EMPRESA
		insert into "BSCL.Plataforma.BD::PLA.Empresa"("IdEmpresa","Codigo","Nombre","IdEstado","IdPais","IdSocioDeNegocio" ) 
			values(:p_IdEmpresa,:p_CodigoEmpresa,:p_NombreEmpresa,'ACT',:p_IdPais,0);
		-- USUARIO
		insert into "BSCL.Plataforma.BD::ACC.Usuario"("IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave","Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado" )  
			values(:v_IdUsuario,:p_IdEmpresa,:v_NombreUsuarioMaster,'4F04F35BEE3556C6E80572D9174BEA77ADB0FE153D2F018AD6AECD21D4BF7633', '2015-07-28','2099-12-30','2015-07-28',100.00000000,'S','17:15:12','Terminal Nro','VIG',0,'0001-01-01 00:00:00.000000000', 'EMP', :v_IdEmpleado, 'S');
		--PERFILAMIENTO
		insert into "BSCL.Plataforma.BD::ACC.Perfil"("IdPerfil","IdEmpresa","Nombre") 
			values (:v_IdPerfil,:p_IdEmpresa,:p_NombrePerfil);
		insert into "BSCL.Plataforma.BD::ACC.Rol"("IdRol","IdEmpresa","Nombre","IdEstado") 
			values (:v_IdRol,:p_IdEmpresa,:p_NombreRol,'ACT');
		insert into "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario","IdUsuario","IdRol","EsAutorizado") 
			values (:v_IdRolUsuario,:v_IdUsuario,:v_IdRol,'S');
		insert into "BSCL.Plataforma.BD::ACC.RolPerfil" ("IdRolPerfil","IdRol","IdPerfil","IdEstado") 
			values (:v_IdRolPerfil,:v_IdRol,:v_IdPerfil,'ACT');
		insert into "BSCL.Plataforma.BD::ACC.Sesion" ( "IdTokenSesion","IdUsuario","TokenValido","FechaUltimoUso","UsoToken","FechaIngreso","FechaSalida" )  
			values('MTp1c3VhcmlvMTo1NURFOERCNTQ4N0QwREVCRTIwMEQ3NkE3OEQ4MTRGRg==',:v_IdUsuario,'S','2015-08-26 23:35:14.596000000',2,'2015-08-26 23:35:04.698000000','0001-01-01 00:00:00.000000000');
		insert into "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil","IdEmpresa","IdPerfil","IdAcceso","IdEstado")
			select "BSCL.Plataforma.BD.Secuencias::PerfilAcceso".nextval,:p_IdEmpresa,:v_IdPerfil,"IdAcceso",'ACT' from "BSCL.Plataforma.BD::ACC.Acceso";
		-- CONFIGURACION
		insert into "BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa","CodigoParametro","Descripcion","Valor")
			select :p_IdEmpresa,"CodigoParametro","Descripcion","Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "IdEmpresa" = :p_IdEmpresaCopiarConfiguracion;
		-- EMPLEADO MASTER
		insert into "BSCL.SocioNegocio.BD::PER.Persona"("IdPersona","IdEstado","IdDocumentoIdentidad","Tipo","Nombres","ApellidoPaterno","ApellidoMaterno","RazonSocial",
			"IdPaisNacimiento","IdPaisNacionalidadPrincipal","IdPaisResidencia","FechaNacimiento","IdDireccionFisicaPrincipal",
			"IdDireccionElectronicaPrincipal","IdTelefonoPrincipal","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador","Sexo")
			select
				:v_IdPersona, 						--IdPersona
				'ACT', 								--IdEstado
			  	null, 								--IdDocumentoIdentidad
			  	'J', 								--Tipo
				:v_NombreEmpleadoMaster, 			--Nombres
				'', 								--ApellidoPaterno
			  	'', 								--ApellidoMaterno
			  	:v_NombreEmpleadoMaster, 			--RazonSocial
			   	:p_IdPais, 							--IdPaisNacimiento
				:p_IdPais, 							--IdPaisNacionalidadPrincipal
			 	:p_IdPais, 							--IdPaisResidencia
			   	'1980-01-01', 						--FechaNacimiento
			   	null, 								--IdDireccionFisicaPrincipal
			  	null, 								--IdDireccionElectronicaPrincipal
			  	null, 								--IdTelefonoPrincipal
			   	CURRENT_UTCTIMESTAMP, 				--FechaIngreso
			  	CURRENT_UTCTIMESTAMP, 				--FechaCreacion
			    CURRENT_UTCTIMESTAMP, 				--FechaModificacion
			  	'S', 								--EsEnrolado
				:v_IdUsuario, 						--IdUsuarioCreador
				''									--Sexo
			from dummy;
		insert into "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"("IdSocioDeNegocio","IdPersona","IdEmpleado","IdEmpresa")  
			values (:v_IdSocioNegocio,:v_IdPersona,:v_IdEmpleado,:p_IdEmpresa);
		insert into "BSCL.SocioNegocio.BD::PER.Fatca" ("IdPersona","IdEstado","IdClasificacionFatca","IdClasificacionEmpresaFatca","NumeroGIIN",
			"AutorizacionInformarOrganismo","FechaIngreso","FechaCreacion","FechaModificacion","IdUsuarioCreador","IdUsuarioModificador")
			select
		 		:v_IdPersona,			--"IdPersona"
			 	'ACT',					--"IdEstado"
			 	'NOUSENTITY',			--"IdClasificacionFatca"
			 	'PENDIENTE',			--"IdClasificacionEmpresaFatca"
			 	'',						--"NumeroGIIN"
			 	'N',					--"AutorizacionInformarOrganismo"
			 	CURRENT_UTCTIMESTAMP,	--"FechaIngreso"
			 	CURRENT_UTCTIMESTAMP,	--"FechaCreacion"
			 	CURRENT_UTCTIMESTAMP,	--"FechaModificacion"
			 	:v_IdUsuario,			--"IdUsuarioCreador"
			 	:v_IdUsuario			--"IdUsuarioModificador"
			 from dummy;
		insert into "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"("IdDocumentoIdentidad","IdPersona","IdTipoDocumentoIdentidad",
			"DocumentoIdentidad","IdEstadoAutorizacion","FechaIngreso","FechaCreacion","FechaModificacion","IdUsuarioCreador")
			select 	
				v_IdDocIdentidad,
				v_IdPersona,
				'R',
				:p_RutEmpleadoMaster,
				'ING',
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP,
				:v_IdUsuario
			from dummy;
		insert into "BSCL.SocioNegocio.BD::PER.DireccionFisica"("IdDireccionFisica","Calle","Numero","Departamento","Sector","CodigoPostal","IdCiudad","IdDivisionPais",
			"CodigoProposito","Comentario","IdEstado","FechaIngreso","FechaCreacion","FechaModificacion","IdPersona","IdEstadoAutorizacion","IdUsuarioCreador") 
			select
		        :v_IdDireccionFisicaPrincipal, 	--IdDireccionFisica
		        '', 							--Calle
		        0, 								--Numero
		        '', 							--Departamento
		        '', 							--Sector
		        '', 							--CodigoPostal
		        v_IdCiudad, 					--IdCiudad
				0, 								--IdDivisionPais
		        'COME', 						--CodigoProposito
		        '', 							--Comentario
		        'ACT', 							--IdEstado
		        CURRENT_UTCTIMESTAMP, 			--FechaIngreso
		        CURRENT_UTCTIMESTAMP, 			--FechaCreacion
		        CURRENT_UTCTIMESTAMP, 			--FechaModificacion
		        :v_IdPersona, 					--IdPersona
		        'ING', 							--IdEstadoAutorizacion
				:v_IdUsuario 					--IdUsuarioCreador
			from  dummy;
		insert into "BSCL.SocioNegocio.BD::PER.Telefono"("IdTelefono","IdPais","IdTipoTelefono","Numero","Anexo","CodigoProposito","Comentario",
			"IdEstado","FechaIngreso","FechaCreacion","FechaModificacion","IdPersona","IdEstadoAutorizacion","IdUsuarioCreador") 
			select
		        :v_IdTelefonoPrincipal, --IdTelefono
		        :p_IdPais, 				--IdPais
				'FIJO',					--IdTipoTelefono
		        '0', 					--Numero
		        '', 					--Anexo
		        'COME', 				--CodigoProposito
		        '', 					--Comentario
		        'ACT', 					--IdEstado
		        CURRENT_UTCTIMESTAMP, 	--FechaIngreso
		        CURRENT_UTCTIMESTAMP, 	--FechaCreacion
		        CURRENT_UTCTIMESTAMP, 	--FechaModificacion
		        :v_IdPersona, 			--IdPersona
		        'ING', 					--IdEstadoAutorizacion
				:v_IdUsuario 			--IdUsuarioCreador
			from dummy;
		insert into "BSCL.SocioNegocio.BD::PER.DireccionElectronica"("IdDireccionElectronica","Direccion","CodigoProposito","Comentario",
			"IdEstado","FechaIngreso","FechaCreacion","FechaModificacion","IdPersona","IdEstadoAutorizacion","IdUsuarioCreador") 
			select
		        :v_IdDireccionElectronicaPrincipal, --IdDireccionElectronica
		        'hola@empresa.com', 				--Direccion
		        'COME', 							--CodigoProposito
		        '', 								--Comentario
		        'ACT', 								--IdEstado
		        CURRENT_UTCTIMESTAMP, 				--FechaIngreso
		        CURRENT_UTCTIMESTAMP, 				--FechaCreacion
		        CURRENT_UTCTIMESTAMP, 				--FechaModificacion
		        :v_IdPersona, 						--IdPersona
		        'ING', 								--IdEstadoAutorizacion
				v_IdUsuario 						--IdUsuarioCreador
			from dummy;
		INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdEmpleado","IdPersona","IdEmpresa","IdEstado","CodigoEmpleado","CodigoTipoCargoEmpleado","IdEmpleadoSupervisor",
			"IdSucursal","FechaInicioCargo","FechaTerminoCargo","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdUsuarioCreador","CargoIndefinido") 
			select
		        v_IdEmpleado, 			--IdEmpleado
		        v_IdPersona, 			--IdPersona
		        p_IdEmpresa, 			--IdEmpresa
		        'ACT', 					--IdEstado
		 		:v_CodigoEmpleadoMaster,
				'ADMIN',
				null,
				null,
				'2017-01-01',
				null,
		        CURRENT_UTCTIMESTAMP, 	--FechaIngreso
		        CURRENT_UTCTIMESTAMP, 	--FechaCreacion
		        CURRENT_UTCTIMESTAMP, 	--FechaModificacion
			    'S', 					--EsEnrolado
				:v_IdUsuario, 			--IdUsuarioCreador
				'S'
		 from dummy;
		 
		 update  "BSCL.SocioNegocio.BD::PER.Persona"
		 set
		 	"IdDocumentoIdentidad" = :v_IdDocIdentidad,
			"IdDireccionFisicaPrincipal" = :v_IdDireccionFisicaPrincipal,
			"IdDireccionElectronicaPrincipal" = :v_IdDireccionElectronicaPrincipal,
			"IdTelefonoPrincipal" = :v_IdTelefonoPrincipal
		 where "IdPersona" = :v_IdPersona;
		/*******************
	 		SALIDA
		********************/
		p_out_Usuario:=v_NombreUsuarioMaster;
		p_out_Clave:='xbw810';
		p_out_IdSocioNegocio:=v_IdSocioNegocio;
		p_out_IdPersona:=v_IdPersona;
		p_out_IdEmpleado:=v_IdEmpleado;
		p_out_Mensaje:='Empresa creada. Recuerde actualizar los datos al ingresar al sistema';
	END; --PROCEDURE  		 	
END;