SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
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
SELECT DISTINCT
	 a."ID"
	,a."IdDocumento"
	,a."VboUsuario"
	,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
	,a."FuenteDatos"
	,a."Numero"
	,a."FolioCartera"
	,a."IdFondo"
	,a."Nemo"
	,CASE WHEN (a."IdEmisor" = '' OR a."IdEmisor" IS NULL) AND a."FuenteDatos" = 'EXTERNA'
		THEN COALESCE(ins."IdEmisor", '')
		ELSE a."IdEmisor"
	END AS "IdEmisor"
	,a."RutEmisor"
	,a."RutParticipe"
	,a."RutFondo"
	,a."RunFondo"
	,a."TipoOperacion"
	,a."FechaOperacion"
	,a."FechaInversion"
	,ROUND(a."MontoNominalInversionActualizado")
	,a."MontoHistorico"
	,CASE WHEN a."TipoOperacion" IN ('8','9') THEN '1900-01-01'
	 ELSE a."FechaRescate"
	 END AS "FechaRescate"
	
	 ,CASE WHEN a."TipoOperacion" IN ('8','9') THEN 0
	 ELSE ROUND(a."MontoNominalRescate")
	 END AS "MontoNominalRescate"
	 
    ,CASE WHEN a."TipoOperacion" IN ('8','9') THEN 0
     ELSE ROUND(a."MayorValorActualizado")
     END AS "MayorValorActualizado"
     
    ,CASE WHEN a."TipoOperacion" IN ('8','9') THEN 0
     ELSE ROUND(a."MenorValorActualizado")
     END AS "MenorValorActualizado"
     
	,CASE
	WHEN a."TipoOperacion" IN ('6','7') THEN
	CASE WHEN a."FechaRescate" <= '2001-04-19' THEN a."RutAdminitradoraFondoReceptor"
	ELSE ''
	END
	WHEN a."TipoOperacion" IN ('8','9') THEN a."RutAdminitradoraFondoReceptor"
	ELSE ''
	END AS "RutAdminitradoraFondoReceptor"
	
 	,CASE
	WHEN a."TipoOperacion" IN ('6','7') THEN
	CASE WHEN a."FechaRescate" <= '2001-04-19' THEN ROUND(a."MontoReinvertido")
	ELSE 0
	END
	WHEN a."TipoOperacion" IN ('8','9') THEN ROUND(a."MontoReinvertido")
	ELSE 0
	END AS "MontoReinvertido"
	,a."NumeroCertificado" 
	,a."CostoUf"
	,a."Cantidad"
	,a."IdCliente"	
	,a."IdPortafolio"
	,a."TipoDeclarante"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1894" a
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" d ON (d."IdDocumento" = a."IdDocumento")
LEFT JOIN "BSCL.Normativa.BD::DOC.Diccionario" b
    ON (b."Tipo"='TipoOperacionDJ1894' AND a."TipoOperacion"=b."Codigo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" sf ON
	(sf."IdSerieFondo" = a."Nemo")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON
	(ins."IdInstrumento" = sf."IdInstrumento")
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1894" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" TO bcs_desarrollador WITH GRANT OPTION;
