SELECT 
"IdOperacionSpot"
,"IdSolicitudAporteRescate"
,"Estado"
,"EstadoConfirmacion"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" 
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) AND "Estado" <> 'MOD'