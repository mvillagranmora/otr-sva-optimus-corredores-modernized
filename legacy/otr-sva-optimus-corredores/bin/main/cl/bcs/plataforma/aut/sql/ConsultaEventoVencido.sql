SELECT EV."IdEvento"
FROM "BSCL.Plataforma.BD::AUT.Evento" as EV
JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" as TEV ON EV."IdTipoEvento" =  TEV."IdTipoEvento" AND TEV."IdEmpresa" = :IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" as USU ON EV."IdUsuario" = USU."IdUsuario"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON USU."IdPersona" = EMP."IdPersona" AND EMP."IdEmpresa" = :IdEmpresa
WHERE EV."IdEstado" = 'VIG' AND "FechaHoraVigencia" < :FechaHoraVigencia
