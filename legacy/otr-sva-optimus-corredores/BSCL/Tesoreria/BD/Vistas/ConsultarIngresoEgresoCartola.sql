SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoCartola";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoCartola" AS
SELECT	 IE."IdTipoPersona"
		,IE."IdPersona"
		,POR_T."IdCliente"
		,IE."IdPortafolio"

		,IE."IdTipoPersonaTitularPago"
		,IE."IdPersonaTitularPago"
		,POR_P."IdCliente" AS "IdClienteTitularPago"
		,IE."IdPortafolioTitularPago"
		
		,IE."IdTipoMovimiento"
		,IE."IdMoneda"
		,IE."Monto"
		,IE."FechaLiquidacion"
		,IE."IdEstado"
		,IE."IdEmpresa"
  FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"   AS IE
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR_T ON POR_T."IdPortafolio" = IE."IdPortafolio"
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR_P ON POR_P."IdPortafolio" = IE."IdPortafolioTitularPago"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoCartola" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
