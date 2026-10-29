SET SCHEMA "BSCL";

CALL "BSCL.Orden.BD.Servicios::MarketData_CrearTransacciones"
(	
	p_IdEmpresa	=> 1,
	p_IdUsuario	=> 0,
    p_SegundosZonaHoraria => -10800,
  	p_out_InformacionContador => ?,
	p_out_AdvertenciasContador => ?,
	p_out_ErroresContador => ?
);



INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada"
select 1 as "IdInterno",* from "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" where "folio" = '45509'
UNION ALL
INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada"
select 2 as "IdInterno",* from "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" where "folio" = '45507'
UNION ALL
INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada"
select 3 as "IdInterno",* from "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" where "folio" = '45503'



UNION ALL
INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada"
select 4 as "IdInterno",* from "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS" where "folio" = '45511'

 
MARKET_DATA_PALO_RV;2017042117390031;210006;21-04-2017 14:39:27.987;?;N;?

INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada"
select 0 as "IdInterno",* from "BSCL.Orden.BD::MRV.PALOS_RV" where "tradeID" = '210006'


delete FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "Folio" = '210009' and "IdEmisor" = 'CMPC'
rollback
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle";
commit


drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle";
drop table "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle";



select * from "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" in (233062,233063)

IdInterno;IdTransaccion
0;233.062
0;233.063

ROLLBACK
SELECT * FROM "BSCL.Orden.BD::MRV.PALOS_RV"
SELECT * FROM "BSCL.Orden.BD::MRV.INDICES_RV"
SELECT * FROM "BSCL.Orden.BD::ORD.ControlMercado" where "TipoMensaje" = 'MARKET_DATA_PALO_RV' and "Procesado" = 'N'

select * FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" WHERE "IdTipoOperacionTipoInstrumento" in (346,347)

delete from "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "TipoMensaje" = 'MARKET_DATA_PALO_RF'
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "IdMensaje2" = '200009'
SELECT * FROM "RUEBAS"."MDC_TX_PALOS_RV"
SELECT * FROM "RUEBAS"."TEMP_TX_Trades_IRF"

SELECT * FROM "BSCL"."BSCL.


update "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" set "Procesado" = 'N' where "IdMensaje2" = '161003'
SELECT * FROM "RUEBAS"."TEMP_TX_Trades_IRF" where ISSUER = 'CENTRAL' order by TRADEID;
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRV_CrearTransaccionesRV_Entrada_Entrada";
INSERT INTO "BSCL.Orden.BD::ORD.MarketDataRV_CrearTransaccionesRV_Entrada_Entrada"
select 0 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '200001' UNION ALL
select 1 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '210001' UNION ALL
select 2 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '210002' UNION ALL
select 3 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '210003' UNION ALL
select 4 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '210004' UNION ALL
select 5 as "IdInterno",* from "RUEBAS"."MDC_TX_PALOS_RV" where "tradeID" = '210005';



SELECT i1.*,"Nemotecnico" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" i1
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" i2 on i1."IdInstrumento" = i2."IdInstrumento"
 where i1."IdTipoInstrumento" = 'PAG'
delete from "BSCL.Orden.BD::ORD.Transaccion" where "Folio" = '161001'
delete from "BSCL.Orden.BD::ORD.Transaccion" where  "IdTransaccion" = 70286085

 from "BSCL.Orden.BD::ORD.MarketDataRV_CrearTransaccionesRV_Entrada_Entrada"


rollback;

COMMIT;
