SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::RequiereAutorizacionAutorizadoOrden";

CREATE PROCEDURE
--
--  Proposito: Servicio que encapsula la información de autorizados de un portafolio
--             Retorna S/N si requeire autorizacion
--  Autor: CFLORES - BCS
--  Fecha Creacion: 02/02/2016
--
"BSCL.Portafolio.BD.Servicios::RequiereAutorizacionAutorizadoOrden" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca
	-- Parametros entrada
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
	--TODO: Escribir IF para los casos que se van a requerir autorizaciones
	p_out_Autorizacion := 'N';

END;
