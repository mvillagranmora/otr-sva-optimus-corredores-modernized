SELECT 
	"PORT"."IdPortafolio", 
	"PORT"."CodigoPortafolio", 
	"PORT"."Proposito", 
	"PORT"."IdCliente"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
WHERE "PORT"."IdPortafolio" IN (:IdPortafolio) AND "PORT"."Proposito" = 'PROPOSITOAPV'