INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT TABLE_KEY,'Custodia_CustodiaFechaLiquidacion', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacion";

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT  table_key,'Custodia_CustodiaFechaOperacion', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaOperacion"

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_CustodiaFechaOperacionHist', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaOperacionHist"
WHERE FECHACUSTODIA IN ('2026-05-21');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'Custodia_CustodiaFechaLiquidacionHist', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL"."BSCL.Replica.BD.Vistas::ReplicaCustodia_CustodiaFechaLiquidacionHist"
WHERE FECHACUSTODIA IN ('2026-05-16');



select * from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica"

SELECT "FechaCustodia" ,count(1)
FROM "BSCL"."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion" 
--where "FechaCustodia"  = '2024-01-16'
group by "FechaCustodia" 
order by "FechaCustodia" desc


SELECT "FechaCustodia" ,count(1)
FROM "BSCL"."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion" 
--where "FechaCustodia"  = '2024-01-16'
group by "FechaCustodia" 
order by "FechaCustodia" desc

SELECT "DocumentoIdentidad" ,count(1)
FROM "BSCL"."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion" 
--where "FechaCustodia"  = '2024-01-18'
--and "DocumentoIdentidad" = ''
group by "DocumentoIdentidad" 
order by "DocumentoIdentidad"

INSERT INTO "BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
("IdEmpresa", "TipoFechaConsulta", "FechaCustodia", "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento", ISIN, "IdInstrumento", "Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", "DocumentoIdentidad", "NombreCliente", "CodigoPortafolio", "IdBoveda", "NumeroCuentaBoveda", "Relacion", "DescripcionRelacion", "CantidadCustodia", "PPPCustodia", "ValorPPPCustodia", "MontoPPPCustodia", "PrecioMercadoCustodia", "Rentabilidad", "ValorMercadoCustodia", "MontoMercadoCustodia", "DiferenciaMercadoCustodia", "IdMonedaReajuste", "EsPagaderoEn", "CantidadSimulCom", "PPPSimulCom", "ValorPPPSimulCom", "PrecioMercadoSimulCom", "ValorMercadoSimulCom", "DiferenciaMercadoSimulCom", "CantidadSimulVen", "PPPSimulVen", "ValorPPPSimulVen", "PrecioMercadoSimulVen", "ValorMercadoSimulVen", "DiferenciaMercadoSimulVen", "CantidadPrestamo", "PPPPrestamo", "ValorPPPPrestamo", "PrecioMercadoPrestamo", "ValorMercadoPrestamo", "DiferenciaMercadoPrestamo", "CantidadEmpresti", "PPPEmpresti", "ValorPPPEmpresti", "PrecioMercadoEmpresti", "ValorMercadoEmpresti", "DiferenciaMercadoEmpresti", "CantidadGarantia", "PPPGarantia", "ValorPPPGarantia", "PrecioMercadoGarantia", "ValorMercadoGarantia", "DiferenciaMercadoGarantia", "CantidadTransIng", "CantidadTransEgr", "TotalPosicion", "TotalPosicionPresunta", "CantidadPactosV", "PPPPactosV", "ValorPPPPactosV", "PrecioMercadoPactosV", "ValorMercadoPactosV", "DiferenciaMercadoPactosV", "CantidadPactosC", "PPPPactosC", "ValorPPPPactosC", "PrecioMercadoPactosC", "ValorMercadoPactosC", "DiferenciaMercadoPactosC", "IdMatrizAgrupador", "EsADC", "FechaFuentePrecioMercadoCustodia", "IdRegimenFondoMutuo")
SELECT "IdEmpresa", "TipoFechaConsulta", '2025-09-28', "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento", ISIN, "IdInstrumento", "Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", "DocumentoIdentidad", "NombreCliente", "CodigoPortafolio", "IdBoveda", "NumeroCuentaBoveda", "Relacion", "DescripcionRelacion", "CantidadCustodia", "PPPCustodia", "ValorPPPCustodia", "MontoPPPCustodia", "PrecioMercadoCustodia", "Rentabilidad", "ValorMercadoCustodia", "MontoMercadoCustodia", "DiferenciaMercadoCustodia", "IdMonedaReajuste", "EsPagaderoEn", "CantidadSimulCom", "PPPSimulCom", "ValorPPPSimulCom", "PrecioMercadoSimulCom", "ValorMercadoSimulCom", "DiferenciaMercadoSimulCom", "CantidadSimulVen", "PPPSimulVen", "ValorPPPSimulVen", "PrecioMercadoSimulVen", "ValorMercadoSimulVen", "DiferenciaMercadoSimulVen", "CantidadPrestamo", "PPPPrestamo", "ValorPPPPrestamo", "PrecioMercadoPrestamo", "ValorMercadoPrestamo", "DiferenciaMercadoPrestamo", "CantidadEmpresti", "PPPEmpresti", "ValorPPPEmpresti", "PrecioMercadoEmpresti", "ValorMercadoEmpresti", "DiferenciaMercadoEmpresti", "CantidadGarantia", "PPPGarantia", "ValorPPPGarantia", "PrecioMercadoGarantia", "ValorMercadoGarantia", "DiferenciaMercadoGarantia", "CantidadTransIng", "CantidadTransEgr", "TotalPosicion", "TotalPosicionPresunta", "CantidadPactosV", "PPPPactosV", "ValorPPPPactosV", "PrecioMercadoPactosV", "ValorMercadoPactosV", "DiferenciaMercadoPactosV", "CantidadPactosC", "PPPPactosC", "ValorPPPPactosC", "PrecioMercadoPactosC", "ValorMercadoPactosC", "DiferenciaMercadoPactosC", "IdMatrizAgrupador", "EsADC", "FechaFuentePrecioMercadoCustodia", "IdRegimenFondoMutuo"
FROM BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
WHERE "FechaCustodia" = '2025-09-27';

