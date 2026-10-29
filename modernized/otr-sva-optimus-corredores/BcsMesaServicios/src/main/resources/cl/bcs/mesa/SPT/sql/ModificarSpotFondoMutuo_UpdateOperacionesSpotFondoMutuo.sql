UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot" 
SET "TipoCambioCierre" = :p_TipoCambioCierre
,"Estado"='VIG'
WHERE "Estado"  IN ('VIG','PEN')
AND "IdSolicitudAporteRescate">0
AND TO_DATE("FechaOperacion")=TO_DATE(:p_FechaOperacion)
AND "IdEmpresa"=:p_IdEmpresa;