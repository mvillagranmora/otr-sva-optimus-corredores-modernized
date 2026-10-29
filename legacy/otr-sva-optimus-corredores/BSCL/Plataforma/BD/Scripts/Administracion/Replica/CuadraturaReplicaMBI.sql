--
select FechaPatrimonio,count(1)
from CuentaInversion_PatrimonioCajaFechaLiquidacion cipcfl
where FechaPatrimonio >= '2026-05-16'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM CuentaInversion_PatrimonioCajaFechaLiquidacion
WHERE FechaPatrimonio in ('2026-05-24')

--
select FechaPatrimonio,count(1)
from CuentaInversion_PatrimonioCajaFechaOperacion cipcfl
where FechaPatrimonio >= '2026-05-18'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM CuentaInversion_PatrimonioCajaFechaOperacion
WHERE FechaPatrimonio in ('2026-05-24')

--CuentaInversion_SaldoFechaEfectiva
select FechaSaldo ,count(1) 
from CuentaInversion_SaldoFechaEfectiva cisfe 
where FechaSaldo >= '2026-05-18' 
group by FechaSaldo order by FechaSaldo desc

delete from CuentaInversion_SaldoFechaEfectiva 
where fechasaldo = '2026-04-18'

--CuentaInversion_SaldoFechaMovimiento
select FechaSaldo ,count(1) 
from CuentaInversion_SaldoFechaMovimiento cisfe 
where FechaSaldo >= '2026-05-18'  
group by FechaSaldo order by FechaSaldo desc

delete from CuentaInversion_SaldoFechaMovimiento 
where fechasaldo = '2026-05-09'

select count(1) from Custodia_CarteraRV c

select FechaRespaldo ,count(1)
from Custodia_CarteraRVHis cpcfl
where FechaRespaldo >= '2023-11-15'
group by FechaRespaldo order by FechaRespaldo desc; 

DELETE FROM Custodia_CarteraRVHis
WHERE FechaRespaldo >= '2023-11-17';

--Custodia_CustodiaFechaLiquidacion
select FechaCustodia ,count(1) 
from Custodia_CustodiaFechaLiquidacion ccfl
where fechacustodia >= '2026-05-18'
group by FechaCustodia order by FechaCustodia desc




delete from Custodia_CustodiaFechaLiquidacion 
where fechacustodia in ('2026-05-23','2026-05-24')

--Custodia_CustodiaFechaOperacion
select FechaCustodia ,count(1) 
from Custodia_CustodiaFechaOperacion ccfl 
WHERE FechaCustodia >= '2026-05-15' 
group by FechaCustodia order by FechaCustodia desc

delete from Custodia_CustodiaFechaOperacion 
where fechacustodia in ('2026-05-17')

--
select FechaPatrimonio,count(1)
from Custodia_PatrimonioConsolidadoFechaLiquidacion cpcfl
where FechaPatrimonio >= '2026-05-18'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM Custodia_PatrimonioConsolidadoFechaLiquidacion
WHERE FechaPatrimonio in ('2026-05-23');


select FechaPatrimonio,count(1)
from Custodia_PatrimonioConsolidadoFechaOperacion cpcfl
where FechaPatrimonio >= '2026-05-18'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM Custodia_PatrimonioConsolidadoFechaOperacion
WHERE FechaPatrimonio in ('2026-05-24');



--
select FechaPatrimonio,count(1)
from Custodia_PatrimonioCustodiaFechaLiquidacion cpcfl
where FechaPatrimonio >= '2026-05-18'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM Custodia_PatrimonioCustodiaFechaLiquidacion
WHERE FechaPatrimonio in ('2026-05-23');

--
select FechaPatrimonio,count(1)
from Custodia_PatrimonioCustodiaFechaOperacion cpcfl
where FechaPatrimonio >= '2026-05-18'
group by FechaPatrimonio order by FechaPatrimonio desc; 

DELETE FROM Custodia_PatrimonioCustodiaFechaOperacion
WHERE FechaPatrimonio in ('2026-05-23','2026-05-24');


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

