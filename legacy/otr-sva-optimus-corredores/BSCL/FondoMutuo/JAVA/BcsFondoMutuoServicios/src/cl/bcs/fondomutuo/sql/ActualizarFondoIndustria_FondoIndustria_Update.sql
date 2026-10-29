UPDATE "BSCL.FondoMutuo.BD::FMM.FondosIndustria"
SET  
	 "NombreFFMM" =:NombreFFMM
	,"RutSociedadAdmFFMM" =:RutSociedadAdmFFMM
	,"SociedadAdmFFMM" =:SociedadAdmFFMM
	,"IdEstado" = :IdEstado
	,"IdMoneda" = :IdMoneda
WHERE "IdFondoIndustria" = :IdFondoIndustria;

 