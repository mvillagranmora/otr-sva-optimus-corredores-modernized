SELECT "LlavePortafolioClasificacion" FROM
(	SELECT distinct "IdPortafolio" || '_' || "IdClasificacionTipoInstrumento" || '_' || TO_CHAR("FechaComprobante",'YYYY-MM-DD') AS "LlavePortafolioClasificacion"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante"
	WHERE "IdPortafolio" in (:idsPortafolio)
	AND "IdClasificacionTipoInstrumento" in (:idsClasificacion)
	AND TO_CHAR("FechaComprobante",'YYYY-MM-DD') in (:fechasComprobante)
)