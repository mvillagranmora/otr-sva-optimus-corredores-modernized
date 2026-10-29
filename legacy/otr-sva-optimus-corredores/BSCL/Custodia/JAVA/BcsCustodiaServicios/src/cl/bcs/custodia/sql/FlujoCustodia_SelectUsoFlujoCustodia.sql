/********************************************************** 
Proposito	: Recuperar la conf de los items del flujo de custodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

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
	
	'' AS "ItemCalculo",
	'' AS "ItemOrigen",
	'' AS "Descripcion",
	'' AS "FuncionOperador"
FROM "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" AS UFC
WHERE UFC."IdEstado" = 'ACT'
	AND UFC."IdEmpresa" = :IdEmpresa;
