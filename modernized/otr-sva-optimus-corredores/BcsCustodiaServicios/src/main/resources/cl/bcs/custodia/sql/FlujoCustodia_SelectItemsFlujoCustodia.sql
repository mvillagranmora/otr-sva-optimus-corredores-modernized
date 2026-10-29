/********************************************************** 
Proposito	: Recuperar la conf de los items del flujo de custodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/
/*
select 
	"IdUsoFlujoCustodia",
	"ItemCalculo",
	"FuncionOperador",
	
	"ItemOrigen",
	"Descripcion",
	"IdEstado"
from "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia"
*/

SELECT
	UFC."IdUsuario",
	UFC."IdUsoFlujoCustodia",
	UFC."UsoTipoOperacion",
	UFC."IdClasificacionTipoInstrumento",
	UFC."IdTipoInstrumento",
	UFC."IdInstrumento",
	UFC."IdCliente",
	UFC."IdPortafolio",
	UFC."BusquedaEspecifica",
	UFC."FechaIngresoRegistro",
	UFC."UsoValorizacion",
	
	IFC."ItemCalculo",
	IFC."ItemOrigen",
	IFC."Descripcion",
	IFC."FuncionOperador"
FROM "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" AS UFC 
		INNER JOIN "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia" AS IFC 
		ON	UFC."IdUsoFlujoCustodia" = IFC."IdUsoFlujoCustodia"
	AND UFC."IdEstado" = 'ACT'
	AND IFC."IdEstado" = 'ACT'
	AND IFC."FuncionOperador" <> '0'   -- 0: Desactivado
	AND UFC."IdEmpresa" = :IdEmpresa;
