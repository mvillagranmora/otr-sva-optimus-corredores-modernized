
/********************************************************** 
Proposito	: INSERTAR ASOCIACIONES
autor		: CSI
fecha		: 01/05/2017
***********************************************************/

insert into "BSCL.Orden.BD::ORD.OrdenAsociada"(	
	"IdOrdenAsociada",
	"IdOrdenN",
	"IdOrdenM",
	"IdOrdenMOrigen",
	"FechaIngresoRegistroCorta",
	"FechaIngresoRegistro",
	"ModalidadCalculo",
	"Monto",
	"Cantidad",
	"Precio",
	"IdUsuario"
)
values (
	:IdOrdenAsociada,
	:IdOrdenN,
	:IdOrdenM,	
	:IdOrdenMOrigen,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	:ModalidadCalculo,
	:Monto,
	:Cantidad,
	:Precio,
	:IdUsuario
)
