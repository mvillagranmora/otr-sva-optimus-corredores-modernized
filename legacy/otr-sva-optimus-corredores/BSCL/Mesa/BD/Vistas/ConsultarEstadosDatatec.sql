SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosDatatec";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosDatatec"
AS 
SELECT "EstadoDatatec"."IdEstado"
     , "Estado"."Nombre"
FROM "BSCL.Mesa.BD::MES.EstadoOperacion" as "EstadoDatatec"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON "Estado"."IdEstado" = "EstadoDatatec"."IdEstado" 
WHERE "NombreEntidad" = 'DATATEC';
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarEstadosDatatec" TO BCS_DESARROLLADOR WITH GRANT OPTION;