INSERT INTO "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"
("IdEmpresa", "TipoFechaConsulta", "FechaCustodia", "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento", ISIN, "IdInstrumento", "Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", "DocumentoIdentidad", "NombreCliente", "CodigoPortafolio", "IdBoveda", "NumeroCuentaBoveda", "Relacion", "DescripcionRelacion", "CantidadCustodia", "PPPCustodia", "ValorPPPCustodia", "MontoPPPCustodia", "PrecioMercadoCustodia", "Rentabilidad", "ValorMercadoCustodia", "MontoMercadoCustodia", "DiferenciaMercadoCustodia", "IdMonedaReajuste", "EsPagaderoEn", "CantidadSimulCom", "PPPSimulCom", "ValorPPPSimulCom", "PrecioMercadoSimulCom", "ValorMercadoSimulCom", "DiferenciaMercadoSimulCom", "CantidadSimulVen", "PPPSimulVen", "ValorPPPSimulVen", "PrecioMercadoSimulVen", "ValorMercadoSimulVen", "DiferenciaMercadoSimulVen", "CantidadPrestamo", "PPPPrestamo", "ValorPPPPrestamo", "PrecioMercadoPrestamo", "ValorMercadoPrestamo", "DiferenciaMercadoPrestamo", "CantidadEmpresti", "PPPEmpresti", "ValorPPPEmpresti", "PrecioMercadoEmpresti", "ValorMercadoEmpresti", "DiferenciaMercadoEmpresti", "CantidadGarantia", "PPPGarantia", "ValorPPPGarantia", "PrecioMercadoGarantia", "ValorMercadoGarantia", "DiferenciaMercadoGarantia", "CantidadTransIng", "CantidadTransEgr", "TotalPosicion", "TotalPosicionPresunta", "CantidadPactosV", "PPPPactosV", "ValorPPPPactosV", "PrecioMercadoPactosV", "ValorMercadoPactosV", "DiferenciaMercadoPactosV", "CantidadPactosC", "PPPPactosC", "ValorPPPPactosC", "PrecioMercadoPactosC", "ValorMercadoPactosC", "DiferenciaMercadoPactosC", "IdMatrizAgrupador", "EsADC", "FechaFuentePrecioMercadoCustodia", "IdRegimenFondoMutuo")
SELECT "IdEmpresa", "TipoFechaConsulta", '2025-09-28', "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento", ISIN, "IdInstrumento", "Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", "DocumentoIdentidad", "NombreCliente", "CodigoPortafolio", "IdBoveda", "NumeroCuentaBoveda", "Relacion", "DescripcionRelacion", "CantidadCustodia", "PPPCustodia", "ValorPPPCustodia", "MontoPPPCustodia", "PrecioMercadoCustodia", "Rentabilidad", "ValorMercadoCustodia", "MontoMercadoCustodia", "DiferenciaMercadoCustodia", "IdMonedaReajuste", "EsPagaderoEn", "CantidadSimulCom", "PPPSimulCom", "ValorPPPSimulCom", "PrecioMercadoSimulCom", "ValorMercadoSimulCom", "DiferenciaMercadoSimulCom", "CantidadSimulVen", "PPPSimulVen", "ValorPPPSimulVen", "PrecioMercadoSimulVen", "ValorMercadoSimulVen", "DiferenciaMercadoSimulVen", "CantidadPrestamo", "PPPPrestamo", "ValorPPPPrestamo", "PrecioMercadoPrestamo", "ValorMercadoPrestamo", "DiferenciaMercadoPrestamo", "CantidadEmpresti", "PPPEmpresti", "ValorPPPEmpresti", "PrecioMercadoEmpresti", "ValorMercadoEmpresti", "DiferenciaMercadoEmpresti", "CantidadGarantia", "PPPGarantia", "ValorPPPGarantia", "PrecioMercadoGarantia", "ValorMercadoGarantia", "DiferenciaMercadoGarantia", "CantidadTransIng", "CantidadTransEgr", "TotalPosicion", "TotalPosicionPresunta", "CantidadPactosV", "PPPPactosV", "ValorPPPPactosV", "PrecioMercadoPactosV", "ValorMercadoPactosV", "DiferenciaMercadoPactosV", "CantidadPactosC", "PPPPactosC", "ValorPPPPactosC", "PrecioMercadoPactosC", "ValorMercadoPactosC", "DiferenciaMercadoPactosC", "IdMatrizAgrupador", "EsADC", "FechaFuentePrecioMercadoCustodia", "IdRegimenFondoMutuo"
FROM BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"
WHERE "FechaCustodia" = '2025-12-27';



