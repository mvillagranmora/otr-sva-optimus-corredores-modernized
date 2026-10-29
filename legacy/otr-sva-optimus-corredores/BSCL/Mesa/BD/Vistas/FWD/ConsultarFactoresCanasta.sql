SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta"
AS 

SELECT Factores."IdEmpresa"
	 , Factores."IdCanasta"
	 , Factores."IdFactor"
	 , Factores."Hasta"
	 , Factores."Factor"
  FROM "BSCL.Mesa.BD::FWD.FactorCanasta" AS Factores 
  Inner Join "BSCL.Mesa.BD::FWD.Canasta" AS Canasta
  		  On Canasta."IdCanasta" = Factores."IdCanasta";
  		  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

