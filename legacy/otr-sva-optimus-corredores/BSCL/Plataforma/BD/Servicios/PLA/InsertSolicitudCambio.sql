SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.InsertSolicitudCambio";

CREATE PROCEDURE  
-- 
--  Proposito: Servicio Interno para crear nueva linea de detalle de solicitud de cambio
--  Autor: JGARCES - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::PLA.InsertSolicitudCambio" (
	-- Parametros entrada comunes en todos los servicios publicos

	IN p_IdSolicitudCambio		NVARCHAR(40),	
	IN p_IdTipoEntidad			NVARCHAR(40),
	IN p_IdEntidadOrigen		BIGINT,
	IN p_CampoModificado		NVARCHAR(255),
	IN p_TipoDato				NVARCHAR(255),
	IN p_ValorAntiguo			NVARCHAR(255),
	IN p_ValorNuevo				NVARCHAR(255),
	IN p_IdEstadoAutorizacion	NVARCHAR(3)	,
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId bigint
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	BEGIN	 
	
		DECLARE v_secuencia_IdSecuenciaCambio 	BIGINT;
		
		SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio".NEXTVAL into v_secuencia_IdSecuenciaCambio from dummy;		
		
		INSERT INTO "BSCL.Plataforma.BD::PLA.SolicitudCambio"(
			"IdSolicitudCambio",
			"IdSecuenciaCambio",
			"IdTipoEntidad",
			"IdEntidadOrigen",
			"CampoModificado",
			"TipoDato",
			"ValorAntiguo",
			"ValorNuevo",
			"FechaSolicitud",
			"FechaRevision",
			"IdEstadoAutorizacion")
		VALUES (
			:p_IdSolicitudCambio,
			:v_secuencia_IdSecuenciaCambio,
			:p_IdTipoEntidad,
			:p_IdEntidadOrigen,
			:p_CampoModificado,
			:p_TipoDato,
			:p_ValorAntiguo,
			:p_ValorNuevo,
			CURRENT_UTCTIMESTAMP,
			'0001-01-01',
			p_IdEstadoAutorizacion
		);
		p_out_RefId := v_secuencia_IdSecuenciaCambio;	
	END;	
END;