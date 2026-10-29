SELECT
	 "IdFondoIndustria"
	,"RunFFMM"
	,"NombreFFMM"
	,"RutSociedadAdmFFMM"
	,"SociedadAdmFFMM"
	,"IdEstado"
	,"IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.FondosIndustria"
WHERE "RunFFMM" = :RunFFMM;

 