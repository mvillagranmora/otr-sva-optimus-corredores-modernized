SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad" ( "IdEstado",
      "NombreEstado",
      "IdEstadoContabilidad" ) AS 
SELECT
EST."IdEstado", 
EST."Nombre" AS "NombreEstado",
CON."IdEstadoContabilidad"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" EST
JOIN  "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad" CON ON CON."IdEstado" = EST."IdEstado"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
