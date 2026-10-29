SELECT
INDICADOR."IdIndicador"
FROM "BSCL.SocioNegocio.BD::ENR.Indicador" AS INDICADOR
WHERE INDICADOR."IdEnrolamiento" = :IdEnrolamiento
AND 
(
	:EsCambioTipoCliente = 'S'
	OR
	NOT EXISTS (
		SELECT 1 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresModelo" AS TIPO_INDICADOR 
		WHERE 
		INDICADOR."IdTipoIndicador" = TIPO_INDICADOR."IdTipoIndicador" 
		AND "IdEstado" != 'INA' 
		AND TIPO_INDICADOR."IdEntidad" = 0
		AND TIPO_INDICADOR."IdTipoCliente" = :IdTipoCliente
	)
)
