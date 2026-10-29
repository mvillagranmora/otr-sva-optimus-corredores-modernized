
WITH precios as
(        
    SELECT 
	    row_number() over (partition by RNP."IdRegistro",RNP."IdInstrumento" order by RNP."IdInstrumento", EVP."Fecha" desc) AS row_num,
	    RNP."IdRegistro",
	    RNP."IdInstrumento",
	    CAST(EVP."Fecha" AS DATE) AS "Fecha",
	    EVP."ValorRegistro" AS "PrecioCierre"
    FROM "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso" AS RNP
    INNER JOIN "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_CriterioSeleccionTasas" AS CST ON (CST."IdRegistro" = RNP."IdRegistro")    
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (INS."IdInstrumento" = RNP."IdInstrumento")
    INNER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio" AS EVP ON (EVP."Entidad" = 'TASARIESGO' AND EVP."TipoRegistro" = 'PRECIOMERCADO'
    															AND INS."Nemotecnico" = EVP."Nemotecnico" AND CAST(EVP."Fecha" AS DATE) <= RNP."FechaConsulta")
	WHERE CST."IdRegistro"   = :IdRegistro  
	AND   CST."CodigoFuente" = :CodigoFuente
	
)


SELECT RNP."IdInstrumento",PRE."PrecioCierre",RNP."IdRegistro",RNP."IdClasificacionTipoInstrumento",RNP."IdTipoInstrumento",RNP."Nemotecnico",RNP."IdMoneda",PRE."Fecha",RNP."FechaConsulta"
FROM "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso" AS RNP
INNER JOIN precios AS PRE ON RNP."IdInstrumento" = PRE."IdInstrumento"
WHERE row_num = 1 
AND RNP."IdRegistro" = PRE."IdRegistro" 
AND PRE."PrecioCierre" > 0
ORDER BY RNP."IdInstrumento", RNP."FechaConsulta", PRE."Fecha" desc;
                        
