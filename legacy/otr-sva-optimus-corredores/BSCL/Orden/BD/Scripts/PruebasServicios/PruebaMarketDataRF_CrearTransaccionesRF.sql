SET SCHEMA "BSCL";

CALL "BSCL.Orden.BD.Servicios::MarketDataRF_CrearTransaccionesRF"
(	
	p_IdEmpresa	=> 1,
	p_IdUsuario	=> 1,
    p_SegundosZonaHoraria => -10800,
  	p_out_InformacionContador => ?,
	p_out_AdvertenciasContador => ?,
	p_out_ErroresContador => ?
);


SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Salida_Resultado";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Salida_InformacionDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Salida_AdvertenciasDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Salida_ErroresDetalle";



select * FROM "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" WHERE "IdTipoOperacionTipoInstrumento" in (346,347)

delete from "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "TipoMensaje" = 'MARKET_DATA_PALO_RF'
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "IdMensaje2" = '161014'
update "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" set "Procesado" = 'N' where "IdMensaje2" = '161003'
SELECT * FROM "RUEBAS"."TEMP_TX_Trades_IRF" where ISSUER = 'CENTRAL' order by TRADEID;
SELECT * FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Entrada_Entrada";
INSERT INTO "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Entrada_Entrada"
select 0 as "IdInterno",* from "RUEBAS"."TEMP_TX_Trades_IRF" where "tradeID" = '161015' UNION ALL
select 1 as "IdInterno",* from "RUEBAS"."TEMP_TX_Trades_IRF" where "tradeID" = '161016';


INSERT INTO "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Entrada_Entrada"
select 0 as "IdInterno",* from "RUEBAS"."TEMP_TX_Trades_IRF" where "tradeID" = '161015' UNION ALL
select 1 as "IdInterno",* from "RUEBAS"."TEMP_TX_Trades_IRF" where "tradeID" = '161016';


delete from "BSCL"."BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Entrada_Entrada"
SELECT i1.*,"Nemotecnico" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" i1
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" i2 on i1."IdInstrumento" = i2."IdInstrumento"
 where i1."IdTipoInstrumento" = 'PAG'
delete from "BSCL.Orden.BD::ORD.Transaccion" where "Folio" = '161001'
delete from "BSCL.Orden.BD::ORD.Transaccion" where  "IdTransaccion" = 70286085

rollback;
COMMIT;

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico"= 'DES015 &08'

SELECT *
			FROM "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesRF_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = 
																					CASE WHEN ent."symbol" LIKE 'CERO%' THEN 'CERO'
																						WHEN ent."symbol" LIKE 'ZERO%' THEN 'ZERO'
																						WHEN ent."symbol" LIKE 'XERO%' THEN 'XERO' ELSE ent."symbol" END
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on emi."IdEmisor" = ins."IdEmisor"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns on tipoIns."IdTipoInstrumento" = ins."IdTipoInstrumento"
			
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = 'BCS' and corrCompraCodigo."CodigoCorredoraFix" = ent."buyExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = 'BCS' and corrVentaCodigo."CodigoCorredoraFix" = ent."sellExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
