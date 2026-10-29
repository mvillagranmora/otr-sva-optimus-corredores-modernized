UPDATE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS NCG
    SET 	
        NCG."IdEstado" = 'INA'
WHERE NCG."Fecha"= :p_Fecha 
AND NCG."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
AND NCG."IdOrigen" = 'PAUTA' 
