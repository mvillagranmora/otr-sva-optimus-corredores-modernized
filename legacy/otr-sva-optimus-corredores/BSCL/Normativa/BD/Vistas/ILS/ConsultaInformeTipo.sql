SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeTipo";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeTipo" 
AS

	SELECT  
		IT."IdTipoInforme" 	 
		,IT."Nombre" 			AS	"Nombre"
		,IT."IdEmpresa"		
		,EMP."Nombre"	     	AS	"NombreEmpresa"     		
		,IT."Estado" 	    
	FROM "BSCL.Normativa.BD::ILS.InformeTipo" IT
	INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON IT."IdEmpresa" = EMP."IdEmpresa";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeTipo" TO BCS_DESARROLLADOR WITH GRANT OPTION;
