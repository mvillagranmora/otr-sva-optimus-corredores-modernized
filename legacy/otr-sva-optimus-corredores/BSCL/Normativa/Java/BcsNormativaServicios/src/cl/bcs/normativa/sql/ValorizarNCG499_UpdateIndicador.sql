UPDATE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS NCG
    SET 	
        NCG."IndicadorValorizado" = 'S'
WHERE NCG."Fecha"= :p_Fecha
AND NCG."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
AND NCG."IdOrigen" = 'PAUTA'
