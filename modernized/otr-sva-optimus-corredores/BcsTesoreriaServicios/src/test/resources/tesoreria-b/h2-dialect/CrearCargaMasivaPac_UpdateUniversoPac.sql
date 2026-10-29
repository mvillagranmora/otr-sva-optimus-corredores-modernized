-- HANA-SOURCE: cl/bcs/tesoreria/sql/CrearCargaMasivaPac_UpdateUniversoPac.sql
-- H2 rewrite of the HANA UPDATE PAC SET .. FROM "PAC.MandatoPAC" PAC LEFT JOIN SAAPV .. LEFT JOIN CSC .. WHERE ..: the LEFT JOINs only feed the WHERE
-- (COALESCE(SAP.IdEstado,'') = 'VIG' OR COALESCE(CSC.Estado,'') = 'ING'), so they become scalar subqueries; one PAC row is updated at most once either way.
UPDATE "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS PAC
SET "EstadoCargo" = 'ACT'
WHERE PAC."IdentServicio" = :IdentServicio AND
	PAC."Estado" = 'ING' AND
	PAC."EstadoCargo" = 'INA' AND
	(COALESCE((SELECT MAX(SAP."IdEstado") FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS SAP WHERE PAC."FolioSAAPV" = SAP."FolioContratoSAAPV" AND SAP."IdEstado" = 'VIG'), '') = 'VIG' OR
	COALESCE((SELECT MAX(CSC."Estado") FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" AS CSC WHERE PAC."FolioCSC" = CSC."FolioCSC" AND CSC."Estado" = 'ING' AND CSC."TipoPago" = 'PAC'), '') = 'ING')
