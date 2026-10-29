-- Inserta en tabla global los valores a eliminar
INSERT INTO "BSCL.Normativa.BD::GBL.EliminarCalendarioPago_RegistroElimina"
("KRutCtraparte1", "KIdContrato", "KFechaContrato", "KIdOpeEstruc", "kProducto", "kPeriodoComercial", "KS4_XR_NroFlu")
VALUES (:KRutCtraparte1, :KIdContrato, :KFechaContrato, :KIdOpeEstruc, :kProducto, :kPeriodoComercial, :KS4_XR_NroFlu);