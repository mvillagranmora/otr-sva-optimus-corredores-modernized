/********************************************************** 
Proposito	: Script para insert en tabla temporal de entrada para rutear ordenes
autor		: RU
fecha		: 01/02/2017
***********************************************************/

insert into "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Ordenes"(
	"IdInterno"									,
	"IdOrden"									,
	"IdTipoCanalIngreso"						,
	"MontoTotalOrden"							,
	"MontoOperacionMonedaLocal"					,
	"MontoMaximoDistribucionOrden"				,
	"ValidaSaldoCaja"							,
	"PortafolioConFlujoCaja"					,
	"PlantillaEmpleadoMaximoDescuentoComision"	,
	"ValidaSaldoCustodia"						,
	"PortafolioConSaldoCustodia"				,
	"PorcentajeMaximoComision"					,
	"PorcentajeMinimoComision"					,
	"PlantillaEmpleadoMontoMaximoOperacion"		,
	
	"CO_OperacionSinFlujoCustodia"						,
	"CO_DescuentoComisionSuperaPermitidoPlantilla"		,
	"CO_ComisionSuperaMaximoPermitido"					,
	"CO_ComisionBajoMinimoPermitido"					,
	"CO_MontoSuperaMaximoPermitidoCanalDistribucion" 	,
	"CO_MontoOperacionSuperaPermitidoPlantilla"			,
	
	"FlujoCajaPortafolio"						,
	"IdMonedaLiquidacionOrden"					,
	"FlujoCustodiaPortafolio"					,
)
values (
	:IdInterno,
	:IdOrden,
	:IdTipoCanalIngreso,
	:MontoTotalOrden,
	:MontoOperacionMonedaLocal,
	:MontoMaximoDistribucionOrden,
	:ValidaSaldoCaja,
	:PortafolioConFlujoCaja,
	:PlantillaEmpleadoMaximoDescuentoComision,
	:ValidaSaldoCustodia,
	:PortafolioConSaldoCustodia,
	:PorcentajeMaximoComision,
	:PorcentajeMinimoComision,
	:PlantillaEmpleadoMontoMaximoOperacion,
	
	:CO_OperacionSinFlujoCustodia,
	:CO_DescuentoComisionSuperaPermitidoPlantilla,
	:CO_ComisionSuperaMaximoPermitido,
	:CO_ComisionBajoMinimoPermitido,
	:CO_MontoSuperaMaximoPermitidoCanalDistribucion,
	:CO_MontoOperacionSuperaPermitidoPlantilla,
	
	:FlujoCajaPortafolio,
	:IdMonedaLiquidacionOrden,
	:FlujoCustodiaPortafolio
)

	