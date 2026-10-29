SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMedioDePago";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMedioDePago" AS
SELECT
	 MEDPAG."IdTipoProducto"
	,MEDPAG."IdInstrumento"
	,MEDPAG."IdMonedaPrincipal"
	,MEDPAG."IdMonedaSecundaria"
	,MEDPAG."IdMedioDePago"
	,MEDPAG."NombreMedioDePago"
	,MEDPAG."IdEstado"
	,MODPAG."IdModalidadDePago"
FROM "BSCL.Mesa.BD::MES.InstrumentoMonedaMedioDePago" MEDPAG
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" MODPAG
	ON (MEDPAG."IdMedioDePago" = MODPAG."IdMedioDePago" AND MODPAG."IdEstado" = 'ACT');

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMedioDePago" TO BCS_DESARROLLADOR WITH GRANT OPTION;
