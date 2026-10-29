SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondosIndustria";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondosIndustria" AS 

	SELECT 
		 "IdFondoIndustria",
	     "RunFFMM" ,
		 "NombreFFMM" ,	
		 "RutSociedadAdmFFMM" ,
		 "SociedadAdmFFMM" ,
		 "IdEstado",
		 "IdMoneda"
	FROM "BSCL.FondoMutuo.BD::FMM.FondosIndustria";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondosIndustria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;