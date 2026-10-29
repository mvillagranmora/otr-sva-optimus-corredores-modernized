-- Inserta en tabla global los valores a modificar
INSERT INTO "BSCL.Normativa.BD::GBL.ModificarGarantia_RegistroCambio"
("RutContraparte", "IdContrato", "FechaContrato", "IdOperacion", "Producto", "PeriodoComercial", "IdGarantia", "IdUsuario", "CampoModificado", "ValorNuevo")
VALUES (:RutContraparte, :IdContrato, :FechaContrato, :IdOperacion, :Producto, :PeriodoComercial,:IdGarantia, :IdUsuario, :CampoModificado, :ValorNuevo);