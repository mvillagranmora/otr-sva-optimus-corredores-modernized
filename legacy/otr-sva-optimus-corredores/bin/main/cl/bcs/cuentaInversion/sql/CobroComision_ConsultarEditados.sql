SELECT "IdRegistro", "EstadoComision", "IdPortafolio"
FROM "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones"
WHERE "IdPortafolio" IN (:IdPortafolio) AND "Periodo" = :Periodo;