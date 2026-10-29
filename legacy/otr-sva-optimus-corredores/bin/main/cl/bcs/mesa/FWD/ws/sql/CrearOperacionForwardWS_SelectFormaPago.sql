SELECT 
"IdFormaDePago"
,"RecibePagaPortafolio" 
FROM "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" 
WHERE "IdMoneda" IN ('CLP','USD') AND "IdTipoProducto" = 'MEFW' AND "IdEstado" = 'ACT' AND "IdEstadoPreferenciaPago" = 'ACT';