SELECT 
"IdCarga"
,"IdCuadraturaCircular1518"
,"Nemotecnico"
,"CompraEnTransito"
,"VentaEnTransito"
,"Diferencia"
,"Precio"
,"DiferenciaValorizada"
,"TotalCustodioEmisores"
,"TotalCustodia"
,"IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1518"
WHERE "FechaCuadratura" = :FechaCuadratura
AND "IdEstado" = 'VIG'