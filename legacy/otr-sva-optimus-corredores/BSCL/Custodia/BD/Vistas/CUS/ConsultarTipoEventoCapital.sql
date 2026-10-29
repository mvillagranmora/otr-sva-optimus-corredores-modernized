SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital"
AS 
SELECT
	"IdTipoEventoCapital",
	"Codigo",
	"Nombre",
	"IdEstado",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento"
FROM "BSCL.Custodia.BD::CUS.TipoEventoCapital";
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
GO