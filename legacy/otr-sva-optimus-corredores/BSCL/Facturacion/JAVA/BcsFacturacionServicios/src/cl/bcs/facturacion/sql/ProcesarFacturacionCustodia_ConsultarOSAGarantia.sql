WITH totOsaGar AS 
(
	SELECT   "IdInstrumento","IdPortafolio", SUM("INGCantidad") AS "INGCantidad"
	FROM "BSCL.Custodia.BD::GTA.Garantia" 
	WHERE "IdPortafolio"  = :IdPortafolio
	AND   "IdInstrumento" = :IdInstrumento
	AND   "IdEstado" = 'VIG'
	GROUP  BY "IdInstrumento", "IdPortafolio"
)
SELECT "IdInstrumento", "IdPortafolio", "INGCantidad"
FROM totOsaGar
WHERE "INGCantidad" >= :CantidadRetiro;