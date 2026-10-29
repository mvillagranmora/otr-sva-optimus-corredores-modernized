SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno" 						BIGINT,
		"IdTransaccion"						BIGINT,
		"FolioTransaccion" 					NVARCHAR(40),
		"FolioTransaccionPlazo"				NVARCHAR(40),
		"IdClasificacionTipoInstrumento"	NVARCHAR(3),
		"IdTipoInstrumento"					NVARCHAR(3),
		"Cantidad"							DECIMAL(26,8),
		"CorteMinimo"						DECIMAL(26,8),
		"Plazo"								DECIMAL(26,8),
		"IdPaisBolsa"						BIGINT,
		"IdPaisSistema"						BIGINT,
		"IdCorredorCorresponsal"			BIGINT,
		"IdEstadoModulo"					NVARCHAR(3),
		"NombreModulo"						NVARCHAR(100),
		"ExisteFolioContado"				NVARCHAR(1),
		"ExisteFolioPlazo"					NVARCHAR(1)
);

INSERT INTO #TT ("IdInterno","IdTransaccion","FolioTransaccion","FolioTransaccionPlazo","IdClasificacionTipoInstrumento","IdTipoInstrumento","Cantidad","CorteMinimo"
,"Plazo","IdPaisBolsa","IdPaisSistema","IdCorredorCorresponsal","IdEstadoModulo","NombreModulo","ExisteFolioContado","ExisteFolioPlazo")
SELECT 0,70285530,'120','0','RV','ACN',1000,0,90,44,44,0,'A','ORDENES','S','S' from dummy;



CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearTransaccionMM"(
	p_Transacciones		=> #TT,
	p_out_SalidaDetalle	=> ?,
	p_out_Salida		=> ?); 	
