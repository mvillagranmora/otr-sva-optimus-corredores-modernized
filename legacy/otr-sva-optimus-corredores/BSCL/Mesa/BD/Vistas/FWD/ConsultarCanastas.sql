SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanasta";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanasta"
AS 

SELECT "IdEmpresa"
	 , "IdCanasta" 
	 , "Nombre"
FROM "BSCL.Mesa.BD::FWD.Canasta"
GROUP BY "IdEmpresa"
	 , "IdCanasta" 
	 , "Nombre";
	
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanasta" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
	
