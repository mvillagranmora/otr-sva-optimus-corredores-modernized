SELECT 
	"IdCuentaCustodioPortafolio", 
	"IdBoveda", 
	"Codigo", 
	"IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
WHERE "Codigo" IN (:CodigoCustodia) 
  AND "IdEstado" = 'ACT';