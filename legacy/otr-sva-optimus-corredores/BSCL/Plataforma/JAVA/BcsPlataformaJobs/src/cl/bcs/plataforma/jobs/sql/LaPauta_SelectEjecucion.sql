SELECT "IdEmpresa","FechaSistema","FechaInicio","FechaFin","Estado"
FROM "BSCL.Plataforma.BD::PAU.Ejecucion" WHERE "FechaSistema" = :FechaSistema AND "IdEmpresa" = :IdEmpresa
