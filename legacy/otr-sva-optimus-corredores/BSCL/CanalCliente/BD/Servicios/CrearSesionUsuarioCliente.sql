SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CanalCliente.BD.Servicios::CrearSesionUsuarioCliente";

CREATE PROCEDURE 
-- 
--  Proposito: Crea una sesion de usuario tipo socio negocio cliente
--  Autor: - BCS
--  Fecha Creacion: 19/12/2016
--  
"BSCL.CanalCliente.BD.Servicios::CrearSesionUsuarioCliente" (
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_Username			NVARCHAR(100),
	IN p_Password			NVARCHAR(520),
	
	-- Salida Correcta
	OUT p_out_StatusCode	INT,
	OUT p_out_DisplayName	NVARCHAR(310),
	OUT p_out_Id			BIGINT,
	OUT p_out_ClientId		BIGINT,
	OUT p_out_Category		NVARCHAR(100),
	OUT p_out_Accounts		TABLE("Account" BIGINT, "IdAccount" BIGINT),	
	OUT p_out_token			NVARCHAR(520)
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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		p_out_StatusCode:=3;
	END;
	
	

	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------	
		DECLARE v_TipoEntidad	NVARCHAR(3);
		DECLARE v_UsuarioBloqueado INT;
		DECLARE v_DebeCambiarClave NVARCHAR(1);
		DECLARE v_token			NVARCHAR(520);
				
		DECLARE p_out_AdvertenciasContador int;
		DECLARE p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE p_out_ErroresContador int;
		DECLARE p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
		p_out_Accounts = select 0 as "Account", 0 as "IdAccount" FROM DUMMY WHERE 1=2;
		p_out_StatusCode := 0;
		-----------------------------------------------------
		--Logica de negocio
		-----------------------------------------------------			
		CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearSesionUsuario"(
			p_IdEmpresa => p_IdEmpresa,
			p_NombreUsuario => p_Username,
			p_ClaveUsuario => p_Password,
			p_IdCanal		=> 'CLIENTES',
			p_out_token => :v_token,
			p_out_entidad => :v_TipoEntidad,
			p_out_AdvertenciasContador => :p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle => :p_out_AdvertenciasDetalle,
			p_out_ErroresContador => :p_out_ErroresContador,
			p_out_ErroresDetalle => :p_out_ErroresDetalle
		);
		--select * from :p_out_ErroresDetalle;
		IF p_out_ErroresContador > 0 OR v_TipoEntidad <> 'CLI' THEN
			SELECT COUNT("Codigo") INTO v_UsuarioBloqueado FROM :p_out_ErroresDetalle WHERE "Codigo" = 'USUARIO_BLOQUEADO';
			IF v_UsuarioBloqueado > 0 THEN
				p_out_StatusCode := 1;
			ELSE
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
		ELSE
			p_out_token := v_token;
			--verificar si debe cambiar clave, continuar con la busqueda de los datos del cliente.
			SELECT "IdUsuario", "IdEntidad", "DebeCambiarClave" INTO p_out_Id, p_out_ClientId, v_DebeCambiarClave FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" WHERE "CodigoUsuario" = p_Username;
			IF v_DebeCambiarClave = 'S' THEN
				p_out_StatusCode := 2;
			END IF;
			
			SELECT CC."NombreCompleto", CC."NombreCategoriaInversionista" INTO p_out_DisplayName, p_out_Category
			FROM "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
			WHERE CC."IdCliente" = p_out_ClientId AND CC."IdEmpresa" = p_IdEmpresa;
			
			p_out_Accounts = SELECT "CodigoPortafolio" AS "Account", "IdPortafolio" AS "IdAccount"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" P
			WHERE P."IdCliente" = p_out_ClientId;
			
		END IF;
	END;	
END;
