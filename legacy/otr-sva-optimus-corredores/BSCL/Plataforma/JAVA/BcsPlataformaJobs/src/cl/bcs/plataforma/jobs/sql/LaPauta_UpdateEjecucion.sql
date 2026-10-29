UPDATE "BSCL.Plataforma.BD::PAU.Ejecucion"
SET "FechaFin" = :FechaFin,
"Estado" = :Estado,
"RutaLog" = :RutaLog
WHERE "IdEmpresa" = :IdEmpresa AND "FechaSistema" = :FechaSistema