ALTER INDEX [ti_X2] ON dbo.Custodia_MovimientoCarteraIngresos REBUILD;
ALTER INDEX [ti_x1] ON dbo.Custodia_MovimientoCarteraIngresos REBUILD;
ALTER INDEX [ti_x0] ON dbo.Custodia_MovimientoCarteraIngresos REBUILD;

ALTER INDEX [ti_x2] ON dbo.Custodia_MovimientoCarteraEgresos REBUILD;
ALTER INDEX [ti_x1] ON dbo.Custodia_MovimientoCarteraEgresos REBUILD;
ALTER INDEX [ti_x0] ON dbo.Custodia_MovimientoCarteraEgresos REBUILD;


ALTER INDEX [ti_X0] ON dbo.Custodia_CustodiaFechaLiquidacion REBUILD;
ALTER INDEX [IX_CustodiaFechaOperacion_Syncro1] ON dbo.Custodia_CustodiaFechaLiquidacion REBUILD;
ALTER TABLE dbo.Custodia_CustodiaFechaLiquidacion REBUILD;

ALTER INDEX [ti_X4] ON dbo.Custodia_CustodiaFechaOperacion REBUILD;
ALTER INDEX [ti_x3] ON dbo.Custodia_CustodiaFechaOperacion REBUILD;
ALTER INDEX [IX_CustodiaFechaOperacion_Syncro1] ON dbo.Custodia_CustodiaFechaOperacion REBUILD;
ALTER TABLE dbo.Custodia_CustodiaFechaOperacion REBUILD;

ALTER TABLE DELETE_KEYS_PIVOT REBUILD

ALTER INDEX [ti_x0] ON dbo.Tesoreria_ComprobanteFormaDePago REBUILD;
ALTER INDEX [ti_x1] ON dbo.Tesoreria_ComprobanteFormaDePago REBUILD;

ALTER INDEX [ti_X0] ON dbo.Tesoreria_Comprobante REBUILD;
ALTER INDEX [ti_x0] ON dbo.Tesoreria_ComprobanteDetalle REBUILD;

ALTER TABLE TBCTMVTO REBUILD;

ALTER TABLE Mesa_MovimientosFWD REBUILD;


ALTER TABLE Plataforma_Instrumento REBUILD;

ALTER TABLE Plataforma_MatrizComercialCorredora REBUILD;


ALTER TABLE Custodia_PatrimonioConsolidadoFechaLiquidacion REBUILD;
ALTER TABLE Custodia_PatrimonioConsolidadoFechaOperacion REBUILD;

ALTER TABLE CuentaInversion_SaldoFechaMovimiento REBUILD;
ALTER TABLE CuentaInversion_SaldoFechaEfectiva REBUILD;

ALTER TABLE Custodia_CarteraFMHis REBUILD;
ALTER TABLE Custodia_PatrimonioCustodiaFechaOperacion REBUILD;
ALTER TABLE Custodia_PatrimonioCustodiaFechaLiquidacion REBUILD;

ALTER TABLE Custodia_GarantiaHis REBUILD;
ALTER TABLE CuentaInversion_PatrimonioCajaFechaLiquidacion REBUILD;
ALTER TABLE CuentaInversion_PatrimonioCajaFechaOperacion REBUILD;


select * from Custodia_CarteraOtrosProductosHis

alter table Custodia_CarteraOtrosProductosHis
add FechaRespaldo varchar2(10) NULL

truncate table Custodia_CarteraOtrosProductosHis

