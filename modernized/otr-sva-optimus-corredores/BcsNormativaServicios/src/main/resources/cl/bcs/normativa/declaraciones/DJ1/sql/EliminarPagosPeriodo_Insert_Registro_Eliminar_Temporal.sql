-- Inserta en tabla global los valores a eliminar
INSERT INTO "BSCL.Normativa.BD::GBL.EliminarPagoPeriodo_RegistroElimina"
("KRutCtraparte1", "KIdContrato", "KFechaContrato", "KIdOpeEstruc", "kProducto", "kPeriodoComercial", "KS5_CorrPago", "KS5_IdPago")
VALUES (:KRutCtraparte1, :KIdContrato, :KFechaContrato, :KIdOpeEstruc, :kProducto, :kPeriodoComercial, :KS5_CorrPago, :KS5_IdPago);