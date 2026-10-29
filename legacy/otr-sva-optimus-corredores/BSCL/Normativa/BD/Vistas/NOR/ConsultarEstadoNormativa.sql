SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" 

AS SELECT
	 EstadoNOR."IdEstado" ,
	 Estado."Nombre",
	 EstadoNOR."NombreEntidad" 
FROM "BSCL.Normativa.BD::NOR.EstadoNormativa" AS EstadoNOR
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado
 ON Estado."IdEstado" = EstadoNOR."IdEstado";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 