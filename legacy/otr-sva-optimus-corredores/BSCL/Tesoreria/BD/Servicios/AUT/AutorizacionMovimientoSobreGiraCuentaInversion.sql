SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::AUT.AutorizacionMovimientoSobreGiraCuentaInversion";
GO
CREATE PROCEDURE  
-- 
--  Proposito: Autoriza movimientos de tesoreria que sobregiran la cuenta inversion
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 14/12/2017
-- 
"BSCL.Tesoreria.BD.Servicios::AUT.AutorizacionMovimientoSobreGiraCuentaInversion" (
    IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.	
	 
	IN 	p_IngresoEgreso					TABLE ("IdIngresoEgreso"	BIGINT),	
	IN  p_IdEstadoAutorizacion			NVARCHAR(3),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	
	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador 	   = 0;
	
  	 
  	IF (:p_IdEstadoAutorizacion = 'AUT') THEN
		UPDATE IE 
		SET "IdEstado" = 'ING'    -- Para de "Pendiente de Autorizacion" a "Ingresado"
		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"  AS IE
		INNER JOIN :p_IngresoEgreso AS AUT ON (AUT."IdIngresoEgreso" = IE."IdIngresoEgreso");
	ELSE
		UPDATE IE 
		SET "IdEstado" = 'ANU'
		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"  AS IE
		INNER JOIN :p_IngresoEgreso AS AUT ON (AUT."IdIngresoEgreso" = IE."IdIngresoEgreso");
	END IF;
END;
