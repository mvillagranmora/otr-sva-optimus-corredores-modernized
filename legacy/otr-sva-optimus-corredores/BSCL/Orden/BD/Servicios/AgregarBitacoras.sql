SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::AgregarBitacoras";

CREATE PROCEDURE
-- 
--  Proposito: agregar bitacora
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015 
--     
"BSCL.Orden.BD.Servicios::AgregarBitacoras" ( 
	p_IdEmpresa					BIGINT,
	p_Registros				TABLE(
								"NombreEntidad"				NVARCHAR(100),
								"IdEntidad"					BIGINT,
								"IdOrden"					BIGINT,
								"IdAsignacion"				BIGINT,
								"IdTransaccion"				BIGINT,
								"MensajeEventoBitacora"		NVARCHAR(5000),
								"IdTipoEventoBitacora"		NVARCHAR(40)
								)


) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL 
AS
BEGIN

	DECLARE v_FechaSistemaSeconddate SECONDDATE;
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,:v_FechaSistemaSeconddate);
  	 
 	INSERT INTO "BSCL.Orden.BD::ORD.EventoBitacora"
 	("IdEventoBitacora","NombreEntidad","IdEntidad","IdOrden","IdAsignacion","IdTransaccion","MensajeEventoBitacora","FechaEventoBitacora","FechaIngresoRegistro","IdTipoEventoBitacora")
	SELECT 
	"BSCL.Orden.BD.Secuencias::EventoBitacora".NEXTVAL,"NombreEntidad","IdEntidad","IdOrden","IdAsignacion","IdTransaccion","MensajeEventoBitacora",:v_FechaSistemaSeconddate,CURRENT_TIMESTAMP,"IdTipoEventoBitacora"
	FROM :p_Registros;
		
END;
 
