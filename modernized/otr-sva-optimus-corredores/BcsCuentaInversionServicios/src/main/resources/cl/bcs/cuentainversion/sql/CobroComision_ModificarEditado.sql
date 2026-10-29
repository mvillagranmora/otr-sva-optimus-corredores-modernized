UPDATE "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones"
SET  "MontoComision"= :MontoComision,  "FechaModificacion"= :FechaModificacion, "EstadoComision" = 'VIG'
WHERE "IdRegistro"= :IdRegistro;