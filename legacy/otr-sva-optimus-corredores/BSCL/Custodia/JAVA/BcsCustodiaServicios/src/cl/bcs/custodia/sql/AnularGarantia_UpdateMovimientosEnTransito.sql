UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET "IdEstado"='ANU', "FechaAnulacionRegistro" = :FechaAnulacionRegistro, "IdUsuarioAnulacion" = :IdUsuarioAnulacion
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito;