SELECT DISTINCT
	 DIS."IdDistribucionInversion"
	,DIS."IdContratoSAAPV"
	,COALESCE(INS."IdTipoInstrumento",'') AS "IdTipoInstrumento"
	,DIS."IdInstrumento"
	,DIS."Nemotecnico"
	,DIS."MontoPesos"
	,DIS."Porcentaje" 
	,DIS."IdEstado"
	,CASE WHEN PORCON."MontoPesos" <> 0 THEN 'CLP' ELSE 
		CASE WHEN PORCON."MontoUF" <> 0  THEN 'UF' END 
	END AS "IdMonedaOrigen"
	,CASE WHEN INS."IdTipoInstrumento" = 'FFM' THEN COALESCE(FON."IdMoneda",'CLP') 
	      ELSE COALESCE(INS."IdMoneda",'CLP') END AS "IdMoneda"
FROM "BSCL.APV.BD::SCS.DistribucionInversion" DIS
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
ON DIS."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS FON
ON DIS."IdInstrumento" = FON."IdInstrumento"
LEFT JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" AS PORCON 
ON PORCON."IdContratoSAAPV" = DIS."IdContratoSAAPV"
WHERE DIS."IdContratoSAAPV" = :IdContrato;