SELECT
    dbschemas.name AS SchemaName,
    dbtables.name AS TableName,
    dbindexes.name AS IndexName,
    indexstats.index_id,
    indexstats.avg_fragmentation_in_percent,
    indexstats.page_count
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') AS indexstats
INNER JOIN sys.tables dbtables
    ON dbtables.object_id = indexstats.object_id
INNER JOIN sys.schemas dbschemas
    ON dbtables.schema_id = dbschemas.schema_id
INNER JOIN sys.indexes AS dbindexes
    ON dbindexes.object_id = indexstats.object_id
   AND indexstats.index_id = dbindexes.index_id
WHERE indexstats.database_id = DB_ID()
  AND indexstats.page_count > 1000              -- evita índices muy pequeños
  AND indexstats.avg_fragmentation_in_percent > 30 -- regla típica para REBUILD
ORDER BY indexstats.avg_fragmentation_in_percent DESC;


ALTER INDEX [ti_X0] ON dbo.Custodia_CustodiaFechaLiquidacion REBUILD;
ALTER INDEX [ti_x3] ON dbo.Custodia_CustodiaFechaOperacion REBUILD;
ALTER INDEX [IX_CustodiaFechaLiquidacion_Syncro1] ON dbo.Custodia_CustodiaFechaLiquidacion REBUILD;



select count(1) from "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoAnalitica" btbvccfdpa 


SELECT year("FechaComprobante") as y,count(1)  
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoAnalitica"  
--where "FechaComprobante" > '2025-03-14'
group by year("FechaComprobante") 
order by y desc


select count(1) from 
"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraRFIFAnalitica" 


select * from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraOtrosProductosHisAnalitica" bc

