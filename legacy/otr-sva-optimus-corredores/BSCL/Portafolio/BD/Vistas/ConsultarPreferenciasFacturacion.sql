SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion"  AS

	SELECT
		PREFFACT."IdEmpresa",
		PREFFACT."IdPortafolio",
		PREFFACT."IdCliente",
		PREFFACT."IdTipoCliente",
		PREFFACT."NombreTipoIndicador" as "Nombre",
		PREFFACT."CodigoTipoIndicador" as "Codigo",
		PREFFACT."ValorIndicador" as "Valor",
		PREFFACT."CodigoValorIndicador" as "CodigoValor",
		PREFFACT."NombreValorIndicador" as "NombreValor"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  PREFFACT
	WHERE "GrupoIndicador" = 'GRUPOFACTURACION';

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
