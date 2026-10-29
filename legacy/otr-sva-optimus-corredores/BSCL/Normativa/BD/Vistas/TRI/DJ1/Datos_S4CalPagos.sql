set schema "BSCL";
GO

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S4CalPagos";
GO

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S4CalPagos" AS
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
	"KS4_XR_NroFlu", 
	"S4_XR_TipoFlujo", 
	"S4_XR_FacTasa", 
	"S4_XR_SpreadTasa", 
	"S4_XR_TasaFija", 
	"S4_XR_FecFixTasa", 
	"S4_XR_FecIni", 
	"S4_XR_FecTer", 
	"S4_XR_MontoNoc", 
	"S4_XR_MontoInt", 
	"S4_X_MonMonto", 
	"UsrUltMod", 
	"FecUltMod", 
	"Estado", 
	"kProducto", 
	"kPeriodoComercial"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "Seccion" = 4
ORDER BY "ID";

GO
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S4CalPagos" to bcs_desarrollador with grant option;
