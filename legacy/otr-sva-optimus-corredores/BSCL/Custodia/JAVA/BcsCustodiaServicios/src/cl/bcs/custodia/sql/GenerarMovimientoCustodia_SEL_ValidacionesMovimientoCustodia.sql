/********************************************************** 
Proposito	: Script para obtener validaciones proceso
autor		: NRM
fecha		: 18/04/2017
***********************************************************/

SELECT
	"IdValidacionesMovimientoCustodia",
	"IdTipoOperacion",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento",
	"IdInstrumento",
	"IdOrigenInstrumento",
	"ValidacionEsIncluyente",
	"CampoAValidar",
	"Conversion",
	"ValorFalloValidacion",
	"CodigoMensaje",
	"CodigoDato1",
	"CodigoDato2",
	"IdEstado"
from "BSCL.Custodia.BD::CUS.ValidacionesMovimientoCustodia";
