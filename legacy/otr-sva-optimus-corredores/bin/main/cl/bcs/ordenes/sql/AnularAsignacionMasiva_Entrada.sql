/********************************************************** 
Proposito	: Script para insert en tabla temporal de entrada para anular asignaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/

insert into "BSCL.Orden.BD::ORD.AnularAsignacionMasivaOptimizada_Entrada_Asignaciones"(
	"IdAsignacion"
)
values (
	:IdAsignacion
)
