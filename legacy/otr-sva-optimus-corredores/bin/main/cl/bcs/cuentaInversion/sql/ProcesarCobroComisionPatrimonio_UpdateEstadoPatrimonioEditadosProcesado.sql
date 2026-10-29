UPDATE "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones"
SET "EstadoComision" = 'PRO'
WHERE "IdPortafolio" = :IdPortafolio AND "Periodo" = :IdPeriodo AND "EstadoComision" = 'ACT';