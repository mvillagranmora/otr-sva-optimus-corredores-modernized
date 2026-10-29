SELECT POR."IdCliente"
	 , POR."IdPortafolio"
	 , CIPC."Periodicidad"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.Normativa.BD.Vistas::CAR.ConsultarIndicadoresPortafolioCartola" CIPC ON CIPC."IdPortafolio" = POR."IdPortafolio"
WHERE CLI."IdEmpresa" = :IdEmpresa AND CIPC."Periodicidad" <> 'NO_SOLICITA'
AND (:IncluirTipoPersona = 'N' OR (:IncluirTipoPersona = 'S' AND CLI."IdTipoPersona" = :IdTipoPersona))
AND (:IncluirCategoriaInversionista = 'N' OR (:IncluirCategoriaInversionista = 'S' AND CLI."IdCategoriaInversionista" = :IdCategoriaInversionista))
AND (:IncluirTipoCliente = 'N' OR (:IncluirTipoCliente = 'S' AND CLI."IdTipoCliente" = :IdTipoCliente))
AND (:ExcluirOperadoresDirectos = 'N' OR (:ExcluirOperadoresDirectos = 'S' AND NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" OP WHERE OP."UtilizaPortafolio" = 'S' AND POR."IdPortafolio" = OP."IdPortafolio")))
AND (:ExcluirPortafoliosIncluidosOtraNomina = 'N' OR (:ExcluirPortafoliosIncluidosOtraNomina = 'S' AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::CAR.Cartola" CAR INNER JOIN "BSCL.Normativa.BD::CAR.Nomina" NOR ON (CAR."IdNomina" = NOR."IdNomina") WHERE CAR."IdPortafolio" = POR."IdPortafolio" AND NOR."Periodo" = :Periodo)))
AND (:IncluirPortafoliosExcluidos = 'N' OR (:IncluirPortafoliosExcluidos = 'S' AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::CAR.Cartola" CAR INNER JOIN "BSCL.Normativa.BD::CAR.Nomina" NOR ON (CAR."IdNomina" = NOR."IdNomina") WHERE CAR."IdPortafolio" = POR."IdPortafolio" AND NOR."Periodo" = :Periodo)));