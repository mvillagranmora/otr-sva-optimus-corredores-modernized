WITH "HistoricoInstrucciones" AS  (
	SELECT 
	    ROW_NUMBER() OVER(PARTITION BY "IdInstruccion" ORDER BY "IdInstruccionHistoria" DESC) as RowNum, 
	    "IdInstruccion", 
	    "IdEstadoInstruccion",
	    "IdMotivo"
	FROM
	    "BSCL.Orden.BD::ORD.InstruccionHistorica"
	    WHERE 
	    "IdEstadoInstruccion" != 'SCN' 
	    AND "IdInstruccion" IN (:idInstrucciones)
)
SELECT "IdInstruccion", 
    "IdEstadoInstruccion",
    "IdMotivo"
FROM "HistoricoInstrucciones"
WHERE "ROWNUM" = 1