SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno"	 						BIGINT,
	"FolioTransaccion"					NVARCHAR(40),
	"FechaCreacion"						NVARCHAR(10),
	"HoraCreacion"						NVARCHAR(10),
	"FechaEmision"						NVARCHAR(10),
	"Plazo"								BIGINT,
	"EsCuentaPropia"					NVARCHAR(1),
	"EsFungible"						NVARCHAR(1),
	"EsVentaCorta"						NVARCHAR(1),
	"CodigoOperadorContraparte"			NVARCHAR(40),
	"CodigoOperadorCorredor"			NVARCHAR(40),
	"IdSistemaBursatil"					NVARCHAR(40),
	"IdEmisor"							NVARCHAR(40),
	"Cantidad"							DECIMAL(26,8),	--NOMINALES EN RF, CORTES EN IF, 
	"ValorCaptacion"					DECIMAL(26,8),	--ValorCaptacionES EN RF, CORTES EN IF, Solo es informativo
	"Monto"								DECIMAL(26,8),
	"MontoValorizado"					DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	"Tasa"								DECIMAL(26,8), --SIMULTANEA, RENTA FIJA, IF, FUTURO BCU
	"MontoPlazo"						DECIMAL(26,8),
	"PrecioPlazo"						DECIMAL(26,8),
	"ValorRescate"						DECIMAL(26,8),
	"IdTipoReajuste"					NVARCHAR(3),
	"PagaderoEn"						NVARCHAR(3),
	"IdEmpleado"						BIGINT,
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
	"FechaLiquidacionContado"			NVARCHAR(10),
	"FechaVencimiento"					NVARCHAR(10),
	"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
	"FechaLiquidacionPlazo"				NVARCHAR(10),
	"IdInstrumento"						BIGINT,
	"IdCorredorContraparte"				BIGINT,
	"IdBolsa"							NVARCHAR(3),
	"IdLibroOrdenes"					BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"EsFueraRueda"						NVARCHAR(1),
	"IndicadorDCV"						NVARCHAR(1),										
	"EsBilateral"						NVARCHAR(1),  -- indicador bilateral
	"MarcaNegociacion"					NVARCHAR(40),
	"TipoCambioCorresponsal"			DECIMAL(26,8),
	"IdCorredorCorresponsal"			BIGINT,
	"MontoLiquidacionCorresponsal"		DECIMAL(26,8),
	"IdMonedaLiquidacionCorresponsal"	NVARCHAR(3),
	"FolioTransaccionPlazo"				NVARCHAR(40),
	"MejorPrecioCompra"					DECIMAL(26,8),
	"MejorPrecioVenta"					DECIMAL(26,8),
	"NemotecnicoEspecificoBolson" 		NVARCHAR(100));



INSERT INTO #TT ("IdInterno","FolioTransaccion","FechaCreacion","HoraCreacion","FechaEmision","Plazo","EsCuentaPropia","EsFungible","EsVentaCorta","CodigoOperadorContraparte","CodigoOperadorCorredor","IdSistemaBursatil","IdEmisor","Cantidad","Monto","MontoValorizado","Precio","Tasa","MontoPlazo","PrecioPlazo","ValorRescate","ValorCaptacion","IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacionContado","FechaLiquidacionContado","FechaLiquidacionPlazo","FechaVencimiento","IdCondicionDeLiquidacionPlazo","IdCorredorContraparte","IdBolsa","IdLibroOrdenes","MarcaNegociacion","MejorPrecioCompra","MejorPrecioVenta","PagaderoEn","TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","EsFueraRueda","IndicadorDCV","EsBilateral","FolioTransaccionPlazo","NemotecnicoEspecificoBolson","IdInstrumento","IdInstrumentoSubyacente")
SELECT 0 as "IdInterno",505 as "FolioTransaccion",'2017-06-05' as "FechaCreacion",'19:11:13' as "HoraCreacion",'2017-06-05' as "FechaEmision",30 as "Plazo",'N' as "EsCuentaPropia",'N' as "EsFungible",'N' as "EsVentaCorta",'' as "CodigoOperadorContraparte",'001' as "CodigoOperadorCorredor",'MANUAL' as "IdSistemaBursatil",'COPEC' as "IdEmisor",1000 as "Cantidad",7465000 as "Monto",7465000 as "MontoValorizado",7465 as "Precio",1 as "Tasa",7539650 as "MontoPlazo",7539.65 as "PrecioPlazo",0 as "ValorRescate",0 as "ValorCaptacion",'CLP' as "IdTipoReajuste",7293 as "IdEmpleado",'COMRRV' as "IdTipoOperacion",'T1' as "IdCondicionDeLiquidacionContado",'2017-06-06' as "FechaLiquidacionContado",'2017-07-06' as "FechaLiquidacionPlazo",'2017-07-06' as "FechaVencimiento",'TP' as "IdCondicionDeLiquidacionPlazo",2001 as "IdCorredorContraparte",'BCS' as "IdBolsa",2 as "IdLibroOrdenes",'' as "MarcaNegociacion",7465 as "MejorPrecioCompra",7465 as "MejorPrecioVenta",'CLP' as "PagaderoEn",0 as "TipoCambioCorresponsal",0 as "IdCorredorCorresponsal",0 as "MontoLiquidacionCorresponsal",'' as "IdMonedaLiquidacionCorresponsal",'S' as "EsFueraRueda",'N' as "IndicadorDCV",'N' as "EsBilateral",506 as "FolioTransaccionPlazo",'COPEC' as "NemotecnicoEspecificoBolson",2250 as "IdInstrumento",502 as "IdInstrumentoSubyacente" from dummy;

CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearTransacciones"(	1,4,'ORD','N','N',#tt,?,?,?,?,?,?,?,?);

rollback


select * from "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento" where "IdTipoOperacion" = 'COMRRV'



