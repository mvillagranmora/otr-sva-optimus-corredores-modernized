SELECT
	 UFC."IdUsoFlujoCustodia"
	,UFC."IdEmpresa"
	,UFC."UsoTipoOperacion"
	,UFC."IdClasificacionTipoInstrumento"
	,UFC."IdTipoInstrumento"
	,UFC."IdInstrumento"
	,UFC."IdCliente"
	,UFC."IdPortafolio"
	,UFC."BusquedaEspecifica"
	,UFC."FechaIngresoRegistro"
	,UFC."IdUsuario"
	,UFC."IdEstado" AS "IdEstadoUso"
	,IFC."ItemCalculo"
	,IFC."ItemOrigen"
	,IFC."Descripcion"
	,IFC."FuncionOperador"
	,IFC."IdEstado" AS "IdEstadoItem"
	,ESTUFC."Nombre" AS "EstadoUso"
	,ESTIFC."Nombre" AS "EstadoItem"
	,UFC."UsoTipoOperacion"||'_'||COALESCE(REPLACE(TIPOPE."Nombre",' ','_'),'') AS "NombreTipoOperacion"
FROM "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" AS UFC 
INNER JOIN "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia" AS IFC 
	ON (UFC."IdUsoFlujoCustodia" = IFC."IdUsoFlujoCustodia")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPOPE
	ON (UFC."UsoTipoOperacion" = TIPOPE."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTUFC
	ON (UFC."IdEstado" = ESTUFC."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTIFC
	ON (IFC."IdEstado" = ESTIFC."IdEstado")
WHERE UFC."IdEstado" = 'ACT'
  AND IFC."IdEstado" = 'ACT'
  AND UFC."IdEmpresa" = :IdEmpresa;