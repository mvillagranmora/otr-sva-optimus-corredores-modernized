SELECT CART."Nemotecnico",CART."MontoCompra",CART."IdClasificacionTipoInstrumento",CART."INGMontoATasaCompraActual"
FROM  "BSCL.Custodia.BD::CUS.Cartera" AS CART
WHERE  CART."Nemotecnico" IN (:p_Nemos)
AND    CART."INGFechaOperacion" = :p_Fecha;

--solo vig en el periodo 