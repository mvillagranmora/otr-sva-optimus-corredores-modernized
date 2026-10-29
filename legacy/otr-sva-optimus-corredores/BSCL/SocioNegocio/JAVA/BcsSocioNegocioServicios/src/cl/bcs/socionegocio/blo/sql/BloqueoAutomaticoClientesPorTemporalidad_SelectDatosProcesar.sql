SELECT "IdCliente",
       "FechaDesbloqueo",
       "FechaDesbloqueoTemporal"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente"
WHERE "IdEstado" = 'ACT'
  AND "IdTipoDesbloqueo" = 'DESBTEMPORAL'
  AND "IdEmpresa" = :IdEmpresa
  AND "FechaDesbloqueoTemporal" <= :FechaSistema;