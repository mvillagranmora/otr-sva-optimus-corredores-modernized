SELECT 
	CUENTA_PROPIA."Valor" 	AS "CuentaPropiaDCV"
	,RELACIONADO."Valor" 	AS "CuentaRelacionadoDCV"
	,RETENCION."Valor" 		AS "CuentaRetencionDCV"
FROM "BSCL.Custodia.BD::CUS.Configuracion" 		AS CUENTA_PROPIA
,"BSCL.Custodia.BD::CUS.Configuracion" 			AS RELACIONADO
,"BSCL.Custodia.BD::CUS.Configuracion" 			AS RETENCION
WHERE CUENTA_PROPIA."CodigoParametro" = 'CuentaDCVCuentaPropia'AND CUENTA_PROPIA."IdEmpresa" = :IdEmpresa
AND RELACIONADO."CodigoParametro" = 'CuentaDCVCuentaRelacionado' AND RELACIONADO."IdEmpresa" = :IdEmpresa
AND RETENCION."CodigoParametro" = 'CuentaDCVRetencion' AND RELACIONADO."IdEmpresa" = :IdEmpresa;