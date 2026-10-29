SELECT 	CCP."IdPortafolio", 
		CCP."Codigo", 
		CCP."IdTipoCuentaCustodia", 
		CCP."Codigo", 
		CCP."LiquidaDividendos",
		CCP."IdBoveda"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" as CCP
WHERE CCP."IdPortafolio" IN (:IdPortafolio)
  AND CCP."IdEstadoAutorizacion" = 'AUT'
  AND IFNULL(CCP."IdEstado",'') = (	CASE 
										WHEN :localizacionSistema = 'PE' THEN 'ACT'
										ELSE IFNULL(CCP."IdEstado",'')
									END)