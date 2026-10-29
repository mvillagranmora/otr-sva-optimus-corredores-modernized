SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ValidarConfirmarNotificacion";
 
CREATE PROCEDURE
-- 
--  Proposito: Valida que no se haya confirmado la notificación y que los ultimos digitos del rut coincidan
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   

 
"BSCL.Plataforma.BD.Servicios::EVT.ValidarConfirmarNotificacion"
 (
									IN p_Token 				NVARCHAR(520),
  									IN p_Rut			 	NVARCHAR(4),
 									
 									
 									OUT p_Template			NCLOB,
 									OUT p_Json 				TABLE ("Llave" NVARCHAR(40),"Valor" NVARCHAR(2000),"CodigoTipoEvento" NVARCHAR(40), "Agrupa" NVARCHAR(1)),
 									OUT p_TagPred			TABLE ("Llave" NVARCHAR(40),"Valor" NVARCHAR(2000),"CodigoTipoEvento" NVARCHAR(40)),
 									OUT p_Salida 			BIGINT,
 									OUT p_MensajeSalida 	NVARCHAR(520)
 								 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	DECLARE p_documentoIdentidad NVARCHAR(30);
	DECLARE p_IdEmpresa   BIGINT;
	DECLARE p_IdUsuario   BIGINT;
	DECLARE p_contador 	  BIGINT;
	DECLARE v_Template    NCLOB;
	p_documentoIdentidad := '';
	p_IdUsuario := -1;

	SELECT "Template" INTO v_Template FROM 
		"BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillaPorNotificacion"
	WHERE "TokenConfirmacion" = p_Token ;
	
	p_Template := v_Template;
	
	p_Json = 
	SELECT DISTINCT "Llave", "Valor", "CodigoTipoEvento",cppn."Agrupa"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalle" ced
	 	INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion" cepc ON cepc."IdConfiguracionEvento" = ced."IdConfiguracionEvento"
	 	INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillaPorNotificacion" cppn ON (cppn."IdConfiguracionEvento" = ced."IdConfiguracionEvento") and ced."Agrupa" = cppn."Agrupa"
	WHERE "TokenConfirmacion" = p_Token;
	
	p_TagPred = 
	SELECT DISTINCT "Llave", "Valor", "CodigoTipoEvento"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalle" ced
	WHERE "IdConfiguracionEvento" = 0;

	SELECT 
		DISTINCT SUBSTRING (SUBSTR_BEFORE (cp."DocumentoIdentidad",'-'),LENGTH(SUBSTR_BEFORE (cp."DocumentoIdentidad",'-'))-3,4) as "documentoIdentidad"
		INTO  p_documentoIdentidad
	FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" cn 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" ccevtdet ON ccevtdet."IdConfiguracionEvento" = cn."IdConfiguracionEvento"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" evt ON evt."CodigoTipoEvento" = ccevtdet."CodigoTipoEvento"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" cp ON cp."IdPersona" = evt."IdPersonaDestinatario"
	WHERE "TokenConfirmacion" = p_Token and SUBSTRING (SUBSTR_BEFORE ("DocumentoIdentidad",'-'),LENGTH(SUBSTR_BEFORE ("DocumentoIdentidad",'-'))-3,4) = p_Rut;
 	
	IF :p_documentoIdentidad = p_Rut THEN	
	  		p_Salida := 1;
	 		p_MensajeSalida := 'Ingreso Coreccto';
	ELSE 
			p_Salida := -1;
	 		p_MensajeSalida := 'Código no coincide';
	END IF;

END;
