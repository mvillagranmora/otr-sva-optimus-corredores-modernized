SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CanalCliente.BD.Servicios::CerrarSesionUsuarioCliente";

CREATE PROCEDURE
-- 
--  Proposito: Cierra una sesion de usuario tipo cliente
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 21/12/2016
--  
"BSCL.CanalCliente.BD.Servicios::CerrarSesionUsuarioCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_Token						NVARCHAR(520),
		
	-- Salida Correcta
	OUT p_out_StatusCode			INT,
	OUT p_out_StatusMessage			NVARCHAR(2000)
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE IdEmpresa BIGINT;
		DECLARE IdUsuario BIGINT;
		DECLARE NombreUsuario NVARCHAR(100);
		DECLARE TipoEntidad NVARCHAR(3);
		DECLARE IdEntidad BIGINT;
		DECLARE TokenValido NVARCHAR(1);
		DECLARE Mensaje NVARCHAR(80);
			
		DECLARE p_out_estado				NVARCHAR(5);
			 
		-- Salidas para mensajes de advertencias y errores
		DECLARE p_out_AdvertenciasContador int;
		DECLARE p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE p_out_ErroresContador int;
		DECLARE p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		-----------------------------------------------------
		--Logica de negocio
		-----------------------------------------------------
		CALL "BSCL.CanalCliente.BD.Servicios::ValidarTokenUsuarioCliente"(
			p_Token => p_Token,
			-- Salida Info sesion
			p_out_IdEmpresa => IdEmpresa,
			p_out_IdUsuario => IdUsuario,
			p_out_NombreUsuario => NombreUsuario,
			p_out_TipoEntidad => TipoEntidad,
			p_out_IdEntidad => IdEntidad,			
			p_out_TokenValido => TokenValido,
			p_out_Mensaje => Mensaje
		);
		
		IF TokenValido = 'S' THEN		
			CALL "BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario"(
				p_IdEmpresa => IdEmpresa,
				p_IdUsuario => IdUsuario,
				p_IdModulo => 'PLA',
				p_Token => p_Token,
				-- Salida
				p_out_estado => :p_out_estado,
				p_out_AdvertenciasContador => :p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle => :p_out_AdvertenciasDetalle,
				p_out_ErroresContador => :p_out_ErroresContador,
				p_out_ErroresDetalle => :p_out_ErroresDetalle
			);
			
			IF p_out_ErroresContador = 0 AND p_out_estado = 'ok+++' THEN
				p_out_StatusCode := 0;
				p_out_StatusMessage := 'OK++';
			ELSE
				p_out_StatusCode := 1;
				SELECT TOP 1 "Descripcion" INTO p_out_StatusMessage FROM :p_out_ErroresDetalle;				
			END IF;
			
		ELSE
			p_out_StatusCode := 2;
			p_out_StatusMessage := Mensaje;
		END IF;
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
