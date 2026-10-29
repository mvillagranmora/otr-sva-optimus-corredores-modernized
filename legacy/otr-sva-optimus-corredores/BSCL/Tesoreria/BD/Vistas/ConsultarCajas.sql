SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajas";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCajas" AS
SELECT 	C."IdCaja",
		C."IdEmpresa",
		C."Codigo" AS "CodigoCaja",
		C."Nombre" AS "NombreCaja",
		C."IdEstado" AS "IdEstadoCaja", 		
		E."Nombre" AS "NombreEstado"
  FROM "BSCL.Tesoreria.BD::TES.Caja" AS C
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON E."IdEstado" = C."IdEstado"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCajas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;