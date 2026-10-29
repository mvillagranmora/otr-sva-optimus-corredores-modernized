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
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS MATAGR
		ON MATAGR."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
	   AND MATAGR."IdPropositoPortafolio" = POR."Proposito"
WHERE POR."Proposito" = 'PROPOSITOAPV'
--AND POR."IdEstado" = 'ACT'
/*AND "IdPortafolio" NOT IN (SELECT PORCON."IdPortafolio"
						   FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON
						   INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
								   ON CON."IdContratoSAAPV" = PORCON."IdContratoSAAPV")*/
AND CLI."IdPersona" = :IdPersona
AND MATAGR."Codigo" = :CodigoRegimen
AND MATAGR."NombrePlan" = :NombrePlan
AND MATAGR."CodigoAgrupador" = :CodigoAgrupador
AND POR."IdEstado" <> 'INA';
								   