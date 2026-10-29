SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdAsignacion" 		BIGINT,
		"IdInterno"			BIGINT,
		"Cantidad"			DECIMAL(26,8),
		"ValorRescate"		DECIMAL(26,8),
		"IdOrden" 			BIGINT,
		"IdTransaccion"		BIGINT,
		"IdEstadoModulo"	NVARCHAR(1),
		"NombreModulo"		NVARCHAR(100)
);

INSERT INTO #TT ("IdAsignacion","IdInterno","Cantidad","ValorRescate","IdOrden","IdTransaccion","IdEstadoModulo","NombreModulo")
SELECT 3726073,3726073,110,0,309768,70285521,'A','ORDENES' from dummy;

INSERT INTO #TT ("IdAsignacion","IdInterno","Cantidad","ValorRescate","IdOrden","IdTransaccion","IdEstadoModulo","NombreModulo")
SELECT 3726074,3726074,110,0,309768,70285522,'A','ORDENES' from dummy;



drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2 (
		"IdOrden" 					BIGINT,
		"IdOrdenReal" 				BIGINT,
		"IdTipoOperacion" 			NVARCHAR(6),
		"IdTipoPrecio"			NVARCHAR(1),
		"EsVentaCorta"				NVARCHAR(1),
		"PrecioOrden"				DECIMAL(26,8),
		"Tamano"					DECIMAL(26,8),
		"IdPortafolio"				BIGINT,
		"IdInstrumento" 			BIGINT,
		"IdTipoInstrumento"			NVARCHAR(3),
		"IdClasificacionTipoInstrumento"	NVARCHAR(2),
		"IdInstrumentoSubyacente" 	BIGINT,
		"IdCondicionDeLiquidacion" 	NVARCHAR(2),
		"IdBolsa"					NVARCHAR(3),
		"FechaOperacionOrden"		SECONDDATE,
		"IdEstadoOrden"				NVARCHAR(3),
		"IdLibroOrdenes"			BIGINT,
		"AccionTipoOperacion"		NVARCHAR(10),
		"IdTipoCanalIngreso"		NVARCHAR(3)
);

INSERT INTO #TT2 ("IdOrden","IdOrdenReal","IdTipoOperacion","IdTipoPrecio","EsVentaCorta","PrecioOrden","Tamano","IdPortafolio","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdInstrumentoSubyacente","IdCondicionDeLiquidacion","IdBolsa","FechaOperacionOrden","IdEstadoOrden","IdLibroOrdenes","AccionTipoOperacion","IdTipoCanalIngreso")
SELECT 309768,309768,'COMSIM','L','N',1645,'1',343710,2248,'SIM','RV',1006,'T0','BCS','2016-11-07 16:54:55','ANU','2','COMPRA','TRA' from dummy;



drop TABLE #TT3;
CREATE LOCAL TEMPORARY TABLE #TT3 (
		"IdTransaccion" 			BIGINT,
		"IdTransaccionReal" 		BIGINT,
		"IdTipoOperacion" 			NVARCHAR(6),
		"IdInstrumento" 			BIGINT,
		"IdLibroOrdenes"			BIGINT,
		"IdCondicionDeLiquidacion" 	NVARCHAR(2),
		"IdBolsa"					NVARCHAR(3),
		"FechaOperacionTransaccion"	SECONDDATE,
		"PrecioTransaccion"			DECIMAL(26,8),
		"EsVentaCorta"				NVARCHAR(1),
		"IdEstadoTransaccion"		NVARCHAR(3),
		"EsFungible"				NVARCHAR(1),
		"IdInstrumentoSubyacente" 	BIGINT,
		"TamanoCorte"				DECIMAL(26,8),
		"AccionTipoOperacion"		NVARCHAR(10),
		"EsAPlazo"					NVARCHAR(1)
);

INSERT INTO #TT3 ("IdTransaccion","IdTransaccionReal","IdTipoOperacion","IdInstrumento","IdLibroOrdenes","IdCondicionDeLiquidacion","IdBolsa","FechaOperacionTransaccion","PrecioTransaccion","EsVentaCorta","IdEstadoTransaccion","EsFungible","IdInstrumentoSubyacente","TamanoCorte","AccionTipoOperacion","EsAPlazo")
SELECT 70285521,70285521,'COMSIM',2248,2,'T0','BCS','2016-11-07 16:54:55',1645,'N','ING','N',1006,null,'COMPRA','N' from dummy;

INSERT INTO #TT3 ("IdTransaccion","IdTransaccionReal","IdTipoOperacion","IdInstrumento","IdLibroOrdenes","IdCondicionDeLiquidacion","IdBolsa","FechaOperacionTransaccion","PrecioTransaccion","EsVentaCorta","IdEstadoTransaccion","EsFungible","IdInstrumentoSubyacente","TamanoCorte","AccionTipoOperacion","EsAPlazo")
SELECT 70285522,70285522,'VENSIM',2248,2,'T0','BCS','2016-11-07 16:54:55',1645,'N','ING','N',1006,null,'VENTA','S' from dummy;


CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearAsignacionOptimizado"(
	p_IdEmpresa             => 1,
	p_Asignaciones			=> #TT,	
	p_Ordenes				=> #TT2,	
	p_Transacciones			=> #TT3,
	p_out_SalidaDetalle		=> ?,
	p_out_Salida        	=> ?


) 	