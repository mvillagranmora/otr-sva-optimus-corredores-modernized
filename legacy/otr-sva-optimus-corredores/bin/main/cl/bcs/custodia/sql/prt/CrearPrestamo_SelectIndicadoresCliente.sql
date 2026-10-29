-- Obtiene valor del indicador de Contrato de PRESTAMOS (Quien solicita el prestamos
WITH 
TipoPrestamoCodigoTipoIndicador AS (
	SELECT "IdTipoPrestamo",
			CASE WHEN "IdTipoPrestamo" IN ('PRTVTC', 'PRVCRF') THEN 'CVC'
				 ELSE 'CONPRESADM'
			END AS "CodigoTipoIndicador"
	FROM "BSCL.Custodia.BD::PRT.TipoPrestamo"
)
SELECT 
	 TPCTP."CodigoTipoIndicador"
	,IND."NombreTipoIndicador"
	,COALESCE(IND."ValorIndicador", 'N') AS "ValorIndicador"	
	,CASE 
		WHEN COALESCE(IND."ValorIndicador", 'N') = 'N' AND TPCTP."CodigoTipoIndicador" = 'CVC' THEN 'CLIENTE_SIN_CONTRATO_VENTA_CORTA'
		WHEN COALESCE(IND."ValorIndicador", 'N') = 'S' AND TPCTP."CodigoTipoIndicador" = 'CVC' THEN 'CLIENTE_CON_CONTRATO_VENTA_CORTA'
		WHEN COALESCE(IND."ValorIndicador", 'N') = 'N' AND TPCTP."CodigoTipoIndicador" = 'CONPRESADM' THEN 'CLIENTE_SIN_CONTRATO_ADMINISTRATIVO_OTROS'
		WHEN COALESCE(IND."ValorIndicador", 'N') = 'S' AND TPCTP."CodigoTipoIndicador" = 'CONPRESADM' THEN 'CLIENTE_CON_CONTRATO_ADMINISTRATIVO_OTROS'
	END AS "MensajeIndicador"
FROM TipoPrestamoCodigoTipoIndicador AS TPCTP 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IND ON TPCTP."CodigoTipoIndicador" = IND."CodigoTipoIndicador" 
			AND IND."IdCliente" = :IdCliente AND IND."ValorIndicador" = 'S'
WHERE TPCTP."IdTipoPrestamo" = :IdTipoPrestamo