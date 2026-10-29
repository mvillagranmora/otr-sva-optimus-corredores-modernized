SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria" AS
SELECT   MP."IdMedioDePago"
		,MP."TipoMedioDePago"
		,MP."Nombre"  
		,MP."UsaCuentaBancaria"  
		,MP."UsaTarjetaBancaria"  
		,MP."IdEstado"
		,MP."NombreEstado"
		,MPP."IdTipoMovimiento"
		,MPP."AfectaSaldoEnEstado"
		,MPP."DesplegarEnCombobox"
		,MPMNTES."IdMoneda"
		,MPMNTES."IdEmpresa"
		,MPMNTES."RequiereRevisionDocumento"
  FROM "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria" as MPMNTES
 INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" as MP ON MP."IdMedioDePago" = MPMNTES."IdMedioDePago"
 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as MPP ON MPP."IdMedioDePago" = MP."IdMedioDePago"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarMedioDePagoTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
