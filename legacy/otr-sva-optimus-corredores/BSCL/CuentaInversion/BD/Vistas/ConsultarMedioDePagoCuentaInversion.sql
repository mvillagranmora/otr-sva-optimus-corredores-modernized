
SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMedioDePagoCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMedioDePagoCuentaInversion" AS

SELECT   MP."IdMedioDePago"
		,MP."TipoMedioDePago"
		,MP."Nombre"  
		,MP."UsaCuentaBancaria"  
		,MP."UsaTarjetaBancaria"  
		,MP."IdEstado"
		,MP."NombreEstado"
		,MPC."IdTipoMovimiento"
		,MPC."DesplegarEnCombobox"
FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" AS MP
INNER JOIN "BSCL.CuentaInversion.BD::CTA.TipoMovimientoMedioDePago" AS MPC ON (MPC."IdMedioDePago" = MP."IdMedioDePago")

WITH READ ONLY;

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarMedioDePagoCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;