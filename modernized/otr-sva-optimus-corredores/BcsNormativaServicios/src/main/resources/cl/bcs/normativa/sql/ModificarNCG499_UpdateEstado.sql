UPDATE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS CIR
    SET 	
        CIR."IdEstado" = :IdEstado
WHERE
CIR."Fecha"= :Fecha
AND CIR."IdClasificacionTipoInstrumento"= :IdClasificacionTipoInstrumento
AND CIR."Nemotecnico"= :Nemotecnico
AND CIR."IdEmisor"= :IdEmisor
AND CIR."IdOrigen" = :IdOrigen