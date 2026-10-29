DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "KRutCtraparte1" = :KRutCtraparte1
AND "KIdContrato" = :KIdContrato
AND	"KFechaContrato" = :KFechaContrato
AND	"KIdOpeEstruc" = :KIdOpeEstruc
AND	"kProducto" = :kProducto
AND	"kPeriodoComercial" = :kPeriodoComercial
AND "KS7_IdGar" = :KS7_IdGar
AND "Seccion" = 7;