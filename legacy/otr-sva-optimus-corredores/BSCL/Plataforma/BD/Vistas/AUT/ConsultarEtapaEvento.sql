SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento" as
SELECT
    EtapaEvento."IdEtapaEvento",
    EtapaEvento."IdEvento",
    EtapaEvento."IdEtapa",
    EtapaEvento."Codigo",
    EtapaEvento."Nombre",
    EtapaEvento."Orden",
    EtapaEvento."TipoAutorizacion",
    EtapaEvento."MinimoAutorizan",
    EtapaEvento."IdUsuarioCambioEstado",
    EtapaEvento."IdRolCambioEstado",
    EtapaEvento."FechaHoraCambioEstado",
    EtapaEvento."IdEstado",
    Estado."Nombre" AS "NombreEstado"
FROM "BSCL.Plataforma.BD::AUT.EtapaEvento" AS EtapaEvento
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado 
    ON EtapaEvento."IdEstado" = Estado."IdEstado";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
