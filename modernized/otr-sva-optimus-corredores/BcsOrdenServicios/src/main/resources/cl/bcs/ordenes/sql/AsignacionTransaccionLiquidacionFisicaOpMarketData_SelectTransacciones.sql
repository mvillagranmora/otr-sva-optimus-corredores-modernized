SELECT DISTINCT "Instrumento", "IdInstrumento","IdTransaccion", "FolioTransaccion", "CantidadDisponibleTransaccion", "MontoTransaccion", "ValorRescateTransaccion"
	--,*
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar"
WHERE "FolioTransaccion" IN (:folio) AND "IdClasificacionTipoInstrumento"='DV';
--WHERE "FolioTransaccion" IN ('1050','1051','1052','1007','1006','1005','1064','1066') AND "IdClasificacionTipoInstrumento"='DV';
--agregar la marca para saber que son las de liquidacion fisica
--WHERE 