INSERT INTO ConsultarCarteraOtrosProductos
("FechaRespaldo", "IdEmpresa", "NombreEmpresa", "IdCartera", "FolioCartera", "FechaIngresoRegistro", "FechaModificacionRegistro", "IdCliente", "NombreCliente", "DocumentoIdentidad", "IdPortafolio", "CodigoPortafolio", "DescripcionPortafolio", "IdClasificacionTipoInstrumento", "ClasificacionTipoInstrumento", "IdTipoInstrumento", "TipoInstrumento", "IdInstrumento", "Nemotecnico", "IdMonedaInstrumento", "MonedaInstrumento", "IdTipoPrestamo", "TipoPrestamo", "CobraPrimaAlInicio", "IdTipoReajuste", "TipoReajuste", "IdEmisor", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "IdBoveda", "NombreBoveda", "FechaOperacionInicial", "FechaVencimientoOperacionInicial", "PlazoEnDiasOperacionInicial", "CantidadInicial", "PrecioCompraInicial", "TasaCompraInicial", "CorteMinimoInicial", "ValorRescateInicial", "MontoCompraInicial", "MontoCompraMonedaInstrumentoInicial", "PrecioAPlazoInicial", "MontoAPlazoInicial", "MontoAPlazoMonedaInstrumentoInicial", "FechaEfectivaInicial", "IdCondicionLiquidacionInicial", "NombreCondicionLiquidacionInicial", "FechaLiquidacionInicial", "FechaOperacion", "EGRFechaOperacion", "EGRFechaLiquidacion", "Cantidad", "PrecioCompra", "MontoCompraActualMonedaInstrumento", "PrecioPromedioCompra", "MontoPrecioPromedioCompra", "MontoPrecioPromedioMonedaInstrumento", "TasaCompra", "MontoATasaCompraActual", "ValorRescate", "PrecioAPlazo", "MontoAPlazo", "MontoAPlazoMonedaInstrumento", "IdCondicionLiquidacion", "FechaLiquidacion", "DurationCompraActual", "ConvexidadCompraActual", "FechaPrecioTasaMercadoActual", "FuenteTasaPrecioMercadoActual", "PrecioMercadoActual", "TasaMercadoActual", "MontoATasaMercadoActual", "MontoMercadoActualMonedaInstrumento", "DurationMercadoActual", "ConvexidadMercadoActual", "DiferenciaMercado", "TasaCorredora", "FechaTasaCorredora", "MontoATasaCorredora", "MontoATasaCorredoraMonedaInstrumento", "FechaCorteCupon", "NumeroCupon", "IdMonedaPagoCupon", "MontoCorteCupon", "FechaPagoCorteCupon", "MontoCapitalCorteCupon", "MontoInteresCorteCupon", "MontoReajusteCorteCupon", "MontoCorteCuponMonedaInstrumento", "MontoCapitalCorteCuponMonedaInstrumento", "MontoInteresCorteCuponMonedaInstrumento", "MontoReajusteCorteCuponMonedaInstrumento", "IdConcepto", "NombreConcepto", "IdTipoOperacion", "NombreTipoOperacion", "IdTipoProductoTIPOPE", "IdTipoComprobante", "FolioComprobante", "FechaComprobante", "IdMesa", "CodigoIFRS", "IdBolsa", "NombreBolsa", "IdSucursal", "IdAgente", "FolioCarteraRelacionada", "IdEstado", "NombreEstado", "IdTipoProducto", "IdTipoCanalIngreso", "IdMonedaSistema", "MonedaSistema", "UsaGarantiaEspecifica", "IdUsuario", "NombreUsuario", "TamanoContrato", "FechaValorizacionActual", "FechaValorizacionAnterior", "FechaValorizacionCartera", "IdPlaza", "IdFixing", "FechaFixing", "HoraFixing", "IdModalidadCumplimiento", "IdReferenciaMercado", "IdMonedaPrincipal", "IdMonedaSecundaria", "ValorTipoCambioCierre", "ValorParidadCierre", "ValorTipoCambioCosto", "ValorParidadCosto", "MontoMonedaPrincipal", "MontoMonedaSecundaria", "EsPuntos", "ValorTipoCambioSpot", "ValorTipoCambioCotizador", "TasaPuntosMonedaPrincipal", "TasaPuntosMonedaSecundaria", "INGMontoMonedaPrincipal", "INGMontoMonedaSecundaria", "MontoDiferido", "MontoUtilidadDiferida", "MontoPerdidaDiferida", "MontoMargen", "MontoValorRazonableActivo", "MontoValorRazonablePasivo", "MontoValorRazonableNeto", "TasaValorRazonableActivo", "TasaValorRazonablePasivo", "MontoDevengoTipoCambio", "MontoDevengoTipoCambioAnterior", "MontoDiferidoDiario", "MontoDiferidoAcumulado", "MontoMatriz", "ClasificacionRiesgoInstrumento")
VALUES('2022-01-17', 1, 'MBI CORREDORA DE BOLSA', 12740, 2741, '2022-01-03 14:17:49.8330000', '0001-01-01 00:00:00.0000000', 1978, 'RAÚL HERNÁN REYES ILLANES', '10632389-5', 7159, '0', '', 'DV', 'DERIVADOS', 'FWD', 'FORWARDS', 133253, 'SECANO', NULL, '', NULL, '', 'N', 'USD', 'DOLAR', 'OPTIMUS', '0001-01-01', '0001-01-01', 'N', 'COR', '', '2022-01-03', '2022-01-31', 28, 250000.00000000, 0.00000000, 0.0000000000, 0.00000000, 0.00000000, 214022500.00000000, 214022500.00000000, 0.00000000, 0.00000000, 0.00000000, '2022-01-31', 'T0', 'Pagadero hoy', '2022-01-31', '2022-01-03', '0001-01-01', '0001-01-01', 250000.00000000, 0.00000000, 250000.00000000, 0.00000000, 0.00000000, 0.00000000, 0.0000000000, 250000.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 'T0', '2022-01-31', 0.00000000, 0.00000000, '0001-01-01', '', 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 250000.00000000, 0.00000000, '0001-01-01', 0.00000000, 0.00000000, '0001-01-01', 0, NULL, 0.00000000, '0001-01-01', 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 'COMFWD', 'COMPRA FORWARD', 'COMFWD', 'COMPRA FORWARD', 'MEFW', 'CUS', 0, '0001-01-01', 1, '', 'BCS', 'Bolsa de comercio de Santiago', 7, 89, 0, 'VIG', 'VIGENTE', 'MEFW', 'MES', 'CLP', 'PESO CHILENO', 'N', NULL, NULL, 1, '0001-01-01', '0001-01-01', '0001-01-01', NULL, 8, '1900-01-01', '00:00:00', 6, NULL, 'USD', 'CLP', 856.0900000000, 1.00000000, 844.6900000000, 0.00000000, 250000.00000000, 214022500.00000000, 'N', 0.00000000, 0.00000000, 0.00000000, 0.0000000000, 250000.00000000, 214022500.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 215000000.00000000, 214022500.00000000, 977500.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 0.00000000, '');



