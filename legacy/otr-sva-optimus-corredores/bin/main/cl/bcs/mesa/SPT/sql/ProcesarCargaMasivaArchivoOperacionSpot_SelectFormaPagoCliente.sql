SELECT 
x."IdCuentaBancaria", 
x."IdFormaDePago",
x."IdPortafolio"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPreferenciaPagoPortafolioSpot" x
WHERE x."IdFormaDePago" IN (:IdFormaDePago)
AND x."IdPortafolio" IN (:IdPortafolio)
AND x."IdTipoProducto" = 'MEME';