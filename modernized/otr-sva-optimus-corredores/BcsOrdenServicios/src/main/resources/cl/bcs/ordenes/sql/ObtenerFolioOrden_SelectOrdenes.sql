SELECT "FechaOperacion","Folio" 
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "FechaOperacion" > TO_TIMESTAMP(:fechaOperacionDesde) AND "FechaOperacion" <= TO_TIMESTAMP(:fechaOperacionHasta)
ORDER BY "FechaOperacion" asc
;