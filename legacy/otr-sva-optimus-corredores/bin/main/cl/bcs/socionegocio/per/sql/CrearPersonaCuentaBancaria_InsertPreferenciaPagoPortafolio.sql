insert into "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"(
	"IdPreferenciaPagoPortafolio",
	"IdPortafolio",
	"IdPreferenciaPago",
	"IdCuentaBancaria",
	"IdTarjetaBancaria",
	"FechaCreacion",
	"FechaModificacion",
	"IdUsuarioCreador",
	"IdEstado",
	"IdEmpresa",			
	"FechaIngreso",					 
	"IdEstadoAutorizacion",
	"PorDefecto"
)
SELECT
	"BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
	POR."IdPortafolio",
	PRE."IdPreferenciaPago",
	:IdCuentaBancaria,
	0,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:IdUsuario,
	'ACT',
	:IdEmpresa,
	:FechaIngreso,
	:IdEstadoAutorizacion,
	'S'
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR,
	 "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
WHERE POR."IdCliente" IN (:IdCliente)
  AND PRE."IdMedioDePago" = 'TRA'
  AND PRE."IdMoneda" IN (:IdMoneda)
  AND PRE."RecibePagaPortafolio" = 'R'
  AND PRE."IdTipoProducto" NOT IN ('MEME','MERF','MEFW')
  AND PRE."IdEmpresa" IN (:IdEmpresa);
