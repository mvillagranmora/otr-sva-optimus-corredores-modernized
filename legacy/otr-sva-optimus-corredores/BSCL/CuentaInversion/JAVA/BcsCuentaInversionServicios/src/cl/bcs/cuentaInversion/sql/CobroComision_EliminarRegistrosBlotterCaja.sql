UPDATE "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja"
SET "IdEstadoComision"= 'INA'
WHERE "IdPortafolio" = :IdPortafolio AND "IdEstadoComision" = 'VIG'
AND "FechaPatrimonio" BETWEEN :FechaDesde AND :FechaHasta