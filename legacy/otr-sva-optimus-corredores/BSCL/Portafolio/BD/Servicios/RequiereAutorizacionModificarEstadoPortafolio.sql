SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::RequiereAutorizacionModificarEstadoPortafolio";

CREATE PROCEDURE
--
--  Proposito: 
--  Autor: JGARCES - BCS
--  Fecha Creacion: 25/01/2016
--
"BSCL.Portafolio.BD.Servicios::RequiereAutorizacionModificarEstadoPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),    --ID del Modulo que lo invoca
	-- Parametros entrada
	IN P_RegistroSolicitudCambio 	"BSCL.Plataforma.BD::PLA.SolicitudCambio",
	IN  p_IdConcepto	       		NVARCHAR(6),														
	-- Parametros salida
	OUT p_out_Autorizacion 			NVARCHAR(1) -- S/N
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--TODO: Escribir IF para los casos que se van a requerir autorizaciones
	p_out_Autorizacion := 'N';
	IF p_IdConcepto='CAEPOR' THEN		--MODIFICAR USO PORTAFOLIO
		p_out_Autorizacion := 'N';
	END IF;
END;
