SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda"
AS 

SELECT CANASTAMONEDA."IdEmpresa"
	 , CANASTAMONEDA."IdCanasta" 
	 , CANASTAMONEDA."IdMoneda"
	 , CANASTA."Nombre"	AS "NombreCanasta"
  FROM "BSCL.Mesa.BD::FWD.CanastaMoneda" AS CANASTAMONEDA
  INNER JOIN "BSCL.Mesa.BD::FWD.Canasta" AS CANASTA
          ON CANASTA."IdCanasta" = CANASTAMONEDA."IdCanasta";
  		  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
	
