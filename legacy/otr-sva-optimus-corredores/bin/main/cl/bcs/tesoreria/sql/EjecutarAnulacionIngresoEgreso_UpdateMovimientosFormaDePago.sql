UPDATE MovForDePago
SET "IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" As MovForDePago
WHERE MovForDePago."IdIngresoEgreso" = :IdIngresoEgreso;