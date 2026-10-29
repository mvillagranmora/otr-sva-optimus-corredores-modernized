
WITH transaccionesInhabil AS (
	 SELECT
		 TRA."IdClasificacionTipoInstrumento" 
		,'GESTION' AS "TipoRegistro"
		,TRA."Folio" AS "FolioTransaccion"
		,TRA."IdBolsa" 
		,TRA."IdTipoOperacion" 
		,TIP."AccionOperacion" AS "AccionOrden"
		,TRA."Nemotecnico" 
		,TRA."CodigoOperadorCorredor" 
		,TRA."Cantidad" AS "CantidadTransaccion"
		,SDO."CantidadAsignada" AS "CantidadAsignadaTransaccion"
		,TRA."Monto" 
		,TRA."IdMoneda" AS "IdMonedaTransaccion"
		,TRA."EsFungible"
		,CASE
			WHEN TRA."EsFungible" = 'S' THEN 
				CASE						
					WHEN SDO."ValorRescateAsignado" = 0 THEN 'UI_SIN_MONTO_RESCATE_ASIGNACION'
					WHEN TRA."ValorRescate" > SDO."ValorRescateAsignado" THEN 'UI_FALTA_MONTO_RESCATE_ASIGNACION'
					WHEN TRA."ValorRescate" < SDO."ValorRescateAsignado" THEN 'UI_SOBRA_ASIGNACION'
					WHEN SDO."MontoAsignado" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
					WHEN TRA."Monto" > SDO."MontoAsignado" THEN 'UI_FALTA_MONTO_ASIGNACION'
					WHEN TRA."Monto" < SDO."MontoAsignado" THEN 'UI_SOBRA_MONTO_ASIGNACION'
					ELSE ''
				END
			ELSE
				CASE
					WHEN SDO."CantidadAsignada" = 0 THEN 'UI_SIN_ASIGNACION'
					WHEN TRA."Cantidad" > SDO."CantidadAsignada" THEN 'UI_FALTA_ASIGNACION'
					WHEN TRA."Cantidad" < SDO."CantidadAsignada" THEN 'UI_SOBRA_ASIGNACION'
					WHEN SDO."MontoAsignado" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
					WHEN TRA."Monto"  > SDO."MontoAsignado" THEN 'UI_FALTA_MONTO_ASIGNACION'
					WHEN TRA."Monto"  < SDO."MontoAsignado" THEN 'UI_SOBRA_MONTO_ASIGNACION'
					ELSE ''
				END
		END  AS "Descripcion"
		,TRA."IdTipoInstrumento" 
		,TRA."ValorRescate" AS "ValorRescateTransaccion"
		,SDO."ValorRescateAsignado" AS "ValorRescateDisponibleTransaccion"
		,ASIG."IdTransaccion"  AS "IdTransaccionASIG"	
		,TO_DATE(TRA."FechaOperacion") AS "FechaOperacion" 	
		,DAYOFWEEK(TRA."FechaOperacion") AS "Dia"
	FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
	LEFT  JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASIG ON   TRA."IdTransaccion" = ASIG."IdTransaccion"	
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "SDO" ON  ( "SDO"."IdTransaccion" = TRA."IdTransaccion" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIP ON  ( TIP."IdTipoOperacion" = TRA."IdTipoOperacion" )
	WHERE TRA."IdEstado" != 'ANU'
), TrasaccionesNoAsignadasInhabil AS (
	SELECT 
		 TNA."IdClasificacionTipoInstrumento" 
		,TNA."TipoRegistro"
		,TNA."FolioTransaccion"
		,TNA."IdBolsa" 
		,TNA."AccionOrden"
		,TNA."Nemotecnico" 
		,TNA."CodigoOperadorCorredor" 
		,TNA."CantidadTransaccion"
		,TNA."CantidadAsignadaTransaccion"
		,TNA."Monto" 
		,TNA."IdMonedaTransaccion"
		,TNA."EsFungible"
		,TNA."Descripcion"
		,TNA."IdTipoInstrumento" 
		,TNA."ValorRescateTransaccion"
		,TNA."ValorRescateDisponibleTransaccion"
		,TNA."FechaOperacion" AS "FechaOperacionTransaccion"
	FROM transaccionesInhabil AS  TNA
	WHERE TNA."Dia" IN (1,7)
	AND YEAR(TNA."FechaOperacion") = YEAR(:FechaInicio)
), transaccionesFeriados AS (
	SELECT
		 TRA."IdClasificacionTipoInstrumento" 
		,'GESTION' AS "TipoRegistro"
		,TRA."Folio" AS "FolioTransaccion"
		,TRA."IdBolsa" 
		,TIP."AccionOperacion" AS "AccionOrden"
		,TRA."Nemotecnico" 
		,TRA."CodigoOperadorCorredor" 
		,TRA."Cantidad" AS "CantidadTransaccion"
		,SDO."CantidadAsignada" AS "CantidadAsignadaTransaccion"
		,TRA."Monto" 
		,TRA."IdMoneda" AS "IdMonedaTransaccion"
		,TRA."EsFungible"
		,CASE
			WHEN TRA."EsFungible" = 'S' THEN 
				CASE						
					WHEN SDO."ValorRescateAsignado" = 0 THEN 'UI_SIN_MONTO_RESCATE_ASIGNACION'
					WHEN TRA."ValorRescate" > SDO."ValorRescateAsignado" THEN 'UI_FALTA_MONTO_RESCATE_ASIGNACION'
					WHEN TRA."ValorRescate" < SDO."ValorRescateAsignado" THEN 'UI_SOBRA_ASIGNACION'
					WHEN SDO."MontoAsignado" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
					WHEN TRA."Monto" > SDO."MontoAsignado" THEN 'UI_FALTA_MONTO_ASIGNACION'
					WHEN TRA."Monto" < SDO."MontoAsignado" THEN 'UI_SOBRA_MONTO_ASIGNACION'
					ELSE ''
				END
			ELSE
				CASE
					WHEN SDO."CantidadAsignada" = 0 THEN 'UI_SIN_ASIGNACION'
					WHEN TRA."Cantidad" > SDO."CantidadAsignada" THEN 'UI_FALTA_ASIGNACION'
					WHEN TRA."Cantidad" < SDO."CantidadAsignada" THEN 'UI_SOBRA_ASIGNACION'
					WHEN SDO."MontoAsignado" = 0 THEN 'UI_SIN_MONTO_ASIGNADO'
					WHEN TRA."Monto"  > SDO."MontoAsignado" THEN 'UI_FALTA_MONTO_ASIGNACION'
					WHEN TRA."Monto"  < SDO."MontoAsignado" THEN 'UI_SOBRA_MONTO_ASIGNACION'
					ELSE ''
				END
		END  AS "Descripcion"
		,TRA."IdTipoInstrumento" 
		,TRA."ValorRescate" AS "ValorRescateTransaccion"
		,SDO."ValorRescateAsignado" AS "ValorRescateDisponibleTransaccion"
		,ASIG."IdTransaccion"  AS "IdTransaccionASIG"	
		,TO_DATE(TRA."FechaOperacion") AS "FechaOperacion" 	
		,DAYOFWEEK(TRA."FechaOperacion") AS "Dia"
	FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
	LEFT  JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASIG ON   TRA."IdTransaccion" = ASIG."IdTransaccion"	
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "SDO" ON  ( "SDO"."IdTransaccion" = TRA."IdTransaccion" )	
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIP ON  ( TIP."IdTipoOperacion" = TRA."IdTipoOperacion" )
	WHERE TRA."IdEstado" != 'ANU'
), TrasaccionesNoAsignadasFeriados AS (	
	SELECT 
		 TNA."IdClasificacionTipoInstrumento" 
		,TNA."TipoRegistro"
		,TNA."FolioTransaccion"
		,TNA."IdBolsa" 
		,TNA."AccionOrden"
		,TNA."Nemotecnico" 
		,TNA."CodigoOperadorCorredor" 
		,TNA."CantidadTransaccion"
		,TNA."CantidadAsignadaTransaccion"
		,TNA."Monto" 
		,TNA."IdMonedaTransaccion"
		,TNA."EsFungible"
		,TNA."Descripcion"
		,TNA."IdTipoInstrumento" 
		,TNA."ValorRescateTransaccion"
		,TNA."ValorRescateDisponibleTransaccion"
		,TNA."FechaOperacion" AS "FechaOperacionTransaccion"
	FROM transaccionesFeriados AS TNA
	INNER JOIN "BSCL.Plataforma.BD::REG.DiaFeriado" AS FER ON FER."Fecha" = TNA."FechaOperacion"
	WHERE FER."IdEstado" = 'ACT'
	AND YEAR(FER."Fecha") = YEAR(:FechaInicio)
	AND TNA."IdTransaccionASIG" IS NULL 
)
SELECT 
	 "IdClasificacionTipoInstrumento" 
	,"TipoRegistro"
	,"FolioTransaccion"
	,"IdBolsa" 
	,"AccionOrden"
	,"Nemotecnico" 
	,"CodigoOperadorCorredor" 
	,"CantidadTransaccion"
	,"CantidadAsignadaTransaccion"
	,"Monto" 
	,"IdMonedaTransaccion"
	,"EsFungible"
	,"Descripcion"
	,"IdTipoInstrumento" 
	,"ValorRescateTransaccion"
	,"ValorRescateDisponibleTransaccion"
	,TO_CHAR("FechaOperacionTransaccion") AS "FechaOperacionTransaccion"
FROM TrasaccionesNoAsignadasFeriados
UNION ALL 
SELECT  
	 "IdClasificacionTipoInstrumento" 
	,"TipoRegistro"
	,"FolioTransaccion"
	,"IdBolsa" 
	,"AccionOrden"
	,"Nemotecnico" 
	,"CodigoOperadorCorredor" 
	,"CantidadTransaccion"
	,"CantidadAsignadaTransaccion"
	,"Monto" 
	,"IdMonedaTransaccion"
	,"EsFungible"
	,"Descripcion"
	,"IdTipoInstrumento" 
	,"ValorRescateTransaccion"
	,"ValorRescateDisponibleTransaccion"
	,TO_CHAR("FechaOperacionTransaccion") AS "FechaOperacionTransaccion"
FROM TrasaccionesNoAsignadasInhabil

