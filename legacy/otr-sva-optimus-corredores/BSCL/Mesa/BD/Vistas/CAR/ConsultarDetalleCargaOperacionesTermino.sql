SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarDetalleCargaOperacionesTermino";
GO 
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarDetalleCargaOperacionesTermino" AS

SELECT 
DETALLE."IdCargaMasivaDetalle"
,DETALLE."Mercado"
,DETALLE."SecuenciaCartera"
,DETALLE."Nemotecnico"
,DETALLE."Emisor"
,DETALLE."Boveda"
,DETALLE."ReajusteCartera"
,DETALLE."MonedaLiquidacion"
,DETALLE."FechaVencimiento"
,DETALLE."Cantidad"
,DETALLE."ValorRescate"
,DETALLE."Fungible"
,DETALLE."Tasa"
,DETALLE."Monto"
,DETALLE."IdCargaMasivaDetalleCabecera"
,DETALLE."ObservacionValidacion"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle" AS DETALLE
WHERE DETALLE."TipoRegistro" = 'D' AND DETALLE."IdEstado" IN ('ING', 'PRO');
GO 
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarDetalleCargaOperacionesTermino" TO  BCS_DESARROLLADOR WITH GRANT OPTION;