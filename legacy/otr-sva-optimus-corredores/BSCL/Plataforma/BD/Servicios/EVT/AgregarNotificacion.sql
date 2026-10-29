SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacion";
GO
CREATE PROCEDURE
-- 
--  Proposito: Agrega una notificacion Masivo gatillada por un evento
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   

"BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacion"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
 									IN p_IdConfiguracion	table("IdConfiguracionEvento" BIGINT, "IdEvento" BIGINT),
 								 
 									
 									OUT p_msgout			NVARCHAR(520),
 									out p_IdNotificacion    TABLE("IdNotificacion" BIGINT, "IdEvento" BIGINT, "IdConfiguracionEvento" BIGINT)
								
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
 
 
 		declare v_fechaManual SECONDDATE;
		declare fechaHoraUTC SECONDDATE;
		declare v_fechaDespacho NVARCHAR(40);
		declare p_token NVARCHAR(2000);
		DECLARE SegundosZonaHoraria BIGINT;
		
		p_token := '';
		
		
		SELECT "Valor" into SegundosZonaHoraria FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.SegundosZonaHoraria' AND "IdEmpresa" = p_IdEmpresa;
	
		SELECT TO_CHAR(HASH_SHA256(TO_VARBINARY(CURRENT_UTCTIMESTAMP||''))) INTO p_token FROM DUMMY;
		
		select CURRENT_UTCTIMESTAMP into fechaHoraUTC from dummy;
		select to_char(fechaHoraUTC,'YYYY-MM-DD') INTO v_fechaDespacho FROM DUMMY; 
		v_fechaManual := '0001-01-01 00:00:00';
		
		v_IdNotificacion = select "BSCL.Plataforma.BD.Secuencias::EVT.Notificacion".nextval as "IdNotificacion", "IdConfiguracionEvento", "IdEvento" from :p_IdConfiguracion; 
 		
		INSERT INTO "BSCL.Plataforma.BD::EVT.Notificacion" 
		(
		"IdEmpresa",
		"IdNotificacion",
		"IdConfiguracionEvento",
		"FechaCreacion",
		"FormaDespacho",
		"TiempoDespacho",
		"FechaDespachoOrginal",
		"FechaDespachoRecordatorio",
		"ConfirmacionDespacho",
		"TokenConfirmacion",
		"Enviar")
		select
			p_IdEmpresa,
			p_conf."IdNotificacion",
			p_conf."IdConfiguracionEvento", 
			fechaHoraUTC,
			conf."FormaDespacho" ,
			conf."TiempoDespacho",
			  CASE conf."TiempoDespacho" WHEN 'I' THEN fechaHoraUTC 
			WHEN 'M' THEN v_fechaManual WHEN 'H' THEN  ADD_SECONDS(v_fechaDespacho || ' '|| conf."FechaDespacho", SegundosZonaHoraria) END ,
			conf."FechaRecordatorio",
			conf."ConfirmacionDespacho",
			 CASE conf."ConfirmacionDespacho" 
			 		WHEN 'S' THEN p_token
			 		WHEN 'N' THEN ''
			 END,
			 CASE WHEN conf."ControlProceso" = 'S'  THEN 'N' ELSE 'S' END
		 FROM :v_IdNotificacion p_conf
		 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" conf ON conf."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento";
		 
		 
		 p_IdNotificacion = select "IdNotificacion", "IdEvento", "IdConfiguracionEvento" from :v_IdNotificacion;

 
END;