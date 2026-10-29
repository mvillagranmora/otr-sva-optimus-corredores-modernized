SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CambioNemo";
-- 
--  Proposito: Ajuste de ordenes por cambio de nemo
--  Autor: - RULLOA - BCS
--  Fecha Creacion: 31/01/2017
--  
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CambioNemo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	IN p_IdInstrumento				BIGINT 			DEFAULT 0,
	IN p_IdInstrumentoNuevo			BIGINT 			DEFAULT 0, 
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN

	p_out_ErroresContador		:= 0;
	
	--aqui hay q hacer algo Renzo

END;
