INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente"  (
	"IdCliente" ,
	"IdPersona" ,
	"IdEmpresa" ,
	"IdEstado" ,
	"IdSegmentoCliente" ,
	"IdCategoriaInversionista" ,
	"IdUsuarioCreador" ,
	"FechaIngreso" ,
	"FechaCreacion" ,
	"FechaModificacion" ,
	"EsEnrolado" ,
	"IdTipoCliente",
	"IdPlantillaLimiteExtra",
	"IdPersonaEjecutivo",
	"CodigoAdicionalHolding",
	"IdClaseCliente",
	"EsClienteFinal",
	"TipoEnrolamiento"
)VALUES(
	:IdCliente, --IdCliente
	:IdPersona, --IdPersona
	:IdEmpresa, --IdEmpresa
	:IdEstado, --IdEstado
	:IdSegmentoCliente, --IdSegmentoCliente
	:CLI_IdCategoriaInversionista, --IdCategoriaInversionista
	:IdUsuario, --IdUsuarioCreador
	CURRENT_UTCTIMESTAMP, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	:EsEnrolado, --EsEnrolado
	:CLI_IdTipoCliente,
	:IdPlantillaLimiteExtra,
	:IdPersonaUsuario,
	:CLI_CodigoAdicionalHolding,
	:CLI_IdClaseCliente,
	:CLI_ClienteFinal,
	:TipoEnrolamiento
 );