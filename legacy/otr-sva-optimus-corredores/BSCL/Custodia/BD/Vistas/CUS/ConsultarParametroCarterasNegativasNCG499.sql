SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarParametroCarterasNegativasNCG499";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarParametroCarterasNegativasNCG499" AS

SELECT 
COALESCE("Fecha", '1900-01-01') AS "Fecha",
"IdEmisor",
REPLACE( REPLACE("Nemotecnico",'ñ','#'),'Ñ','#') AS "Nemotecnico",
"IdOrigen",
"IdEstado",
COALESCE(CONF."Valor", 'S') AS "Valor"
FROM
(SELECT 
    CIR."Fecha",
    CIR."IdEmisor",
    CIR."Nemotecnico",
    CIR."IdOrigen",
    CIR."IdEstado"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE CIR."IdEstado" IN ('NEG')
OR CIR."CantidadCustodia" < 0

UNION    
   SELECT 
    CIR."Fecha", 
    CIR."IdEmisor",
    CIR."Nemotecnico",
    CIR."IdOrigen",
    CIR."IdEstado"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE CIR."IdEstado" IN ('NEG')
OR CIR."CantidadGarantia" < 0

UNION
   SELECT 
    CIR."Fecha", 
    CIR."IdEmisor",
    CIR."Nemotecnico",
    CIR."IdOrigen",
    CIR."IdEstado"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE CIR."IdEstado" IN ('NEG')
OR CIR."CantidadPrestamo" < 0

UNION
   SELECT 
    CIR."Fecha", 
    CIR."IdEmisor",
    CIR."Nemotecnico",
    CIR."IdOrigen",
    CIR."IdEstado"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE CIR."IdEstado" IN ('NEG')
OR CIR."CantidadPlazoCompra" < 0

UNION
   SELECT 
    CIR."Fecha", 
    CIR."IdEmisor",
    CIR."Nemotecnico",
    CIR."IdOrigen",
    CIR."IdEstado"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
WHERE CIR."IdEstado" IN ('NEG')
OR CIR."CantidadPlazoVenta" < 0

)
LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONF
    ON CONF."CodigoParametro" = 'CARTERASNEGATIVAS'
WHERE "Nemotecnico" NOT IN (SELECT * FROM "BSCL.Normativa.BD.Utilitarios::NOR.SplitTextBySystemParams"('EXCLUIR_INSTRUMENTOS', ';', 'PARAMETRO'))
GROUP BY
"Fecha",
"IdEmisor",
"Nemotecnico",
"IdOrigen",
"IdEstado",
"Valor"

ORDER BY "Fecha" DESC
