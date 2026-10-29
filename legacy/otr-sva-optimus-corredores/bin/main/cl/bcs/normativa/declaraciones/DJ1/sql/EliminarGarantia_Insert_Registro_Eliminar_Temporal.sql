-- Inserta en tabla global los valores a eliminar
INSERT INTO "BSCL.Normativa.BD::GBL.EliminarGarantia_RegistroElimina"
("KRutCtraparte1", "KIdContrato", "KFechaContrato", "KIdOpeEstruc", "kProducto", "kPeriodoComercial", "KS6_IdGar")
VALUES (:KRutCtraparte1, :KIdContrato, :KFechaContrato, :KIdOpeEstruc, :kProducto, :kPeriodoComercial, :KS6_IdGar);