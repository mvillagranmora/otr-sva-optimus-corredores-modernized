SELECT 
	ENTIDAD."IdEmpresa",	
	ENTIDAD."Nombre",
	CLI."IdPortafolioDefecto"	
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS ENTIDAD 
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS CONTR ON CONTR."IdSocioDeNegocio" = ENTIDAD."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CONTR."IdPersona" = CLI."IdPersona"
WHERE ENTIDAD."IdEstado" = 'ACT';