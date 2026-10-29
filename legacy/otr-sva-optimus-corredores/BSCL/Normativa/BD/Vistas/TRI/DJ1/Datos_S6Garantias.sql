SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S6Garantias";
GO

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S6Garantias" AS
SELECT
	"ID", 
	"IdDocumento",
	"VboUsuario", 
	"Observaciones", 
	"FuenteDatos", 
	"KRutCtraparte1", 
	"KIdContrato", 
	"KFechaContrato", 
	"KIdOpeEstruc", 
	"S6_TipoGar", 
	"S6_MonVal", 
	"KS6_IdGar", 
	"S6_MontoUmb", 
	"S6_ValGtia",
	"UsrUltMod", 
	"FecUltMod", 
	"Estado", 
	"kProducto",  
	"kPeriodoComercial"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "Seccion" = 6
ORDER BY "ID";

GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S6Garantias" TO bcs_desarrollador WITH GRANT OPTION;