select * from Mesa_MovimientosFWD mmf 
where DocumentoIdentidadCliente = '76772393-8'
and IdEstado = 'VIG'



select * from FMMovtosForward mmf 
where RutCliente = ' 76772393-8'
and Estado = 'V'

select Estado,count(1) from FMMovtosForward mmf 
group by Estado 


select * from CUENTA_BANCARIA cb where per_id = 47014626


INSERT INTO Custodia_CustodiaFechaOperacion
(IdEmpresa, TipoFechaConsulta, FechaCustodia, IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, ISIN, IdInstrumento, Nemotecnico, IdEmisor, EsFungible, IdCliente, IdPortafolio, DocumentoIdentidad, NombreCliente, CodigoPortafolio, IdBoveda, NumeroCuentaBoveda, Relacion, DescripcionRelacion, CantidadCustodia, PPPCustodia, ValorPPPCustodia, MontoPPPCustodia, PrecioMercadoCustodia, Rentabilidad, ValorMercadoCustodia, MontoMercadoCustodia, DiferenciaMercadoCustodia, IdMonedaReajuste, EsPagaderoEn, CantidadSimulCom, PPPSimulCom, ValorPPPSimulCom, PrecioMercadoSimulCom, ValorMercadoSimulCom, DiferenciaMercadoSimulCom, CantidadSimulVen, PPPSimulVen, ValorPPPSimulVen, PrecioMercadoSimulVen, ValorMercadoSimulVen, DiferenciaMercadoSimulVen, CantidadPrestamo, PPPPrestamo, ValorPPPPrestamo, PrecioMercadoPrestamo, ValorMercadoPrestamo, DiferenciaMercadoPrestamo, CantidadEmpresti, PPPEmpresti, ValorPPPEmpresti, PrecioMercadoEmpresti, ValorMercadoEmpresti, DiferenciaMercadoEmpresti, CantidadGarantia, PPPGarantia, ValorPPPGarantia, PrecioMercadoGarantia, ValorMercadoGarantia, DiferenciaMercadoGarantia, CantidadTransIng, CantidadTransEgr, TotalPosicion, TotalPosicionPresunta, CantidadPactosV, PPPPactosV, ValorPPPPactosV, PrecioMercadoPactosV, ValorMercadoPactosV, DiferenciaMercadoPactosV, CantidadPactosC, PPPPactosC, ValorPPPPactosC, PrecioMercadoPactosC, ValorMercadoPactosC, DiferenciaMercadoPactosC, IdMatrizAgrupador, EsADC, FechaFuentePrecioMercadoCustodia, IdRegimenFondoMutuo)
SELECT IdEmpresa, TipoFechaConsulta, '2025-12-27' as FechaCustodia, IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, ISIN, IdInstrumento, Nemotecnico, IdEmisor, EsFungible, IdCliente, IdPortafolio, DocumentoIdentidad, NombreCliente, CodigoPortafolio, IdBoveda, NumeroCuentaBoveda, Relacion, DescripcionRelacion, CantidadCustodia, PPPCustodia, ValorPPPCustodia, MontoPPPCustodia, PrecioMercadoCustodia, Rentabilidad, ValorMercadoCustodia, MontoMercadoCustodia, DiferenciaMercadoCustodia, IdMonedaReajuste, EsPagaderoEn, CantidadSimulCom, PPPSimulCom, ValorPPPSimulCom, PrecioMercadoSimulCom, ValorMercadoSimulCom, DiferenciaMercadoSimulCom, CantidadSimulVen, PPPSimulVen, ValorPPPSimulVen, PrecioMercadoSimulVen, ValorMercadoSimulVen, DiferenciaMercadoSimulVen, CantidadPrestamo, PPPPrestamo, ValorPPPPrestamo, PrecioMercadoPrestamo, ValorMercadoPrestamo, DiferenciaMercadoPrestamo, CantidadEmpresti, PPPEmpresti, ValorPPPEmpresti, PrecioMercadoEmpresti, ValorMercadoEmpresti, DiferenciaMercadoEmpresti, CantidadGarantia, PPPGarantia, ValorPPPGarantia, PrecioMercadoGarantia, ValorMercadoGarantia, DiferenciaMercadoGarantia, CantidadTransIng, CantidadTransEgr, TotalPosicion, TotalPosicionPresunta, CantidadPactosV, PPPPactosV, ValorPPPPactosV, PrecioMercadoPactosV, ValorMercadoPactosV, DiferenciaMercadoPactosV, CantidadPactosC, PPPPactosC, ValorPPPPactosC, PrecioMercadoPactosC, ValorMercadoPactosC, DiferenciaMercadoPactosC, IdMatrizAgrupador, EsADC, FechaFuentePrecioMercadoCustodia, IdRegimenFondoMutuo
FROM BCSMBI_ONLINE.dbo.Custodia_CustodiaFechaOperacion
WHERE FechaCustodia = '2025-12-26'


