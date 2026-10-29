SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarEstadoContratoSuscripcionCuota";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarEstadoContratoSuscripcionCuota" AS
SELECT
"IdEstado"
,"Nombre" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN 
('ANU'
,'ING'
,'RVC'
,'RZD');


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::CSC.ConsultarEstadoContratoSuscripcionCuota" TO  BCS_DESARROLLADOR WITH GRANT OPTION;