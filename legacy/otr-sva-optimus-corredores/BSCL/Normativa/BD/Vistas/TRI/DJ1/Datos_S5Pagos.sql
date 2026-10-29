SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S5Pagos";
GO

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S5Pagos" AS
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
	"KS5_CorrPago", 
	"KS5_IdPago", 
	"S5_FecPago", 
	"S5_MontoPago",
	"S5_TipoPago", 
	"S5_CodMoneda",
	"UsrUltMod", 
	"FecUltMod", 
	"Estado", 
	"kProducto", 
	"kPeriodoComercial"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "Seccion" = 5
ORDER BY "ID";


GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos_S5Pagos" TO bcs_desarrollador WITH GRANT OPTION;