/********************************************************** 
Proposito	: Script para insert en tabla temporal de entrada para rutear ordenes
autor		: RU
fecha		: 01/02/2017
***********************************************************/

insert into "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Cobros"(
	"IdInterno",
	"IdOrden",
	"IdCobro",
	"FormaCobro",
	"CodigoCobroClasificacion",
	"ValorCobroPerfil",
	"ValorCobroAplicado"
)
values (
	:IdInterno,
	:IdOrden,
	:IdCobro,
	:FormaCobro,
	:CodigoCobroClasificacion,
	:ValorCobroPerfil,
	:ValorCobroAplicado,
)

	
