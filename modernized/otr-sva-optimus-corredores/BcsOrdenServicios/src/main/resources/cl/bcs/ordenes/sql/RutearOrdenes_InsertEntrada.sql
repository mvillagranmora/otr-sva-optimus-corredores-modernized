/********************************************************** 
Proposito	: Script para insert en tabla temporal de entrada para rutear ordenes
autor		: RU
fecha		: 01/02/2017
***********************************************************/

insert into "BSCL.Orden.BD::MIG.RutearOrdenes_Entrada"(
	"IdOrden",
	"IdBolsaDistribucion"
)
values (
	:IdOrden,:IdBolsaDistribucion
)
