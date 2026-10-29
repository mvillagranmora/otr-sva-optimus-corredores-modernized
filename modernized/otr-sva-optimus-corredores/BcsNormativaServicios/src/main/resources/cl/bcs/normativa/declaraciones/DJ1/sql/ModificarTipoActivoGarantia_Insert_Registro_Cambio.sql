-- Inserta en tabla global los valores a modificar
INSERT INTO "BSCL.Normativa.BD::GBL.ModificarTipoActivoGarantia_RegistroCambio"
("RutContraparte", "IdContrato", "FechaContrato", "IdOperacion", "Producto", "PeriodoComercial", "IdGarantia", "IdUsuario", "CampoModificado", "ValorNuevo")
VALUES (:RutContraparte, :IdContrato, :FechaContrato, :IdOperacion, :Producto, :PeriodoComercial, :IdGarantia, :IdUsuario, :CampoModificado, :ValorNuevo);