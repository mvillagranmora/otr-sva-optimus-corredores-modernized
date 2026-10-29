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
	"IdCarga"
)VALUES(
	:IdCliente,
	:IdPersona,
	:IdEmpresa,
	:IdEstado,
	:IdSegmentoCliente,
	:IdCategoriaInversionista,
	:IdUsuarioCreador,
	CURRENT_UTCTIMESTAMP, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	:EsEnrolado,
	:IdTipoCliente,
	:IdPlantillaLimiteExtra,
	:IdPersonaEjecutivo,
	:CodigoAdicionalHolding,
	:IdCarga
 );