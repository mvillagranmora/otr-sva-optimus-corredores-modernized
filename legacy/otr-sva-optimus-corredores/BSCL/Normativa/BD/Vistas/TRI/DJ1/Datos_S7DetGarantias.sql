SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S7DetGarantias";
GO

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S7DetGarantias" AS
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
	"S7_TipoGar", 
	"KS7_IdGar", 
	"S7_TipoActGar", 
	"S7_PorcGar", 
	"UsrUltMod", 
	"FecUltMod", 
	"Estado", 
	"kProducto",  
	"kPeriodoComercial"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "Seccion" = 7
ORDER BY "ID";

GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S7DetGarantias" TO bcs_desarrollador WITH GRANT OPTION;