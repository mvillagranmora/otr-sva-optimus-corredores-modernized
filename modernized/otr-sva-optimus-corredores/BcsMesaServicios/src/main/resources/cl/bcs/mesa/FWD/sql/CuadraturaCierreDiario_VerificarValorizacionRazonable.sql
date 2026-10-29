SELECT COUNT(1) AS "Estado"
FROM "BSCL.Plataforma.BD::PAU.ControlProceso"
WHERE "CodigoModulo" =  'MFW'
AND "CodigoProceso" =  'VALORIZACION_FWD'
AND "FechaSistema" = :Fecha
AND "IdEmpresa" = :IdEmpresa;