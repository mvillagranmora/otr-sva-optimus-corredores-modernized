WITH instrumentosEnUso AS (
	SELECT DISTINCT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.Cartera"
	UNION
	SELECT DISTINCT "IdInstrumento" FROM "BSCL.Custodia.BD::GTA.Garantia"
	UNION
	SELECT DISTINCT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
	UNION
	SELECT DISTINCT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.Reserva"
	UNION
	SELECT DISTINCT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.Subyacente"
)
SELECT 
	tinst."IdClasificacionTipoInstrumento"
    ,tinst."IdTipoInstrumento"
    ,inn."IdInstrumento"
    ,inn."Nemotecnico"
    ,inn."IdMoneda"
FROM "BSCL.Plataforma.BD::INS.Instrumento" inn
INNER JOIN instrumentosEnUso ON inn."IdInstrumento" = instrumentosEnUso."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tinst on inn."IdTipoInstrumento" = tinst."IdTipoInstrumento"
WHERE --(tinst."IdClasificacionTipoInstrumento" IN ('RV'))
(inn."IdInstrumento" IN (:idInstrumentos) OR 1 = :filtroPorId)
AND (tinst."IdTipoInstrumento" IN (:idTiposInstrumentos) OR 1 = :filtroPorTipo)
AND (inn."Nemotecnico" IN (:nemos) OR 1 = :filtroPorNemo)
AND (tinst."IdClasificacionTipoInstrumento" IN (:idClasificaciones) OR 1 = :filtroPorClasificacion)
ORDER BY inn."IdInstrumento";
