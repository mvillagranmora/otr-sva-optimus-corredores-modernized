SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto"
AS
SELECT
       	USU."IdUsuario",
		USU."IdEmpresa",
		USU."CodigoUsuario",
		'*****' AS "Clave",
		USU."FechaIngreso",
		USU."FechaVigencia",
		USU."FechaUltimaConexion",
		USU."DiasVencimientoClave",
		USU."Conectado",
		USU."Hora",
		USU."Terminal",
		USU."IdEstado",
		USU."IntentosConexion",
		USU."FechaBloqueoTemporal",
		USU."EsAutorizado",
		(CASE WHEN "FechaBloqueoTemporal" > CURRENT_UTCTIMESTAMP THEN 'S' ELSE 'N' END) AS "TieneBloqueoTemporal",
		USU."TipoEntidad",
		USU."IdEntidad",
		USU."DebeCambiarClave",
		USU."UltimoCambioClave",
		PER."IdPersona",
		PER."NombreCompleto",
		PER."RazonSocial",
		PER."TipoDocumentoIdentidad",
		PER."DocumentoIdentidad",
		EPL."Descripcion" as "DescripcionEstado",
		TSN."NombreTipoSocioDeNegocio",
		USU."UsuarioCorredores",
		USU."UsuarioNegocios",
		USU."UsuarioClientes",
		USU."UsuarioServicios",
		USU."EsUsuarioBolsa",
		USU."IdTipoBloqueo",
		(CASE WHEN USU."IdEstado" = 'BLO' THEN USU."ComentarioBloqueo" ELSE '' END) AS "ComentarioBloqueo",
		(CASE WHEN USU."IdEstado" = 'BLO' THEN TBSN."NombreTipoBloqueo" ELSE '' END) AS "NombreTipoBloqueo"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
ON (SOC."IdEmpleado" = USU."IdEntidad" AND USU."TipoEntidad" = 'EMP')
	OR  (SOC."IdCliente" = USU."IdEntidad" AND USU."TipoEntidad" = 'CLI')
	OR	(SOC."IdOperador" = USU."IdEntidad" AND USU."TipoEntidad" = 'OPE')
	OR	(SOC."IdProveedor" = USU."IdEntidad" AND USU."TipoEntidad" = 'PRO')
	OR	(SOC."IdPersona" = USU."IdEntidad" AND USU."TipoEntidad" = 'PER')
JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" =  SOC."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL ON EPL."IdEstado" = USU."IdEstado" AND EPL."NombreEntidad" = 'USUARIO'
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" TSN ON TSN."IdTipoSocioDeNegocio" = USU."TipoEntidad"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" TBSN ON TBSN."IdTipoBloqueo" = USU."IdTipoBloqueo"
ORDER BY USU."IdUsuario";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
