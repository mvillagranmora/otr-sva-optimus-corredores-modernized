SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarEstadoMandatoPAC";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarEstadoMandatoPAC" AS
SELECT
"IdEstado"
,"Nombre" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN 
('ANU'
,'ING'
,'PAU'
,'RVC'
,'RZD');


GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::PAC.ConsultarEstadoMandatoPAC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
