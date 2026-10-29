UPDATE "BSCL.Custodia.BD::CUS.PatrimonioCustodia"
SET "IdEstadoComision"= 'INA'
WHERE "IdPortafolio" = :IdPortafolio AND "IdEstadoComision" = 'VIG'
AND "FechaPatrimonio" BETWEEN :FechaDesde AND :FechaHasta