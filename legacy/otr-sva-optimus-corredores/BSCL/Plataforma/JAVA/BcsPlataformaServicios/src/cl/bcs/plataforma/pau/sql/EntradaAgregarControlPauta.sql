/************************************************************* 
Proposito	: Script insertar datos entrada sp CrearCargoAbono
autor		: JG
fecha		: 02/02/2017
**************************************************************/
insert into "BSCL.Plataforma.BD::PAU.AgregarControlPauta_Entrada_Registros"(
	"IdTrabajo",
	"IdPrograma",
	"IdTipoPrograma",
	"Ejecucion",
	"FechaSistema",
	"IdEstado",
	"Mensaje"
)
values (
	:IdTrabajo,
	:IdPrograma,
	:IdTipoPrograma,
	:Ejecucion,
	:FechaSistema,
	:IdEstado,
	:Mensaje
)
