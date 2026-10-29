SELECT 	"IdPortafolio", 
		"IdBoveda", 
		"Codigo"		AS "CuentaCustodio", 
		"IdTipoCuentaCustodia", 
		"NombreTipoCuentaCustodia"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio"
WHERE "IdPortafolio" IN (:IdPortafolio)
  AND "IdBoveda" IN (:IdBoveda)
  AND "IdEstado" = 'ACT'