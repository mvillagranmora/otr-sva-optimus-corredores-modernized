SELECT "IdEmpresa","IdControlProceso","CodigoModulo","CodigoProceso","FechaSistema","FechaRegistro","CodigoUsuario" 
FROM "BSCL.Plataforma.BD::PAU.ControlProceso" 
WHERE "CodigoProceso" = 'CUADRATURA_MRF' AND "FechaSistema" = :FechaSistema