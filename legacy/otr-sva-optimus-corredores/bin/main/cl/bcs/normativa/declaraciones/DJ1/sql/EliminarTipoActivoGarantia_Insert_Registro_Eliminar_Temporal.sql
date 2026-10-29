-- Inserta en tabla global los valores a eliminar
INSERT INTO "BSCL.Normativa.BD::GBL.EliminarTipoActivoGarantia_RegistroElimina"
("KRutCtraparte1", "KIdContrato", "KFechaContrato", "KIdOpeEstruc", "kProducto", "kPeriodoComercial", "KS7_IdGar")
VALUES (:KRutCtraparte1, :KIdContrato, :KFechaContrato, :KIdOpeEstruc, :kProducto, :kPeriodoComercial, :KS7_IdGar);