INSERT INTO Custodia_CustodiaFechaOperacion
(IdEmpresa, TipoFechaConsulta, FechaCustodia, IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, ISIN, IdInstrumento, Nemotecnico, IdEmisor, EsFungible, IdCliente, IdPortafolio, DocumentoIdentidad, NombreCliente, CodigoPortafolio, IdBoveda, NumeroCuentaBoveda, Relacion, DescripcionRelacion, CantidadCustodia, PPPCustodia, ValorPPPCustodia, MontoPPPCustodia, PrecioMercadoCustodia, Rentabilidad, ValorMercadoCustodia, MontoMercadoCustodia, DiferenciaMercadoCustodia, IdMonedaReajuste, EsPagaderoEn, CantidadSimulCom, PPPSimulCom, ValorPPPSimulCom, PrecioMercadoSimulCom, ValorMercadoSimulCom, DiferenciaMercadoSimulCom, CantidadSimulVen, PPPSimulVen, ValorPPPSimulVen, PrecioMercadoSimulVen, ValorMercadoSimulVen, DiferenciaMercadoSimulVen, CantidadPrestamo, PPPPrestamo, ValorPPPPrestamo, PrecioMercadoPrestamo, ValorMercadoPrestamo, DiferenciaMercadoPrestamo, CantidadEmpresti, PPPEmpresti, ValorPPPEmpresti, PrecioMercadoEmpresti, ValorMercadoEmpresti, DiferenciaMercadoEmpresti, CantidadGarantia, PPPGarantia, ValorPPPGarantia, PrecioMercadoGarantia, ValorMercadoGarantia, DiferenciaMercadoGarantia, CantidadTransIng, CantidadTransEgr, TotalPosicion, TotalPosicionPresunta, CantidadPactosV, PPPPactosV, ValorPPPPactosV, PrecioMercadoPactosV, ValorMercadoPactosV, DiferenciaMercadoPactosV, CantidadPactosC, PPPPactosC, ValorPPPPactosC, PrecioMercadoPactosC, ValorMercadoPactosC, DiferenciaMercadoPactosC, IdMatrizAgrupador, EsADC, FechaFuentePrecioMercadoCustodia, IdRegimenFondoMutuo)
SELECT IdEmpresa, TipoFechaConsulta, '2025-12-28' as FechaCustodia, IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, ISIN, IdInstrumento, Nemotecnico, IdEmisor, EsFungible, IdCliente, IdPortafolio, DocumentoIdentidad, NombreCliente, CodigoPortafolio, IdBoveda, NumeroCuentaBoveda, Relacion, DescripcionRelacion, CantidadCustodia, PPPCustodia, ValorPPPCustodia, MontoPPPCustodia, PrecioMercadoCustodia, Rentabilidad, ValorMercadoCustodia, MontoMercadoCustodia, DiferenciaMercadoCustodia, IdMonedaReajuste, EsPagaderoEn, CantidadSimulCom, PPPSimulCom, ValorPPPSimulCom, PrecioMercadoSimulCom, ValorMercadoSimulCom, DiferenciaMercadoSimulCom, CantidadSimulVen, PPPSimulVen, ValorPPPSimulVen, PrecioMercadoSimulVen, ValorMercadoSimulVen, DiferenciaMercadoSimulVen, CantidadPrestamo, PPPPrestamo, ValorPPPPrestamo, PrecioMercadoPrestamo, ValorMercadoPrestamo, DiferenciaMercadoPrestamo, CantidadEmpresti, PPPEmpresti, ValorPPPEmpresti, PrecioMercadoEmpresti, ValorMercadoEmpresti, DiferenciaMercadoEmpresti, CantidadGarantia, PPPGarantia, ValorPPPGarantia, PrecioMercadoGarantia, ValorMercadoGarantia, DiferenciaMercadoGarantia, CantidadTransIng, CantidadTransEgr, TotalPosicion, TotalPosicionPresunta, CantidadPactosV, PPPPactosV, ValorPPPPactosV, PrecioMercadoPactosV, ValorMercadoPactosV, DiferenciaMercadoPactosV, CantidadPactosC, PPPPactosC, ValorPPPPactosC, PrecioMercadoPactosC, ValorMercadoPactosC, DiferenciaMercadoPactosC, IdMatrizAgrupador, EsADC, FechaFuentePrecioMercadoCustodia, IdRegimenFondoMutuo
FROM BCSMBI_ONLINE.dbo.Custodia_CustodiaFechaOperacion
WHERE FechaCustodia = '2025-12-26'


