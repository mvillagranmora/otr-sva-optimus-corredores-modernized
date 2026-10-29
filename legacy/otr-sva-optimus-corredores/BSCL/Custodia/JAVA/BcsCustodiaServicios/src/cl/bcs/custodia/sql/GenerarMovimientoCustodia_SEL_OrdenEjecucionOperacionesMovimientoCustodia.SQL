/********************************************************** 
Proposito	: Script para obtener orden de ejecucion
autor		: NRM
fecha		: 18/04/2017
***********************************************************/

SELECT 
	"IdOrdenEjecucionOperacionesMovimientoCustodia",
	"IdEmpresa",
	"IdTipoOperacion",
	"IdTipoPaso",
	"IdOrdenPaso",
	"IdPasoEjecucion",
	"NombreColumnaPaso",
	"PasoEsIngreso",
	"TablaAfectadaPorPaso",
	"PasoPerteneceClienteRetiro",
	"PasoEjecucion",
	"TablaOrigenDatosDelPaso",
	"OrigenDatosDelPaso",
	"PasoRelacionadoCon",
	"IdEstadoPaso",
	"PasoMantieneFolioCustodia",
	"PasoUtilizaInstrumentoRelacionado",
	"TipoRegistroDelPaso",
	"SeValorizaPaso",
	"PasoTieneConfiguradaContingencia",
	"EsPasoContingencia",
	"EjecutarPasoHabiendoContingencia",
	"ContinuarEjecucionPasosNoTeniendoSaldo",
	"EjecutarPasoNoTeniendoSaldo",
	"PasoRetiraCarteraOperacionOrigen",
	--"PasoEsRetiroCarteraOrigen",
	--"PasoSumaCustodia",
	"AgregarSumaDeCustodiaAlPaso",
	"IdTipoOperacionPaso",
	"IdConceptoPaso",
	"RetornaCarteraAlPrecioTasaPaso",
	"PasoEsCustodiaRetornada",
	"PasoMueveCustodia",
	"PasoPermiteCertificacion",
	"EjecutarPasoSiendoNominal",
	"CodigoMensajePaso",
	"IdEstadoRegistro"
FROM "BSCL.Custodia.BD::CUS.OrdenEjecucionOperacionesMovimientoCustodia"
WHERE "IdEstadoRegistro" = 'ACT'
  AND "IdEmpresa" = :IdEmpresa
ORDER BY "IdTipoOperacion","IdTipoPaso","IdOrdenPaso";