SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionModificarEstadoPersona";

CREATE PROCEDURE
--
--  Proposito: Servicio que encapsula la información para modificar el estado de una persona
--             Retorna S/N si requeire autorizacion
--  Autor: CFLORES - BCS
--  Fecha Creacion: 29/12/2015
--
"BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionModificarEstadoPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca
	-- Parametros entrada
	IN  p_IdEstado						NVARCHAR(3),
	IN 	p_IdPersona 					BIGINT,
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
