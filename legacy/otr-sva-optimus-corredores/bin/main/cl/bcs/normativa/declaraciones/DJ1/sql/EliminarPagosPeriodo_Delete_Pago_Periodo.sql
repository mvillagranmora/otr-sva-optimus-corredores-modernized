DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1"
WHERE "KRutCtraparte1" = :KRutCtraparte1
AND "KIdContrato" = :KIdContrato
AND	"KFechaContrato" = :KFechaContrato
AND	"KIdOpeEstruc" = :KIdOpeEstruc
AND	"kProducto" = :kProducto
AND	"kPeriodoComercial" = :kPeriodoComercial
AND "KS5_CorrPago" = :KS5_CorrPago
AND "KS5_IdPago" = :KS5_IdPago
AND "Seccion" = 5;