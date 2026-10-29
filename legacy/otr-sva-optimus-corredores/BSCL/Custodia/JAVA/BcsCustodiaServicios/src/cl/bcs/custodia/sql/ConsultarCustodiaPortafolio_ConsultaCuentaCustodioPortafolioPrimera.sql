SELECT
"IdPortafolio" || "IdBoveda" AS "Id"
,"IdPortafolio"
,"IdBoveda"
,"IdCuentaCustodioPortafolio"
,"Codigo"
FROM  "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolioPrimera"
WHERE "IdPortafolio" IN (:IdPortafolio)