SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTotalDerechoJuntaAccionista";
 
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTotalDerechoJuntaAccionista" 
AS
SELECT 
  CLIENTE_DERECHO."IdEventoJuntaAccionista" 			AS "IdEventoJuntaAccionista"
, SUM(CLIENTE_DERECHO."CantidadDerecho")				AS "TotalCantidadDerecho"
, SUM(CLIENTE_DERECHO."CantidadDerechoCalculado")		AS "TotalCantidadDerechoCalculado"						
FROM "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista" AS CLIENTE_DERECHO
GROUP BY "IdEventoJuntaAccionista";
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTotalDerechoJuntaAccionista" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
