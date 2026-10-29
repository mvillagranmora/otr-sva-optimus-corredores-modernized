WITH "HistoricoInstrucciones" AS  (
	SELECT 
	    ROW_NUMBER() OVER(PARTITION BY insHis."IdInstruccion" ORDER BY insHis."IdInstruccionHistoria" DESC) as RowNum, 
	    insHis."IdInstruccion", 
	    insHis."IdEstadoInstruccion",
	    insHis."IdMotivo",
	    ins."IdCliente"
	FROM "BSCL.Orden.BD::ORD.Instruccion" AS ins
	JOIN "BSCL.Orden.BD::ORD.InstruccionHistorica" insHis ON (ins."IdInstruccion" = insHis."IdInstruccion")
	WHERE ins."IdCliente" = :p_IdCliente AND 
		ins."IdEstadoInstruccion" != 'CAN' AND
		insHis."FechaIngreso" >= :p_FechaDesde
)
SELECT "IdInstruccion", 
    "IdEstadoInstruccion",
    "IdMotivo",
    "IdCliente"
FROM "HistoricoInstrucciones"
WHERE "ROWNUM" = 1 AND 
	"IdEstadoInstruccion" = 'SCN';