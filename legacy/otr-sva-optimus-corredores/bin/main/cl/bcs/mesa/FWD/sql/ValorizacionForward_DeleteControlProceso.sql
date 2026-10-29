DELETE FROM "BSCL.Plataforma.BD::PAU.ControlProceso"
WHERE "IdEmpresa" = (:IdEmpresa)
  AND "CodigoModulo" = 'MFW'
  AND "CodigoProceso" = 'VALORIZACION_FWD'
  AND "FechaSistema" = (:FechaProceso);
