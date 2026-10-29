SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaDireccionElectronica";

CREATE PROCEDURE
--
--  Proposito: Servicio que encapsula la información de atributos a modificar una direccion
--             Retorna S/N si requeire autorizacion
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17/12/2015
--
"BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaDireccionElectronica" (
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
	IF p_IdConcepto='MODDIE' THEN		--MODIFICAR DOCUMENTO
		p_out_Autorizacion := 'N';
	END IF;
	
	IF p_IdConcepto='NUEDIE' THEN 		--NUEVO DOCUMENTO
		p_out_Autorizacion := 'N';
	END IF;	
END;
