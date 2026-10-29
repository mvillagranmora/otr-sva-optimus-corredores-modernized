SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarEstadoPrograma"; 
 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarEstadoPrograma"
AS SELECT
	 estpla."IdEstado",
	 est."Nombre" as "NombreEstado"
FROM "BSCL.Plataforma.BD::PLA.EstadoPla" estpla
INNER JOIN  "BSCL.Plataforma.BD::PLA.Estado" est on est."IdEstado" = estpla."IdEstado" and "NombreEntidad" = 'PROGRAMA';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.ConsultarEstadoPrograma" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
