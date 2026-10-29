SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteCartola";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteCartola" AS
SELECT 	 
	 Comprobante."IdTipoPersona"
	,Comprobante."IdPersona"

	,Comprobante."IdTipoPersonaTitularPago"
	,Comprobante."IdPersonaTitularPago"
	,POR_P."IdCliente" AS "IdClienteTitularPago"
	,Comprobante."IdPortafolioTitularPago"
	
	,Comprobante."IdTipoMovimiento"
	,Comprobante."IdMoneda"
	,Comprobante."Monto"
	,Comprobante."FechaLiquidacion"
	,Comprobante."IdEmpresa"
	,Comprobante."IdEstado"

FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR_P ON POR_P."IdPortafolio" = Comprobante."IdPortafolioTitularPago"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteCartola" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



