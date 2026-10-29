SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Origen" (
	"IdEmpresa"
	,"Numero"
	,"FolioCartera"
	,"IdFondo"
	,"Nemo"
	,"IdEmisor"
	,"RutEmisor"
	,"RutParticipe"
	,"RutFondo"
	,"RunFondo"
	,"TipoOperacion"
	,"FechaOperacion"
	,"FechaInversion"
	,"MontoNominalInversionActualizado"
	,"MontoHistorico"
	,"FechaRescate"
	,"MontoNominalRescate"
	,"MayorValorActualizado"
	,"MenorValorActualizado"
	,"RutAdminitradoraFondoReceptor"
	,"MontoReinvertido"
	,"NumeroCertificado" 
	,"CostoUf"
	,"Cantidad"
	,"IdCliente"
	,"IdPortafolio"	
	,"TipoDeclarante" 
) AS
WITH CTE AS (	
SELECT 
	 TRI."IdEmpresa"
	,TRI."Numero"
	,TRI."FolioCartera"
	,TRI."IdFondo"
	,TRI."Nemo"
	,TRI."IdEmisor"
	,TRI."RutEmisor"
	,TRI."RutParticipe"
	,TRI."RutFondo"
	,TRI."RunFondo"
	,TRI."TipoOperacion"
	,TRI."FechaOperacion"
	,TRI."FechaInversion"
	,TRI."MontoNominalInversionActualizado"
	--,TRI."MontoHistorico"
	,CASE WHEN TRI."TipoOperacion" < 4 THEN 
	 	CASE WHEN (M_HIS."Remanente"+ M_HIS."MontoRescatado") IS NULL THEN TRI."MontoNominalInversionActualizado"
	 	ELSE M_HIS."Remanente"+ M_HIS."MontoRescatado" END
	 ELSE TRI."MontoHistorico" 
	 END AS "MontoHistorico"
	,TRI."FechaRescate"
	,TRI."MontoNominalRescate"
	,TRI."MayorValorActualizado"
	,TRI."MenorValorActualizado"
	,TRI."RutAdminitradoraFondoReceptor"
	,TRI."MontoReinvertido"
	,TRI."NumeroCertificado" 
	,TRI."CostoUf"
	,TRI."Cantidad"
	,TRI."IdCliente"
	,TRI."IdPortafolio"
	,TRI."TipoDeclarante"	
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" TRI
LEFT JOIN 
(
SELECT TRI."FolioCartera"--,TRI."VersionDj"
	,TRI."MontoNominalInversionActualizado"
	, (IPCACTR."Valor"/IPCINVR."Valor") as "FactorIPC"
	,(( ((TRI."Cantidad" - c_res."CuotasRescatadas")/TRI."Cantidad") * TRI."CostoUf")* uf_inv."Valor") AS "Remanente"
	,TRI."FechaRescate"
	,"MontoRescatado" AS "MontoRescatado"
	,TRI."MontoNominalRescate" * (IPCACTR."Valor"/IPCINVR."Valor") AS "RemanenteIPC"
	,"MontoRescatado" * (IPCACTR."Valor"/IPCINVR."Valor") AS "MontoRescatadoIPC"
	--TRI.* 
	FROM  "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" TRI
	INNER JOIN   ---Cuotas rescatadas
	(
		SELECT "FolioCartera", SUM("Cantidad") AS "CuotasRescatadas"
		FROM  "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" TRI
		WHERE  TRI."TipoOperacion" > 3
		GROUP BY TRI."FolioCartera"
	
	) AS c_res ON (c_res."FolioCartera"=TRI."FolioCartera" ) 
	INNER JOIN   ---Monto Rescatado
	(
		SELECT "FolioCartera", SUM(TRI."CostoUf" * uf_inv."Valor") AS "MontoRescatado"
		FROM  "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" TRI
		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_inv ON (uf_inv."Fecha"= TRI."FechaRescate" )  AND uf_inv."IdParametroFinanciero" = 'UF'
		WHERE  TRI."TipoOperacion" > 3
		GROUP BY TRI."FolioCartera"
	
	) AS M_res ON (M_res."FolioCartera"=TRI."FolioCartera") 
	
	LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_inv ON (uf_inv."Fecha"= TRI."FechaInversion" )  AND uf_inv."IdParametroFinanciero" = 'UF'	
	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (SUBSTRING(ADD_MONTHS (TO_DATE (COALESCE(TRI."FechaOperacion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
								AND IPCINVR."IdParametroFinanciero" = 'IPC'
 	LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(TRI."FechaOperacion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
								AND IPCACTR."IdParametroFinanciero" = 'IPC'
	WHERE  	TRI."TipoOperacion" < 4 
	and		TRI."MontoHistorico" = 0
	) AS M_HIS
ON (M_HIS."FolioCartera"=TRI."FolioCartera" AND TRI."TipoOperacion" < 4)
LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (SUBSTRING(ADD_MONTHS (TO_DATE (COALESCE(TRI."FechaOperacion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")  
							AND IPCINVR."IdParametroFinanciero" = 'IPC'
LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(TRI."FechaOperacion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")  		
							AND IPCACTR."IdParametroFinanciero" = 'IPC'
WHERE COALESCE("TipoOperacion",0) != 0
)


SELECT   
	"IdEmpresa"
	,"Numero"
	,"FolioCartera"
	,"IdFondo"
	,"Nemo"
	,"IdEmisor"
	,"RutEmisor"
	,"RutParticipe"
	,"RutFondo"
	,"RunFondo"
	,"TipoOperacion"
	,"FechaOperacion"
	,"FechaInversion"
	,SUM("MontoNominalInversionActualizado") AS "MontoNominalInversionActualizado"
	,SUM("MontoHistorico") AS "MontoHistorico"
	,"FechaRescate"
	,SUM("MontoNominalRescate") AS "MontoNominalRescate"
	,"MayorValorActualizado"
	,"MenorValorActualizado"
	,"RutAdminitradoraFondoReceptor"
	,SUM("MontoReinvertido") AS "MontoReinvertido"
	,"NumeroCertificado" 
	,"CostoUf"
	,SUM("Cantidad") AS "Cantidad"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoDeclarante"
FROM CTE 
WHERE COALESCE("TipoOperacion",0) != 0

GROUP BY 	
	 "IdEmpresa"
	,"Numero"
	,"FolioCartera"
	,"IdFondo"
	,"Nemo"
	,"IdEmisor"
	,"RutEmisor"
	,"RutParticipe"
	,"RutFondo"
	,"RunFondo"
	,"TipoOperacion"
	,"FechaOperacion"
	,"FechaInversion"
	,"MontoNominalInversionActualizado"
	,"MontoHistorico"
	,"FechaRescate"
	,"MontoNominalRescate"
	,"MayorValorActualizado"
	,"MenorValorActualizado"
	,"RutAdminitradoraFondoReceptor"
	,"MontoReinvertido"
	,"NumeroCertificado" 
	,"CostoUf"
	,"Cantidad"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoDeclarante"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Origen" TO bcs_desarrollador WITH GRANT OPTION;
