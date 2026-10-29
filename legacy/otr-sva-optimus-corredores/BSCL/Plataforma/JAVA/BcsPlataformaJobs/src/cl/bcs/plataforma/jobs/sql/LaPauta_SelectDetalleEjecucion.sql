SELECT "FechaSistema","IdEmpresa","CodigoProceso","FechaInicio","FechaFin","Estado"
FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle"
WHERE "FechaSistema" = :FechaSistema AND "IdEmpresa" = :IdEmpresa
ORDER BY "IdEjecucion" ASC;

