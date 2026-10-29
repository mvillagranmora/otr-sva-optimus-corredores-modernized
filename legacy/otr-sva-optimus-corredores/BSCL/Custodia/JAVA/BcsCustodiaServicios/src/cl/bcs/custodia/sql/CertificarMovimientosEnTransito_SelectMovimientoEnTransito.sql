SELECT "IdMovimientoCarteraEnTransito",
"IdCliente",
"IdPortafolio",
"Nemotecnico",
"IdTipoOperacion",
"IdEstado",
"Cantidad"
FROM  "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "IdMovimientoCarteraEnTransito" IN (:p_IdMovimientoCarteraEnTransito);