SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoSolicitudesPuntas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoSolicitudesPuntas" 
AS
SELECT 
       EST."IdEstado",
       EST."Nombre" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::PLA.Estado" EST
WHERE EST."IdEstado" IN ('PEN', 'APD', 'RZD');

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarEstadoSolicitudesPuntas" TO BCS_DESARROLLADOR WITH GRANT OPTION; 