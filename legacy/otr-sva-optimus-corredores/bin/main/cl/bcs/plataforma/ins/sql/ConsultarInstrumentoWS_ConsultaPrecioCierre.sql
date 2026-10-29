WITH  PreciosRentabilidad AS ( 
SELECT  TO_VARCHAR(precios."Fecha", 'YYYY-MM-DD') AS "UltimaFechaCierre",
	TO_VARCHAR(precios1."Fecha", 'YYYY-MM-DD') AS "PrimeraFechaInicio",
	inst."IdInstrumento",
	"Nemotecnico",
 	COALESCE(precios."PrecioCierre",0) AS "UltimoPrecioCierre",
 	COALESCE(precios1."PrecioCierre",0) AS "PrimerPrecioCierre"
 	,CASE WHEN COALESCE(precios."PrecioCierre",0) > 0 AND COALESCE(precios1."PrecioCierre",0) >0 THEN 
 		 round(((COALESCE(precios."PrecioCierre",0)-COALESCE(precios1."PrecioCierre",0))/COALESCE(precios1."PrecioCierre",0))*100,2,ROUND_HALF_UP)
 		 ELSE 0 END AS "Rentabilidad"
 	,ROW_NUMBER() OVER (PARTITION BY (inst."Nemotecnico")
                            ORDER BY (precios1."Fecha") ASC) AS seq	 
 FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" precios ON precios."IdInstrumento" = inst."IdInstrumento" AND precios."Fecha" IN (:FechaCierre) 
and precios."IdBolsa" ='BCS'
LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" precios1 ON precios1."IdInstrumento" = inst."IdInstrumento" AND precios1."Fecha">= (:FechaInicioAno)
and precios1."IdBolsa" ='BCS'
WHERE inst."IdEstado" = 'ACT'  AND inst."Nemotecnico" IN  (:Nemotecnicos)
)
SELECT PR.* FROM PreciosRentabilidad  AS PR
WHERE PR."SEQ"=1;