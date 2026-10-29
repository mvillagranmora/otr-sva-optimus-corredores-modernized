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

TipoMensaje;IdMensaje1;IdMensaje2;FechaIngresoRegistro;FechaProcesamiento;Procesado;Log
MARKET_DATA_PALO_RV;2017050815400025;210001;08-05-2017 12:45:38.527;?;N;?
MARKET_DATA_PALO_RV;2017050815400023;210002;08-05-2017 12:45:36.964;?;N;?
MARKET_DATA_PALO_RV;2017050815400022;210003;08-05-2017 12:45:35.395;?;N;?
MARKET_DATA_PALO_RV;2017050815400021;200001;08-05-2017 12:45:34.342;?;N;?
MARKET_DATA_PALO_RV;2017050815400020;200002;08-05-2017 12:45:33.277;?;N;?

rollback
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" where "IdMensaje2" = '225011';
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle";
SELECT * FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle";

INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada"
select 0,* from "BSCL"."BSCL.Orden.BD::MRV.PALOS_RV" where "tradeID" = 210002

INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada"
select 0,* from "BSCL"."BSCL.Orden.BD::MRF.PALOS_RF" where "tradeID" = 160001

TipoMensaje;IdMensaje1;IdMensaje2;FechaIngresoRegistro;FechaProcesamiento;Procesado;Log
MARKET_DATA_PALO_RF;2017040511420002;160001;05-04-2017 11:46:39.43;?;N;?

rollback;
commit;
delete from "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" = 29278
SELECT * from "BSCL"."BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" = 29278

update "BSCL.Orden.BD::ORD.MarketDataRF_CrearTransaccionesIF_Entrada_Entrada" set "BUYEXECUTINGFIRM" = '066'
rollback;
COMMIT;
