SELECT
	TO_NVARCHAR(base64_encode(to_char("IdEmpresa")||':'||"CodigoUsuario"||':'||cast(CURRENT_TIMESTAMP as varchar(80)) || ':' || SYSUUID )) AS "Token",
	"IdUsuario",
	"IdEmpresa",
	"CodigoUsuario",
	"EsAutorizado",
	"TieneBloqueoTemporal",
	"TipoEntidad",
	"DebeCambiarClave",
	"DescripcionEstado",
	"UsuarioClientes"
FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" WHERE "CodigoUsuario" = :CodigoUsuario AND "IdEmpresa" = :IdEmpresa;