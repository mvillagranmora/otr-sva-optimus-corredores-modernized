SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarEspeciesEnGarantia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarEspeciesEnGarantia" AS 
SELECT  A."IdEspecieGarantia",
	 	A."Codigo",
	 	A."Descripcion",
	 	A."IdEstado",
	 	B."Nombre" as "NombreEstado"
  FROM "BSCL.Custodia.BD::GTA.EspecieEnGarantia" AS A
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS B ON B."IdEstado" = A."IdEstado"
  WITH READ ONLY;	 	
 
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarEspeciesEnGarantia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;