INSERT INTO CuentaInversion_SaldoFechaEfectiva
(IdEjecutivo, NombreEjecutivo, CodigoSucursal, NombreSucursal, IdCuentaInversion, CodigoCuentaInversion, IdCliente, NombreCliente, DocumentoIdentidad, IdPortafolio, NombrePortafolio, PropositoPortafolio, CodigoPortafolio, FechaSaldo, IdMoneda, CodigoMoneda, NombreMoneda, SaldoDisponible, SaldoEnGarantia, SaldoAPlazo, SaldoRetiro, SaldoRetenido, Producto, IdMatrizAgrupador, EsADC, Checksum)
SELECT IdEjecutivo, NombreEjecutivo, CodigoSucursal, NombreSucursal, IdCuentaInversion, CodigoCuentaInversion, IdCliente, NombreCliente, DocumentoIdentidad, IdPortafolio, NombrePortafolio, PropositoPortafolio, CodigoPortafolio, '2026-02-14' AS FechaSaldo, IdMoneda, CodigoMoneda, NombreMoneda, SaldoDisponible, SaldoEnGarantia, SaldoAPlazo, SaldoRetiro, SaldoRetenido, Producto, IdMatrizAgrupador, EsADC, Checksum
FROM CuentaInversion_SaldoFechaEfectiva
WHERE FechaSaldo = '2026-02-13'