SELECT CAST(MIN("IdPortafolio") AS VARCHAR)||'' AS MIN,CAST(MAX("IdPortafolio") AS VARCHAR) ||'' AS MAX,FLOOR("IdPortafolio"/100) AS GRUPO,COUNT(1)*1.0 as C,1.0*sum(CASE WHEN length("IdTipoBloqueo") = 0 THEN 1 when "IdTipoBloqueo" = 'BNOUSO' THEN 3 ELSE 7 END + "IdPortafolio" + "IdMatrizAgrupador" + CASE WHEN "Proposito" IS NULL THEN 0 ELSE LENGTH("Proposito") END + CASE "IdEstado" WHEN 'ACT' THEN 11 WHEN 'INA' THEN 13 WHEN 'BLO' THEN 15 ELSE 5 END) AS HASH FROM BSCL."BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" WHERE (FLOOR("IdPortafolio"/100) = 0 OR 1 = 1) GROUP BY FLOOR("IdPortafolio"/100) 


SELECT CASE WHEN length("IdTipoBloqueo") = 0 THEN 1 when "IdTipoBloqueo" = 'BNOUSO' THEN 3 ELSE 7 END, *, "IdPortafolio" + "IdMatrizAgrupador" + CASE WHEN "Proposito" IS NULL THEN 0 ELSE LENGTH("Proposito") END + CASE "IdEstado" WHEN 'ACT' THEN 11 WHEN 'INA' THEN 13 WHEN 'BLO' THEN 15 ELSE 5 END AS HASH FROM BSCL."BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" WHERE (FLOOR("IdPortafolio"/100) = 0)


select count(1) from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLogAnalitica" bpbvpcla 



select * from sys.TRIGGERS t where SUBJECT_TABLE_NAME like '%CuentaB%'


select count(1) from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 

truncate table "BSCL.Plataforma.BD::PLA.Replica.WorkStack" 



select * from sys.triggers where SUBJECT_TABLE_NAME like '%CTA%'


select * from "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ParametroFinanciero" brbvrppf 




select * from "BSCL.Plataforma.BD::MER.CargaEmisores" bpbmce 
where EMISOR like '%BICE%'


select "Table" ,count(1) 
from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bp
group by "Table" 



select * from "BSCL.Plataforma.BD::PLA.Replica.WorkStack"

truncate table "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaDatosEntrada"


select * from "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaDatosEntrada"

select count(1) from "BSCL.Orden.BD::ORD.ControlMercado" bobocm  


select * from "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoAnalitica" bpbvictia 

select * from "BSCL.Custodia.BD::CUS.Cartera" bpbvictia 



select * from "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento";

select * from "BSCL.Contabilidad.BD::ITZ.Movimiento";

select * from "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsientoEnvioContabilida";

select * from "BSCL.Custodia.BD::CUS.CuadraturaCustodia";

select * from "BSCL.Custodia.BD::CUS.EventoCapitalCliente";

select * from "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito";

select * from "BSCL.APV.BD::SCS.ContratoSAAPV";

select * from "BSCL.Custodia.BD::CUS.Cartera";


select * from "BSCL.Custodia.BD::CUS.Cartera"

SELECT * FROM "BSCL.Custodia.BD::CUS.Cartera"
FOR SYSTEM_TIME AS OF '2026-05-18 16:00:00';

truncate table "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento";
truncate table "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja";
truncate table "BSCL.Custodia.BD::CUS.CuadraturaCustodia";
truncate table "BSCL.Custodia.BD::CUS.EventoCapitalClienteCartera";

16 00 -> 20 00
16 15 -> 21 00


19 -> 23
select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTOv2" brb
WHERE "Nemotecnico" like '%OSA%' AND FEC_MVTO LIKE '%2026-05%' AND RUT_CLI = ' 77176902-0'



select * from "BSCL.Facturacion.BD::FAC.Comprobante"

select * from "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"

select "NemotecnicoInstrumento",* from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
where "FechaIngresoRegistro" >= '2026-05-18 20:00:00'

select "FechaIngresoRegistro" , "IdTipoOperacion","Nemotecnico" ,"IdEstado" ,"EsNominal" ,"MueveCustodia" ,* 
from "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" bcb
where "FechaIngresoRegistro"  >= '2026-05-18 19:40:00'
order by bcb."FechaIngresoRegistro" asc


select "Nemotecnico" ,* from "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" bcb
where "FechaIngresoRegistro" >= '2026-05-18 20:00:00'
and "Nemotecnico" = 'CONCHATORO'


681197

select * from "BSCL.Plataforma.BD::PLA.Log" where "FechaRegistro" >= '2026-05-18 20:00:00'
order by "FechaRegistro"asc 



