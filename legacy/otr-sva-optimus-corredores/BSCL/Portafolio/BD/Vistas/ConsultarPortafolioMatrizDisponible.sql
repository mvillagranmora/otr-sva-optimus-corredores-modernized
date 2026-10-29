SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizDisponible"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizDisponible" AS 


SELECT POR."IdPortafolio"
     , POR."IdCliente"
     , CLI."IdPersona"
     , MATAGR."NombreAgrupador"
     , MATAGR."IdMatrizAgrupadorAPV"
     , MATAGR."Codigo"
     , MATAGR."CodigoAgrupador"
     , MATAGR."NombreAgrupador" || ' / ' || MATAGR."Codigo" AS "NombreAgrupadorCodigo"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
		ON POR."IdCliente" = CLI."IdCliente"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MATAGR
		ON MATAGR."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
	   AND MATAGR."IdPropositoPortafolio" = POR."Proposito"
WHERE POR."Proposito" = 'PROPOSITOAPV'
AND POR."IdEstado" = 'ACT'
AND "IdPortafolio" NOT IN (SELECT PORCON."IdPortafolio"
						   FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON
						   INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
								   ON CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV");
      	
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizDisponible" TO  BCS_DESARROLLADOR WITH GRANT OPTION;