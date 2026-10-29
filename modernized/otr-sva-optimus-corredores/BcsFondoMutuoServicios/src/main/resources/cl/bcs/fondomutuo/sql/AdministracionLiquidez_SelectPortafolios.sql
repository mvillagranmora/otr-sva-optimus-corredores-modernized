SELECT "PORTAFOLIO"."IdPortafolio"
,"PORTAFOLIO"."IdCliente"
,"CONFIGURACION"."IdSerieFondo"
,"CONFIGURACION"."MontoMinimo"
,"CONFIGURACION"."IdMoneda"
FROM (
	SELECT "PORT"."IdPortafolio","PORT"."IdCliente"
	FROM  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "PORT"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS "INDICADOR" 
	ON ("INDICADOR"."IdCliente" = "PORT"."IdCliente" 
	AND "INDICADOR"."CodigoTipoIndicador" = 'CNTADMLIQ' 
	AND "INDICADOR"."ValorIndicador" = 'S')
	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS "INDICADOR_PORTAFOLIO" 
	ON ("INDICADOR_PORTAFOLIO"."IdPortafolio" = "PORT"."IdPortafolio" 
	AND "INDICADOR_PORTAFOLIO"."CodigoTipoIndicador" = 'FFMM_MMAUT' 
	AND "INDICADOR_PORTAFOLIO"."ValorIndicador" = 'S')
) AS "PORTAFOLIO",
(
	SELECT CONFIGURACION."IdEmpresa"
	,CONFIGURACION."IdMoneda"
	,CONFIGURACION."IdSerieFondo"
	,CONFIGURACION."MontoMinimo"
	FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionFondoAdministracionLiquidez" CONFIGURACION
	WHERE (:HoraSistema >= "HoraCierreLiquidezComercial" and ABS(seconds_between(TO_TIME(:HoraSistema) ,TO_TIME("HoraCierreLiquidezComercial"))) < 900)
	AND NOT EXISTS (
		SELECT 1 FROM "BSCL.FondoMutuo.BD::FMM.EjecucionAdministacionLiquidez" EJECUCION 
		WHERE CONFIGURACION."IdEmpresa" = EJECUCION."IdEmpresa" 
		AND CONFIGURACION."IdMoneda" = EJECUCION."IdMoneda" 
		AND EJECUCION."FechaEjecucion" = :FechaProceso
	)
) AS "CONFIGURACION"
WHERE COALESCE("CONFIGURACION"."IdSerieFondo",'') != '' 
AND COALESCE("CONFIGURACION"."MontoMinimo",0) >= 0;