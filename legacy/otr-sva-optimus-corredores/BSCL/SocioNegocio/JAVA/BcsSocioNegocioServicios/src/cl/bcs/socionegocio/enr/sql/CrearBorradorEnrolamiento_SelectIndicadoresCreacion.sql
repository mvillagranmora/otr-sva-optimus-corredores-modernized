SELECT
TIPO_INDICADOR."IdTipoEntidad"
,TIPO_INDICADOR."ValorIndicador" AS "ValorIndicadorDefecto"
,TIPO_INDICADOR."IdTipoIndicador"
,TIPO_INDICADOR."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" AS TIPO_INDICADOR
WHERE TIPO_INDICADOR."IdTipoEntidad" IN ('PERSONA')
AND "IdEstado" != 'INA'
AND TIPO_INDICADOR."IdEntidad" = 0
AND TIPO_INDICADOR."IdTipoCliente" = :IdTipoCliente
AND NOT EXISTS (SELECT 1 FROM "BSCL.SocioNegocio.BD::ENR.Indicador" AS INDICADOR WHERE INDICADOR."IdEnrolamiento" = :IdEnrolamiento AND INDICADOR."IdTipoIndicador" = TIPO_INDICADOR."IdTipoIndicador")
UNION ALL
SELECT
TIPO_INDICADOR."IdTipoEntidad"
,CASE TIPO_INDICADOR."IdTipoIndicador" WHEN ENROL."IdTipoIndicador" THEN CAST (ENROL."CLI_IdTipoClienteCMF" AS VARCHAR) ELSE TIPO_INDICADOR."ValorIndicador" END  AS "ValorIndicadorDefecto"
,TIPO_INDICADOR."IdTipoIndicador"
,TIPO_INDICADOR."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" AS TIPO_INDICADOR
LEFT JOIN
	(
		SELECT TIND."IdTipoIndicador",IND."CLI_IdTipoClienteCMF"
		FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIND
		, "BSCL.SocioNegocio.BD::ENR.Enrolamiento" IND 
		WHERE  TIND."CodigoTipoIndicador" IN ('TCLICMF','TCLICMFJ')
				AND ind."IdEnrolamiento" = :IdEnrolamiento
			
	)ENROL ON (ENROL."IdTipoIndicador"=TIPO_INDICADOR."IdTipoIndicador") 
WHERE TIPO_INDICADOR."IdTipoEntidad" IN ('CLIENTE')
AND "IdEstado" != 'INA'
AND TIPO_INDICADOR."IdEntidad" = 0
AND TIPO_INDICADOR."IdTipoCliente" = :IdTipoCliente
AND NOT EXISTS (SELECT 1 FROM "BSCL.SocioNegocio.BD::ENR.Indicador" AS INDICADOR WHERE INDICADOR."IdEnrolamiento" = :IdEnrolamiento AND INDICADOR."IdTipoIndicador" = TIPO_INDICADOR."IdTipoIndicador")
UNION ALL
SELECT
TIPO_INDICADOR."IdTipoEntidad"
,TIPO_INDICADOR."ValorIndicador" AS "ValorIndicadorDefecto"
,TIPO_INDICADOR."IdTipoIndicador"
,TIPO_INDICADOR."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" AS TIPO_INDICADOR
WHERE TIPO_INDICADOR."IdTipoEntidad" = 'PORTAFOLIO'
AND TIPO_INDICADOR."CodigoTipoIndicador" IN ('ADMCART','ORDMODALIDAD','GEN_OCCINV')
AND TIPO_INDICADOR."IdEntidad" = 0
AND "IdEstado" != 'INA'
AND TIPO_INDICADOR."IdTipoCliente" = :IdTipoCliente
AND NOT EXISTS (SELECT 1 FROM "BSCL.SocioNegocio.BD::ENR.Indicador" AS INDICADOR WHERE INDICADOR."IdEnrolamiento" = :IdEnrolamiento AND INDICADOR."IdTipoIndicador" = TIPO_INDICADOR."IdTipoIndicador");