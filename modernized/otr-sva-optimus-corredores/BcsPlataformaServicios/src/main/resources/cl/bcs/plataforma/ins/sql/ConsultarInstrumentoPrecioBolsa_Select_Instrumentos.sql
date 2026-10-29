WITH instrumentos as(
    SELECT
         row_number() over (partition by bip."IdInstrumento" order by bip."IdInstrumento") AS row_num
        ,tinst."IdClasificacionTipoInstrumento"
        ,tinst."IdTipoInstrumento"
        ,inn."IdInstrumento"
        ,inn."Nemotecnico"
        ,inn."IdCondicionLiquidacion"
        ,inn."IdMoneda"
        ,bip."IdBolsa"
    FROM "BSCL.Plataforma.BD::INS.Instrumento" inn
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tinst on inn."IdTipoInstrumento" = tinst."IdTipoInstrumento"
    LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" bip ON inn."IdInstrumento" = bip."IdInstrumento"
    WHERE (inn."IdInstrumento" IN (:idInstrumentos) OR 1 = :filtroPorId)
    AND (inn."IdTipoInstrumento" IN (:idTiposInstrumentos) OR 1 = :filtroPorTipo)
    AND (bip."IdBolsa" IN (:idBolsas) OR 1 = :filtroPorBolsa)
    AND (inn."Nemotecnico" IN (:nemos) OR 1 = :filtroPorNemo)
    AND (tinst."IdClasificacionTipoInstrumento" IN (:idClasificaciones) OR 1 = :filtroPorClasificacion)
    AND (inn."IdEstado" = 'ACT')
    AND (inn."EstaInscrito" = CASE :soloInscritos WHEN 'S' THEN :soloInscritos ELSE inn."EstaInscrito" END)
	  AND (:incluyeNemoNegociacion = 'S' OR (inn."IdInstrumentoRaiz" IS NULL OR inn."IdInstrumentoRaiz" = 0))
)
SELECT "IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMoneda","IdBolsa","IdCondicionLiquidacion" as "IdCondicionDeLiquidacion"
FROM instrumentos
WHERE row_num = 1
ORDER BY "IdInstrumento","IdBolsa";
