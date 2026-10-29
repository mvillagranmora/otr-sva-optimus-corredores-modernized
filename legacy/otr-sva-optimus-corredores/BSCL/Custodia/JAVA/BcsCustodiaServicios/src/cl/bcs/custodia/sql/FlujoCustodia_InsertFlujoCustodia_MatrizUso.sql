/********************************************************** 
Proposito	: Insertar DatosFlujoCustodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

INSERT INTO "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso"(
	"IdCliente",
	"IdPortafolio",
	"UsoTipoOperacion",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento",
	"IdInstrumento",
	"IdEmisor",
	"FechaVencimiento",
	"IdTipoReajuste",
	"EsFungible",
	"IdBoveda",
	"IdTipoInstrumentoEspecifico",
	"IdUsoFlujoCustodia",
	"BusquedaEspecifica",
	"IdEstado",
	"IdEmpresa",
	"IdRegimenFondoMutuo",
	"IdTrabajo"
)
VALUES (
	:IdCliente,
	:IdPortafolio,
	:UsoTipoOperacion,
	:IdClasificacionTipoInstrumento,
	:IdTipoInstrumento,
	:IdInstrumento,
	:IdEmisor,
	:FechaVencimiento,
	:IdTipoReajuste,
	:EsFungible,
	:IdBoveda,
	:IdTipoInstrumentoEspecifico,
	:IdUsoFlujoCustodia,
	:BusquedaEspecifica,
	:IdEstado,
	:IdEmpresa,
	:IdRegimenFondoMutuo,
	:IdTrabajo
)