select * from TBTEBACC t 



SELECT 
    r.session_id AS [Session_ID],
    s.login_name AS [Login],
    DB_NAME(r.database_id) AS [Database],
    r.status AS [Status],
    r.command AS [Command],
    r.blocking_session_id AS [Blocking_Session_ID],
    r.wait_type AS [Wait_Type],
    r.wait_time / 1000.0 AS [Wait_Time_Secs],
    r.cpu_time AS [CPU_Time_ms],
    r.logical_reads AS [Logical_Reads],
    t.text AS [Query_Text],
    SUBSTRING(t.text, (r.statement_start_offset/2)+1, 
        ((CASE r.statement_end_offset
          WHEN -1 THEN DATALENGTH(t.text)
         ELSE r.statement_end_offset
         END - r.statement_start_offset)/2) + 1) AS [Statement_Text],
    s.host_name AS [Host_Name],
    s.program_name AS [Program_Name],
    r.start_time AS [Start_Time]
FROM sys.dm_exec_requests r
INNER JOIN sys.dm_exec_sessions s ON r.session_id = s.session_id
CROSS APPLY sys.dm_exec_sql_text(r.sql_handle) t
WHERE s.is_user_process = 1
ORDER BY r.cpu_time DESC, r.blocking_session_id DESC;


select * FROM sys.dm_exec_requests r
INNER JOIN sys.dm_exec_sessions s ON r.session_id = s.session_id



SELECT * FROM Plataforma_Banco pb 
where NombreBanco like '%CONTI%'



select * from Plataforma_ParametroFinanciero  where IdParametroFinanciero = 'UF'



select count(1) from Plataforma_MatrizComercialPortafolios pmcp 

select * from Plataforma_MatrizComercialCorredora pmcp 
where IdPerfilComercialBase = 'MC ARRIESGADO'
and IdTipoProducto = 'INAC'


select * from Plataforma_MatrizComercialPortafolios

select count(1) from CuentaInversion_MovimientosCuentaInversion cimci 


select count(1) from Plataforma_Instrumento pi2 


select * from Plataforma_MatrizComercialCorredora pmcc 

CREATE CLUSTERED INDEX [CIX_TBCTDTCF_TipoCompFac_FolioCompFac_LinCompFac] ON [dbo].[TBCTDTCF] ([TIPO_COMP_FAC] ASC, [FOLIO_COMP_FAC] ASC, [LIN_COMP_FAC] ASC);


SELECT ic.*
FROM sys.tables t
INNER JOIN sys.indexes i ON t.object_id = i.object_id
INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
where i.type_desc  = 'CLUSTERED'
and t.name = 'BANCO'

