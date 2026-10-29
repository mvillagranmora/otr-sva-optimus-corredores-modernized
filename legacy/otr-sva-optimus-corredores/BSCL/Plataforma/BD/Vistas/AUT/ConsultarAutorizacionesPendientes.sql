SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacionesPendientes"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacionesPendientes" AS
SELECT "IdConcepto" as "IdConcepto",
"IdTipoOperacion" as "IdTipoOperacion",
"NumeroOperacionConcepto" as "NumeroOperacionConcepto",
"IdCliente" as "IdCliente"
FROM "BSCL.Plataforma.BD::AUT.Evento" AS Evento
WHERE "IdEstado" = 'VIG';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacionesPendientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 