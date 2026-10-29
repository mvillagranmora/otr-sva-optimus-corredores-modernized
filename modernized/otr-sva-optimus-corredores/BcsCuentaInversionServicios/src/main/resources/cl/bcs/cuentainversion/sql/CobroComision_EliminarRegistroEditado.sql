UPDATE "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones" 
SET "EstadoComision"= 'INA'
WHERE "IdPortafolio" = :IdPortafolio AND "EstadoComision" = 'VIG' AND "Periodo" = :Periodo;