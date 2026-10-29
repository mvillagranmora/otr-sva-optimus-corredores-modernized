INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte"  (
	"IdContraparte" ,
	"IdPersona" ,
	"IdEmpresa" ,
	"IdEstado" ,
	"CodigoInscripcion",
	"Emisor",
	"Depositos",
	"Bolsa",
	"Corredora",
	"CamaraDeCompensacion",
	"Banco", 
	"Boveda", 
	"FechaIngreso" ,
	"FechaCreacion" ,
	"FechaModificacion",
	"EsEnrolado",
	"IdUsuarioCreador",
	"Otros"
 ) select
	:IdContraparte, --IdContraparte
	:IdPersona, --IdPersona
	:IdEmpresa, --IdEmpresa
	:IdEstado, --IdEstado
	:CodigoInscripcion,
	'S',
	'N',
	'N',
	'N',
	'N',
	'N', 
	'N', 
	CURRENT_UTCTIMESTAMP, --FechaIngreso
	CURRENT_UTCTIMESTAMP, --FechaCreacion
	CURRENT_UTCTIMESTAMP, --FechaModificacion
	'S', --EsEnrolado
	:IdUsuarioCreador, --IdUsuarioCreador
	''
 from  DUMMY;