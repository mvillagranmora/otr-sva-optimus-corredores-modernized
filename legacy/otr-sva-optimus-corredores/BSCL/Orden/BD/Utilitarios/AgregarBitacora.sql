SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Utilitarios::AgregarBitacora";

CREATE PROCEDURE
-- 
--  Proposito: agregar bitacora
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015 
--     
"BSCL.Orden.BD.Utilitarios::AgregarBitacora" ( 
	p_IdEmpresa					BIGINT,
	p_NombreEntidad				NVARCHAR(100),
	p_IdEntidad					BIGINT,
	p_IdOrden					BIGINT,
	p_IdAsignacion				BIGINT,
	p_IdTransaccion				BIGINT,
	p_MensajeEventoBitacora		NVARCHAR(5000),
	p_IdTipoEventoBitacora		NVARCHAR(40)


) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL 
AS
BEGIN
	
	v_Registros = 	SELECT	:p_NombreEntidad as "NombreEntidad",:p_IdEntidad as "IdEntidad",:p_IdOrden as "IdOrden",:p_IdAsignacion as "IdAsignacion",:p_IdTransaccion as "IdTransaccion",
							:p_MensajeEventoBitacora as "MensajeEventoBitacora",:p_IdTipoEventoBitacora as "IdTipoEventoBitacora"
					FROM DUMMY;
	
	CALL "BSCL.Orden.BD.Servicios::AgregarBitacoras" (:p_IdEmpresa,:v_Registros);		
END;
 
