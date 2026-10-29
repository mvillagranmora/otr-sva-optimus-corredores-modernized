SELECT "FechaHasta"
FROM "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil" 
WHERE ("Esquema" = :esquema) 
  AND ("Version" = :version);