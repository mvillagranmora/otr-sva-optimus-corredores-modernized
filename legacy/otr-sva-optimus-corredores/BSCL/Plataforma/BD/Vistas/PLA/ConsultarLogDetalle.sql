SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle"  
AS
SELECT 	"LogDetalle"."IdDetalleLog"		,
		"LogDetalle"."IdLog"			, 
		"LogDetalle"."SubRegistro"		, 
		"LogDetalle"."CampoRegistro"	,
		"LogDetalle"."ValorAntiguo"		, 
		"LogDetalle"."ValorNuevo"		
FROM "BSCL.Plataforma.BD::PLA.DetalleLog" AS "LogDetalle";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
