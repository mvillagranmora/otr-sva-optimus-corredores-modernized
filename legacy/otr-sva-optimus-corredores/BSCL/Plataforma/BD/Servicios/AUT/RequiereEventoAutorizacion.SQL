SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion";
CREATE PROCEDURE
--
--  Proposito: Servicio que encapsula la información de parametros segun el concepto del Evento
--             Retorna S/N si requeire autorizacion
--  Autor: CFLORES - BCS
--  Fecha Creacion: 24/03/2016
--
--  Proposito: Se incorpora IdEmpresa para la búsqueda
--  Autor: JGarces - BCS
--  Fecha Creacion: 28/02/2017
--
"BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca
	IN P_RegistroSolicitudCambio "BSCL.Plataforma.BD::PLA.SolicitudCambio",
	IN  p_IdConcepto	       				NVARCHAR(6),														
	-- Parametros salida
	OUT p_out_Autorizacion  	 	 	 	nvarchar(1) -- S/N
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--TODO: Escribir busqueda del parametro segun concepto del evento
	DECLARE v_count				BIGINT:=0;
	p_out_Autorizacion := '';
	
	SELECT COUNT(1) into v_count
		FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"
		WHERE "IdConcepto" = :p_IdConcepto and "IdEmpresa" = :p_IdEmpresa; 
	
	IF (v_count = 0) THEN
			p_out_Autorizacion := 'N';
	ELSE				
    	SELECT COALESCE("RequiereAutorizacion",'N') into p_out_Autorizacion
		FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"
		WHERE "IdConcepto" = :p_IdConcepto  and "IdEmpresa" = :p_IdEmpresa; 
 	END IF;
END;