SELECT 
    QUOTENAME(SCHEMA_NAME(t.schema_id)) + '.' + QUOTENAME(t.name) AS [Tabla],
    i.name AS [IndiceAgrupado],
    -- Alternativa robusta a STRING_AGG usando FOR XML para versiones < 2017
    STUFF((
        SELECT ', ' + c.name
        FROM sys.index_columns ic
        INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id
        ORDER BY ic.key_ordinal
        FOR XML PATH(''), TYPE
    ).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS [ColumnasLlave],
    SUM(p.rows) AS [TotalFilas],
    CAST((SUM(a.total_pages) * 8.0) / 1024.0 AS DECIMAL(18,2)) AS [TamañoTotal_MB],
    CAST((SUM(a.used_pages) * 8.0) / 1024.0 AS DECIMAL(18,2)) AS [TamañoUsado_MB],
    CAST(MAX(dmv.avg_fragmentation_in_percent) AS DECIMAL(5,2)) AS [PorcentajeFragmentacion]
FROM sys.tables t
INNER JOIN sys.indexes i ON t.object_id = i.object_id
INNER JOIN sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id
INNER JOIN sys.allocation_units a ON p.partition_id = a.container_id
LEFT JOIN sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') dmv 
    ON i.object_id = dmv.object_id AND i.index_id = dmv.index_id
WHERE i.type = 1 -- 1 = CLUSTERED
  AND t.is_ms_shipped = 0
GROUP BY 
    t.schema_id, 
    t.name, 
    i.name, 
    i.object_id, 
    i.index_id
ORDER BY 
    [TamañoTotal_MB] DESC;
GO


SELECT 
    QUOTENAME(SCHEMA_NAME(t.schema_id)) + '.' + QUOTENAME(t.name) AS [Tabla],
    i.name AS [NombreIndice],
    i.type_desc AS [TipoIndice],
    -- Concatenación universal robusta para columnas de la llave
    STUFF((
        SELECT ', ' + c.name + CASE WHEN ic.is_descending_key = 1 THEN ' DESC' ELSE ' ASC' END
        FROM sys.index_columns ic
        INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id
        ORDER BY ic.key_ordinal
        FOR XML PATH(''), TYPE
    ).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS [ColumnasLlave],
    SUM(p.rows) AS [TotalFilas],
    CAST((SUM(a.total_pages) * 8.0) / 1024.0 AS DECIMAL(18,2)) AS [TamañoTotal_MB],
    CAST(MAX(dmv.avg_fragmentation_in_percent) AS DECIMAL(5,2)) AS [PorcentajeFragmentacion]
FROM sys.tables t
INNER JOIN sys.indexes i ON t.object_id = i.object_id
INNER JOIN sys.partitions p ON i.object_id = p.object_id AND i.index_id = p.index_id
INNER JOIN sys.allocation_units a ON p.partition_id = a.container_id
LEFT JOIN sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') dmv 
    ON i.object_id = dmv.object_id AND i.index_id = dmv.index_id
WHERE t.name = 'TBPLMAIN' -- Filtro exclusivo para su tabla objetivo
  AND t.is_ms_shipped = 0
GROUP BY 
    t.schema_id, 
    t.name, 
    i.name, 
    i.type_desc,
    i.object_id, 
    i.index_id
ORDER BY 
    i.type_desc ASC, [TamañoTotal_MB] DESC;
GO

alter table tbplmain drop primary key

DROP INDEX IF EXISTS PK_TBPLMAIN
ON dbo.TBPLMAIN;
GO

drop index  PK_TBPLMAIN

CREATE CLUSTERED INDEX [CIX_TBPLMAIN_NEMO] ON [dbo].[TBPLMAIN] ([NEMO] ASC);


select * from sys.TRIGGERS t 
where SUBJECT_TABLE_NAME = 'BSCL.Plataforma.BD::INS.Instrumento'



 
   select estado, * from [BCSOPTMRL].dbo.FMMovtosSpot 
 where FechaOperacion > '20260519' and FechaOperacion <= '20260521' and estado = 'V' order by FechaOperacion desc
 
 
 
 
 
 
 
 
 select * from FMPagos f where id = 
 
 
 
 select * from TBCTMVTO brbvst 
where FEC_MVTO >= '2026-05-19'


select count(1)
from Mesa_MovimientosFWD mmf 
where FechaIngresoRegistro > '2026-01-20'
