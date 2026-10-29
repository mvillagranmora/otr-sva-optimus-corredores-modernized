SET SCHEMA "BSCL";


INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes" ("IdInterno","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdCorredorCorresponsal","IdInstrumento","DiasPlazo","CantidadOrden","MontoOrden","ValorRescateOrden","EsFungible","TasaSimultanea","TasaRFIF","TasaVentaCorta","IdMonedaLiquidacionOrden","PrecioOrden","TipoCambio","MontoTotal","EsPorMonto")
SELECT 0 as "IdInterno",166130 as "IdPortafolio",'INCX' as "IdTipoProducto",'COMCFX' as "IdTipoOperacion",'AGE' as "IdTipoCanalIngreso",'BCS' as "IdBolsa",null as "IdCorredorCorresponsal",
2612 as "IdInstrumento",'0' as "DiasPlazo",12 as "CantidadOrden",0 as "MontoOrden",0 as "ValorRescateOrden",'N' as "EsFungible",0 as "TasaSimultanea",0 as "TasaRFIF",0 as "TasaVentaCorta",
'CLP' as "IdMonedaLiquidacionOrden",360 as "PrecioOrden",null as "TipoCambio",0 as "MontoTotal",'N' as "EsPorMonto" from dummy;


CALL "BSCL.Orden.BD.Servicios::CalcularMontoYCobrosOrden" (
	p_IdEmpresa 				=> 1,
	p_IdUsuario 				=> 4,
	p_IdModulo 					=> 'ORD',
	p_out_AdvertenciasContador	=> ?,
	p_out_ErroresContador		=> ?
);

SELECT * FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes";


rollback;
commit

delete from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 391
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"  ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") VALUES ( 391,'BSCL.Orden.BD.Servicios::CalcularMontoYCobrosOrden',0,'P','','p_Ordenes=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes;p_CobrosModificados=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_CobrosModificados','P_OUT_ORDENES=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes;P_OUT_COBROS=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros;P_OUT_ADVERTENCIASDETALLE=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle'); 
   
