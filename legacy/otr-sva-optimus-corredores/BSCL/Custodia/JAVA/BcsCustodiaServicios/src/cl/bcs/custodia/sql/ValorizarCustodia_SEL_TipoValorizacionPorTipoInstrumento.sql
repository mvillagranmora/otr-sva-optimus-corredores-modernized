/********************************************************** 
Proposito	: Script para recuperar los tipo de valorizacion
autor		: NRM
fecha		: 17/05/2017
***********************************************************/


SELECT   "IdTipoValorizacionPorTipoInstrumento"
		,"IdTipoInstrumento"
		,"UsaTasaCompra"
		,"UsaTasaMercado"
		,"UsaTasaCorredora"
		,"UsaTasaTIF"
		,"UsaPibMasMenos"
		,"ValorizaAnterior"
		,"ValorizaActual"
		,"ValorizaProximo"
		,"CalculaIntereses"
		,"CalculaInteresesAPlazo"		  
		,"TipoCalculoInteres"
		,"BaseCalculoDeInteres"
		,"TipoValorizacion"
		,"SubTipoValorizacion"
FROM "BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento"
WHERE "IdEmpresa" = :IdEmpresa;
