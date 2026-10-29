SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
AS 
SELECT
"IdComprobanteFormaDePago"
,"IdEmpresa"
,"FolioOperacion"
,"IdTipoProducto"
,"IdTipoMovimiento"
,CASE WHEN "IdTipoMovimiento" = 'MOVEGR' THEN 'ENTREGAMOS' ELSE 'RECIBIMOS' END AS "NombreTipoMovimiento"
,"IdMedioDePago"
,"NombreMedioDePago"
,"IdModalidadPago"
,"NombreModalidadDePago"
,"IdMoneda"
,"Monto"
,TO_CHAR("FechaIngresoRegistro",'yyyy-MM-dd') as "FechaIngresoRegistro"
,TO_CHAR("FechaCondicionLiquidacion",'yyyy-MM-dd') as "FechaCondicionLiquidacion"
,"FechaIngresoRegistro" AS "FechaIngresoRegistroGrilla"
,"FechaCondicionLiquidacion" AS "FechaCondicionLiquidacionGrilla"
,"IdEstado"
,"IdCartera"
FROM "BSCL.Mesa.BD::ComprobanteFormaDePago";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
