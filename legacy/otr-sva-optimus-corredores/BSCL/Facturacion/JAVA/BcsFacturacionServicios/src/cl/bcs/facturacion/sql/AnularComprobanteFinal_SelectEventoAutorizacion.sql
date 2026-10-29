SELECT Evento."NumeroOperacionConcepto"
FROM "BSCL.Plataforma.BD::AUT.Evento" Evento
WHERE 
Evento."NumeroOperacionConcepto" IN (:idsComprobante) AND 
Evento."IdEstado" = 'VIG' AND 
Evento."IdModulo" = 'FAC' AND 
Evento."IdConcepto" = 'ANUFAC'
