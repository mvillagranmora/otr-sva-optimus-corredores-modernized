SELECT "IdInstrumento","Fecha","FechaEncontrada","Precio","TipoRegistro","Entidad"
FROM
(
	SELECT 
	row_number() over (partition by ff."IdInstrumento",ff."Fecha" order by ff."IdInstrumento",ff."Fecha",EVP."Fecha" desc) AS row_num,
	ff."TipoRegistro",ff."Entidad",
	ff."IdInstrumento",ff."Fecha",CAST(EVP."Fecha" AS DATE) AS "FechaEncontrada",EVP."ValorRegistro" AS "Precio"
	FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoFecha" ff
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = ff."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" EVP on EVP."Entidad" = ff."Entidad" 
		  AND EVP."TipoRegistro" = ff."TipoRegistro"
    	  AND INS."Nemotecnico" = EVP."Nemotecnico" 
    	  AND CAST(EVP."Fecha" AS DATE) BETWEEN ff."FechaDesde" AND ff."Fecha"
	WHERE ff."Correlativo" = :Correlativo AND EVP."Nemotecnico" IS NOT NULL
	GROUP BY ff."TipoRegistro",ff."Entidad",ff."IdInstrumento",ff."Fecha",EVP."ValorRegistro",EVP."Fecha"
	ORDER BY EVP."Fecha" ASC
);

--ASC para que ordene por antiguedad, no cambiar!
