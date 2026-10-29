SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CrearSucursal";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de Sucursales.
--  Autor: - RULLOA
--  Fecha Creacion: 29/07/2015
--  
"BSCL.Plataforma.BD.Servicios::CrearSucursal" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					bigint,  		--ID del Modulo que lo invoca 
	
	IN  p_NombreSucursal			nvarchar(520),  	
	IN  p_CodigoSucursal			nvarchar(520),		
	

	-- Parametros salida del servicio publico
	OUT p_out_RefId bigint--,
		
	-- Salidas para mensajes de advertencias y errores
	--OUT p_out_AdvertenciasContador int,
	--OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	--OUT p_out_ErroresContador int,
	--OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN

	SELECT "BSCL.Plataforma.BD.Secuencias::Sucursal".NEXTVAL INTO p_out_RefId FROM DUMMY;
	
	insert into "BSCL.Plataforma.BD::PLA.Sucursal" 
	("IdSucursal"
	,"IdEmpresa"
	,"CodigoSucursal"
	,"NombreSucursal"
	,"EstadoSucursal")
	values
	(p_out_RefId
	,p_IdEmpresa
	,p_CodigoSucursal
	,p_NombreSucursal
	,'ACT');


END;
