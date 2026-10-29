SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice" 
AS

SELECT    "IdIndice"
    	 ,"IdEmpresa" 	 
		 ,"Nombre" 	     	
		 ,"NombreCorto" 
		 ,"Tipo"
		 ,"Estado" 	    
FROM "BSCL.Normativa.BD::ILS.Indice";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice" TO BCS_DESARROLLADOR WITH GRANT OPTION;
