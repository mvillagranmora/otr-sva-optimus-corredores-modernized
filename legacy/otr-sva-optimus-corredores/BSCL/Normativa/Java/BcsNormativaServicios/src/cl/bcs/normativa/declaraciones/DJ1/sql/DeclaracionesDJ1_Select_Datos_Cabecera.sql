SELECT
	"IdDocumento",
	"VboUsuario",
	"Observaciones",
	"KRutCtraparte1",
	"KIdContrato",
	"KFechaContrato",
	"HoraContrato",
	"KIdOpeEstruc",
	"kPeriodoComercial",
	"kProducto",
	"Rectifica"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" 
WHERE "KRutCtraparte1" = :KRutCtraparte1
AND "KIdContrato" = :KIdContrato
AND	"KFechaContrato" = :KFechaContrato
AND	"KIdOpeEstruc" = :KIdOpeEstruc
AND	"kProducto" = :kProducto
AND	"kPeriodoComercial" = :kPeriodoComercial
AND "Seccion" = 1;
