SELECT USU."IdUsuario", USU."IdEmpresa", USU."CodigoUsuario", USU."Clave", USU."FechaIngreso", USU."FechaVigencia", USU."FechaUltimaConexion", USU."DiasVencimientoClave", USU."Conectado", USU."Hora", USU."Terminal",
USU."IdEstado", USU."IntentosConexion", USU."FechaBloqueoTemporal", USU."TipoEntidad", USU."IdEntidad", USU."EsAutorizado", USU."UsuarioCorredores", USU."UsuarioNegocios", USU."UsuarioClientes", USU."UsuarioServicios",
USU."IdTipoBloqueo", USU."ComentarioBloqueo", TBSN."NombreTipoBloqueo"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" TBSN ON TBSN."IdTipoBloqueo" = USU."IdTipoBloqueo"
WHERE USU."CodigoUsuario" = :CodigoUsuario AND USU."IdEmpresa" = :IdEmpresa;