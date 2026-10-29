SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"Cantidad"							DECIMAL(26,8),
	"EsFungible"						NVARCHAR(1),
	"EsFacturaInmediata"				NVARCHAR(1),
	"EsOD"								NVARCHAR(1),
	"EsVentaCorta"						NVARCHAR(1),
	"EspecificaBolsa"					NVARCHAR(1),
	"FechaEmision"						NVARCHAR(10),
	"FechaOperacion"					NVARCHAR(19),
	"FechaRecepcion"					NVARCHAR(19),
	"FechaVigencia"						NVARCHAR(10),
	"Glosa"								NVARCHAR(520),
	"IdBolsa"							NVARCHAR(3),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
	"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
	"IdEmisor"							NVARCHAR(40),
	"IdFormaCustodia"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"IdInterno"	 						BIGINT,
	"IdMonedaLiquidacionCobros"			NVARCHAR(3),
	"IdMonedaLiquidacionOrden"			NVARCHAR(3),
	"IdPorcentajeVisibilidad"			BIGINT,
	"IdPortafolio"						BIGINT,
	"IdTipoCanalIngreso"				NVARCHAR(3),
	"IdTipoCustodia"					NVARCHAR(3),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdTipoPrecio"						NVARCHAR(1),
	"IdTipoReajuste"					NVARCHAR(3),
	"IdTipoVigencia"					NVARCHAR(2),
	"MontoNeto"							DECIMAL(26,8),
	"PlazoSimultanea"					DECIMAL(26,8),
	"PlazoVentaCorta"					BIGINT,
	"PrecioLimite"						DECIMAL(26,8),
	"PrecioMercado"						DECIMAL(26,8),
	"TasaRFIF"							DECIMAL(26,8),
	"TasaSimultanea"					DECIMAL(26,8),
	"TasaVentaCorta"					DECIMAL(26,8),
	"TipoCambio"						DECIMAL(26,8),
	"ValorRescate"						DECIMAL(26,8),
	"IdTipoLibroOrden"					NVARCHAR(40)
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2		(
										"IdInterno"				BIGINT,
										"IdCobro"				NVARCHAR(6),
										"ValorCobroAplicado"	DECIMAL(26,8)
										);

INSERT INTO #TT ("IdInterno","FechaRecepcion","FechaVigencia","FechaEmision","FechaOperacion","IdTipoVigencia","Glosa","Cantidad","MontoNeto","ValorRescate","IdTipoCustodia","IdFormaCustodia","IdPorcentajeVisibilidad","PrecioLimite","PrecioMercado","EsFacturaInmediata","IdTipoPrecio","IdPortafolio","IdTipoLibroOrden","IdTipoCanalIngreso","IdTipoReajuste","IdEmisor","IdBolsa","EspecificaBolsa","IdMonedaLiquidacionOrden","IdMonedaLiquidacionCobros","TipoCambio","IdInstrumento","IdInstrumentoSubyacente","IdTipoOperacion","IdCondicionDeLiquidacionContado","IdCondicionDeLiquidacionPlazo","TasaVentaCorta","TasaSimultanea","TasaRFIF","PlazoSimultanea","PlazoVentaCorta","EsVentaCorta","EsOD","EsFungible")
SELECT '0','2016-07-14 15:02:33','0001-01-01','01-01-0001','2016-07-14 01:53:19','SH','',5000000,'0','0','DCV','CUS','1','100','100','N','L',10000,'NORMAL','TRA','CLP','BTP','BCS','S','CLP','CLP','1',103946,'0','COMRF','T1','T0','0','0','1','0','0','N','N','N' from dummy;


--INSERT INTO #TT ("IdInterno","FechaRecepcion","FechaVigencia","FechaEmision","FechaOperacion","IdTipoVigencia","Glosa","Cantidad","MontoNeto","ValorRescate","IdTipoCustodia","IdFormaCustodia","IdPorcentajeVisibilidad","PrecioLimite","PrecioMercado","EsFacturaInmediata","IdTipoPrecio","IdPortafolio","IdTipoLibroOrden","IdTipoCanalIngreso","IdTipoReajuste","IdEmisor","IdBolsa","EspecificaBolsa","IdMonedaLiquidacionOrden","IdMonedaLiquidacionCobros","TipoCambio","IdInstrumento","IdInstrumentoSubyacente","IdTipoOperacion","IdCondicionDeLiquidacionContado","IdCondicionDeLiquidacionPlazo","TasaVentaCorta","TasaSimultanea","TasaRFIF","PlazoSimultanea","PlazoVentaCorta","EsVentaCorta","EsOD","EsFungible")
--SELECT TOP 1 "IdOrden",'2016-06-20 14:13:41','0001-01-01','0001-01-01','2016-06-20 02:09:54','SH','','2155550000','0','0','DCV','CUS','1','2240','2240','N','L','343710','NORMAL','TRA','CLP','','BCS','S','CLP','CLP','1','76','0','COMACN','T0','T0','0','0','0','0','0','N','N','N' 
--from "BSCL"."BSCL.Orden.BD::ORD.Orden";

--SET 'DEPURACION' = 'S';
CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearOrdenMM"(1,3,'ORD','N','S',#tt,#tt2,?,?,?,?,?,?);

--select * from "BSCL.Plataforma.BD::PLA.Depuracion" order by "Fecha" desc
