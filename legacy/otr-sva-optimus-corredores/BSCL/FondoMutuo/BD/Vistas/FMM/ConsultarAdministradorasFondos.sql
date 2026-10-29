SET SCHEMA BSCL;

DROP VIEW  "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAdministradorasFondos"; 

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAdministradorasFondos" AS 

	SELECT    
		  "RutSociedadAdmFFMM" 
		 ,"SociedadAdmFFMM"
		 ,CONCAT("RutSociedadAdmFFMM" ,CONCAT(' - ',"SociedadAdmFFMM" )) AS "Codigo"
		 ,FONIND."IdEstado"
		 ,ENTREC."IdEntidadRecaudadora"  
	FROM "BSCL.FondoMutuo.BD::FMM.FondosIndustria" FONIND
	INNER JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora" ENTREC
	ON FONIND."RutSociedadAdmFFMM" = ENTREC."DocumentoIdentidad"
	GROUP BY "RutSociedadAdmFFMM" , "SociedadAdmFFMM",FONIND."IdEstado",ENTREC."IdEntidadRecaudadora";


