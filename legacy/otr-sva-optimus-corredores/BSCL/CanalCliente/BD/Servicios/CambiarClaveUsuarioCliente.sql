SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.CanalCliente.BD.Servicios::CambiarClaveUsuarioCliente";
-- 
--  Proposito: Servicio para la actualizacion de la clave de un usuario tipo Cliente.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 20/12/2016
--  
CREATE PROCEDURE "BSCL.CanalCliente.BD.Servicios::CambiarClaveUsuarioCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_Token						NVARCHAR(520),
	IN p_Username					NVARCHAR(100),
	
	-- Parametro de entrada para actualizar la clave
	IN  p_Clave						nvarchar(520), 
	IN  p_ClaveNueva				nvarchar(520), 

	-- Parametros salida del servicio publico
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
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			p_out_StatusCode := 3;								
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
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
	
		DECLARE p_out_update NVARCHAR(5);
		DECLARE p_out_AdvertenciasContador int;
		DECLARE p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE p_out_ErroresContador int;
		DECLARE p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		
		p_out_StatusCode := 0;
		p_out_StatusMessage := '';
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
		--select IdEmpresa AS "IdEmpresa", IdUsuario AS "IdUsuario", NombreUsuario AS "NombreUsuario", TipoUsuario AS "TipoUsuario", TokenValido AS "TokenValido", Mensaje AS "Mensaje" FROM DUMMY;
		
		IF TokenValido = 'S' AND Mensaje = 'OK++' THEN
			IF UPPER(p_Username) = UPPER(NombreUsuario) THEN
				
				CALL "BSCL.Plataforma.BD.Servicios::ACC.CambiarClaveUsuario"(
					p_IdEmpresa => IdEmpresa,
					p_IdUsuario => IdUsuario,
					p_IdModulo => 'PLA',
					p_Clave => p_Clave,
					p_ClaveNueva => p_ClaveNueva,
					
					p_out_update => :p_out_update,
					p_out_AdvertenciasContador =>:p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle =>:p_out_AdvertenciasDetalle,
					p_out_ErroresContador =>:p_out_ErroresContador,
					p_out_ErroresDetalle =>:p_out_ErroresDetalle
				);
				
				IF :p_out_ErroresContador > 0 THEN
					p_out_StatusCode := 1;
					SELECT TOP 1 "Descripcion" INTO p_out_StatusMessage FROM :p_out_ErroresDetalle;				
				ELSE
					p_out_StatusCode := 0;
					p_out_StatusMessage := 'OK++';
				END IF;
				
			ELSE
				p_out_StatusCode := 1;
				p_out_StatusMessage := 'USUARIO_INVALIDO';
			END IF;
			
		ELSE
			p_out_StatusCode := 1;
			p_out_StatusMessage := Mensaje;
		END IF;
	END;
END;
