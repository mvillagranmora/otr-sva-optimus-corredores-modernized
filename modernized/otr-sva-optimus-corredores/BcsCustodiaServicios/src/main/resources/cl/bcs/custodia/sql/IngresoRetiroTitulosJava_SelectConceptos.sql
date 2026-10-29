SELECT TRA."IdMovimientoCarteraEnTransito", CON."IdConcepto", CON."ConCertificadoAutomatico"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" TRA
INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" CON ON TRA."IdConcepto" = CON."IdConcepto"
WHERE TRA."IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)