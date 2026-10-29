SELECT x."IdCuentaBancaria", x."IdFormaDePago"
FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" y
LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPreferenciaPagoPortafolioCorredoraSpot" x ON x."IdPortafolio" = y."IdPortafolio"
WHERE x."IdFormaDePago" IN (:IdFormaDePago) AND y."IdTipoProducto" = 'MEME';