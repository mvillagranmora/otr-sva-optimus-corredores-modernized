-- Inserta en tabla global los valores a modificar
INSERT INTO "BSCL.Normativa.BD::GBL.ModificarCalendarioPago_RegistroCambio"
("RutContraparte", "IdContrato", "FechaContrato", "IdOperacion", "Producto", "PeriodoComercial", "NumeroFlujo", "IdUsuario", "CampoModificado", "ValorNuevo")
VALUES (:RutContraparte, :IdContrato, :FechaContrato, :IdOperacion, :Producto, :PeriodoComercial, :NumeroFlujo, :IdUsuario, :CampoModificado, :ValorNuevo);