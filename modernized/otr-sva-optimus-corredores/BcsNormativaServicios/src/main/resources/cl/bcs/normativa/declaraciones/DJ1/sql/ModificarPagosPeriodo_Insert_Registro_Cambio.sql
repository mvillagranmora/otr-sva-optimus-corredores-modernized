-- Inserta en tabla global los valores a modificar
INSERT INTO "BSCL.Normativa.BD::GBL.ModificarPagosPeriodo_RegistroCambio"
("RutContraparte", "IdContrato", "FechaContrato", "IdOperacion", "Producto", "PeriodoComercial", "CorrelativoPago", "IdPago", "IdUsuario", "CampoModificado", "ValorNuevo")
VALUES (:RutContraparte, :IdContrato, :FechaContrato, :IdOperacion, :Producto, :PeriodoComercial, :CorrelativoPago, :IdPago, :IdUsuario, :CampoModificado, :ValorNuevo);