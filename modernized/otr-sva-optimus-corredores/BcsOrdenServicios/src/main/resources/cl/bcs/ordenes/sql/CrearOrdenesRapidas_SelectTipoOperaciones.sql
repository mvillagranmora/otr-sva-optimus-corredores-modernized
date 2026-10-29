SELECT 
	"Accion" || '_' || "IdTipoInstrumento" AS "CodigoCampo",
	"IdTipoOperacion" AS "Valor" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"  
WHERE "EsOrden" = 'S' AND "IdTipoInstrumento" IN (:IdsTipoInstrumento) AND "Accion" IN (:Acciones);