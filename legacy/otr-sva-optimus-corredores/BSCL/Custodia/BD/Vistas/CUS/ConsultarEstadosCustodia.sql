SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia"
AS 
SELECT
	 "IdEstadoCustodia",
	 "IdEstado",
	 UPPER("NombreEntidad") AS "NombreEntidad",
	 "Descripcion"
FROM "BSCL.Custodia.BD::CUS.EstadoCustodia";


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
