SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoCargaMasiva";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoCargaMasiva" AS
SELECT
"IdEstado"
,"Nombre" AS "Nombre"
FROM "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN 
('ING'
,'INV'
,'PRO'
,'ERR'
);


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;