select "IdMovimientoComprobante" 
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTO
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" CPTO ON MVTO."IdConceptoMovimiento" = CPTO."IdConcepto"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" TCMBTE ON MVTO."IdTipoComprobante" = TCMBTE."IdTipoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CLIQ ON MVTO."IdCondicionLiquidacion" = CLIQ."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" TREAJ ON MVTO."IdTipoReajuste" = TREAJ."IdTipoReajuste"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoConvenio" TCONV ON MVTO."IdTipoConvenio" = TCONV."IdTipoConvenio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON MVTO."IdEmisor" = EMI."IdEmisor"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONLIQ ON MVTO."IdMonedaLiquidacion" = MONLIQ."IdMoneda"

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON MVTO."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON INS."IdTipoInstrumento" = TIPINS."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLAINS ON TIPINS."IdClasificacionTipoInstrumento" = CLAINS."IdClasificacionTipoInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::INS.RelacionInstrumento" RELINS ON MVTO."IdInstrumentoRelacionado" = RELINS."IdInstrumentoRelacionado" AND RELINS."IdEstado" = 'VIG'
--LEFT JOIN "BSCL.Plataforma.BD::INS.RelacionInstrumento" RELINS ON MVTO."IdInstrumentoRelacionado" = RELINS."IdInstrumento" AND RELINS."IdEstado" = 'VIG'
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" REL ON MVTO."IdInstrumentoRelacionado" = REL."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINSREL ON REL."IdTipoInstrumento" = TIPINSREL."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLAREL ON TIPINSREL."IdClasificacionTipoInstrumento" = CLAREL."IdClasificacionTipoInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" SUB ON MVTO."IdInstrumentoSubyacente" = SUB."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINSSUB ON SUB."IdTipoInstrumento" = TIPINSSUB."IdTipoInstrumento"

LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPDIGITADOR ON MVTO."IdEmpleadoDigitador" = EMPDIGITADOR."IdEmpleado"

LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" TPROD ON MVTO."IdTipoProducto" = TPROD."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONMVTO ON MVTO."IdMoneda" = MONMVTO."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSUB ON MVTO."IdMonedaSubyacente" = MONSUB."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TCANAL ON MVTO."IdTipoCanalIngreso" = TCANAL."IdTipoCanalIngreso"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON MVTO."IdTipoOperacion" = TOPE."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON MVTO."IdBoveda" = BOV."IdBoveda"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COR ON MVTO."IdCorredoraContraparte" = COR."IdCorredora"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" BOL ON MVTO."IdBolsa" = BOL."IdBolsa"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON MVTO."IdOrden" = ORD."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" INSREAJ ON MVTO."IdInstrumento" = INSREAJ."IdInstrumento" AND MVTO."IdTipoReajuste" = INSREAJ."IdTipoReajuste"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT ON PORT."IdPortafolio" = MVTO."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = PORT."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoCliente" tipocli ON CLI."IdTipoCliente" = tipocli."IdTipoCliente" AND tipocli."IdEmpresa" = CLI."IdEmpresa"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" PREF ON MVTO."IdPortafolio" = PREF."IdPortafolio" AND PREF."Codigo" = 'FAC_MODFAC'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPPORT ON PORT."IdEmpleadoEjecutivo" = EMPPORT."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPDIG ON MVTO."IdEmpleadoDigitador" = EMPDIG."IdEmpleado"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUCPORT ON SUCPORT."IdSucursal" = EMPPORT."IdSucursal"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUCDIG ON SUCDIG."IdSucursal" = EMPDIG."IdSucursal"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CREF ON CREF."IdComprobante" = MVTO."IdComprobanteReferencia"
where mvto."FechaIngresoRegistro" >= '2026-05-18'



alter system disconnect session '352576'



SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.FMMovtosSpot" br
where FECHAOPERACION >= '2026-05-20'


select * from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 

select * from "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT" brbvst 
where FEC_LIQUID >= '2026-05-19'

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst 
where FEC_MVTO >= '2026-05-19'


select top 100 * from "BSCL.Plataforma.BD::PLA.AuditoriaServicios" bpbpas 
order by "FechaRegistro" desc
