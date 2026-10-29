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

MARKET_DATA_PALO_IF;2017051610580056;161052;16-05-2017 10:58:55.13;?;N;?
MARKET_DATA_PALO_IF;2017051610580059;161053;16-05-2017 10:58:57.554;?;N;?
MARKET_DATA_PALO_IF;2017051610590002;161054;16-05-2017 10:59:01.489;?;N;?
MARKET_DATA_PALO_IF;2017051611070031;160012;16-05-2017 11:07:29.61;?;N;?

INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada"
select 1 as "IdInterno",* from "BSCL.Orden.BD::MRF.PALOS_IF" where "TRADEID" = '161052'

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdInstrumento" = 2226

SELECT ent."IdInterno","TRADETIME","TRADEID",ent."TRADEID" as "Folio", 
			TO_TIME(ADD_SECONDS(TO_SECONDDATE("TRADETIME",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)) as "HoraSistemaCreacion",
			TO_NVARCHAR(TO_DATE(ADD_SECONDS(TO_SECONDDATE("TRADETIME",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)),'YYYY-MM-DD') as "FechaCreacion",
			TO_NVARCHAR(TO_DATE(ent."SETTLDATE",'YYYYMMDD')) as "FechaLiquidacion",TO_NVARCHAR(TO_DATE(ent."EXPIRETIME",'YYYYMMDD')) as "FechaVencimiento",
			ent."ISSUER" AS "IdEmisor",CASE LEFT(ent."AUCTIONID",1) WHEN 'H' THEN 'BCSREMATEHOLANDES' WHEN 'T' THEN 'BCSTELERENTA' WHEN 'E' THEN 'BCSREMATEELECTRONICO' ELSE NULL END AS "IdSistemaBursatil",
			1 as "Cantidad",ent."LOCALAMOUNT" as "Monto",0 as "Precio",ent."RESCUEVALUE" as "ValorRescate",
			
			CASE ent."RESETTYPE" WHEN 'NR' THEN 'CLP' WHEN 'UD' THEN 'USD' WHEN 'US' THEN 'USD' WHEN 'IC' THEN 'ICP' WHEN 'UF' THEN 'UF' WHEN 'ES' THEN 'EUR' WHEN 'EU' THEN 'EUR' END AS "IdTipoReajuste",
			CASE ent."RESETTYPE" WHEN 'NR' THEN 'CLP' WHEN 'UD' THEN 'USD' WHEN 'US' THEN 'CLP' WHEN 'IC' THEN 'CLP' WHEN 'UF' THEN 'CLP' WHEN 'ES' THEN 'CLP' WHEN 'EU' THEN 'EUR' END AS "PagaderoEn",
			
			CASE RIGHT(LEFT("TRADECONDITION",2),1) WHEN ' ' THEN 'T2' WHEN 'M' THEN 'T1' WHEN 'H' THEN 'T0' WHEN '3' THEN 'T3' WHEN '5' THEN 'T5' ELSE null END AS "IdCondicionDeLiquidacion",
			ent."RATE"*CASE ent."SIGNRATE" WHEN '+' THEN 1 WHEN '-' THEN -1 ELSE 0 END as "Tasa",ent."TERM" as "Plazo",CASE ent."SYMBOL" WHEN 'PRBC' THEN 'N' WHEN 'PDBC' THEN 'N' ELSE 'S' END AS "EsFungible",
			'BCS' as "IdBolsa",ins."IdInstrumento",ins."IdMoneda" as "IdMonedaLiquidacion",
			CASE RIGHT(LEFT("TRADECONDITION",4),1) WHEN ' ' THEN 'N' WHEN 'V' THEN 'S' ELSE 'N' END AS "EsVentaCorta",
			
			CASE WHEN ent."BUYEXECUTINGFIRM" != ent."BUYENTERINGFIRM" AND RTRIM(ent."BUYENTERINGFIRM") != '' THEN ent."BUYENTERINGFIRM" ELSE ent."TRADERBUY" END AS "CodigoOperadorCompra",
			CASE WHEN ent."SELLEXECUTINGFIRM" != ent."SELLENTERINGFIRM" AND RTRIM(ent."SELLENTERINGFIRM") != '' THEN ent."SELLENTERINGFIRM" ELSE ent."TRADERSELL" END AS "CodigoOperadorVenta",
			
			
			'' as "MarcaNegociacion",'N' AS "EsFueraRueda",ent."DCVTITLE" as "IndicadorDCV",
			corrCompra."IdCorredora" as "IdCorredoraCompra",
			corrVenta."IdCorredora" as "IdCorredoraVenta",
			
			
			0 AS "MejorPrecioCompra",0 as "MejorPrecioVenta",
			ent."BUYEXECUTINGFIRM" AS "CodigoCorredoraCompra",ent."SELLEXECUTINGFIRM" AS "CodigoCorredoraVenta"
			FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = ent."SYMBOL"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns on tipoIns."IdTipoInstrumento" = ins."IdTipoInstrumento"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = 'BCS' and corrCompraCodigo."CodigoCorredoraFix" = ent."BUYEXECUTINGFIRM"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = 'BCS' and corrVentaCodigo."CodigoCorredoraFix" = ent."SELLEXECUTINGFIRM"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
			
			;	
			
			rollback


SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Salida_Resultado";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Salida_InformacionDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Salida_AdvertenciasDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Salida_ErroresDetalle";

IdInterno;IdTransaccion
0;70.286.069
0;70.286.070

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" in (70286069,70286070)

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.CorredoraCodigos"

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "IdMensaje2" = '161041'
update "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" set "Procesado" = 'N' where "IdMensaje2" = '161003'
SELECT * FROM "RUEBAS"."TEMP_TX_TRADES_IIF" where ISSUER = 'CENTRAL' order by TRADEID;
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Entrada_Entrada";
INSERT INTO "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Entrada_Entrada"
select 0 as "IdInterno",* from "RUEBAS"."TEMP_TX_TRADES_IIF" where "TRADEID" = '160001';

SELECT * FROM "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"

IdInstrumento;IdTipoInstrumento;IdTipoReajuste;CorteMinimo;IdMoneda
328.683;PAG;UF;1;CLP
INSERT INTO  "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
select 2223,'PAG','ICP',1,'CLP' from dummy;
INSERT INTO  "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
select 2223,'PAG','IVP',1,'CLP' from dummy;
INSERT INTO  "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
select 2223,'PAG','EUR',1,'CLP' from dummy;

SELECT i1.*,"Nemotecnico" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" i1
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" i2 on i1."IdInstrumento" = i2."IdInstrumento"
 where i1."IdTipoInstrumento" = 'PAG'
delete from "BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" = 70286005
select * from "BSCL.Orden.BD::ORD.Transaccion" where "Folio" = '161004' "IdTransaccion" = 70286007

rollback;
COMMIT;
