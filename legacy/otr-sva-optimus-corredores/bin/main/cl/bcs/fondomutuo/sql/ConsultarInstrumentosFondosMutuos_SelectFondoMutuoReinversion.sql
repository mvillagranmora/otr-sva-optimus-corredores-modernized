---DESTINOS DE REINVERSION SEGUN REGIMEN
SELECT
  SER."IdSerieFondo"
, SER."IdFondo" AS "CodigoFondo" 
, FDO."NombreFondo"
, FDO."Nemotecnico"
, SER."CodigoSerie"
, COALESCE(SER."NombreSerieFondo",'') AS "NombreSerie"
, FDO."IdEmisor"
, COALESCE(CEM."NombreCompleto",'') AS "NombreEmisor"
, FDO."IdMoneda"
, SER."IdFamiliaFondo"
, COALESCE(VCO."ValorCuota",0) AS "ValorCuota"
, COALESCE(TO_VARCHAR(VCO."FechaCierre",'YYYY-MM-DD'),'0001-01-01') AS "FechaValorCuota"
, SER."MontoMinimo" AS "MontoMinimoInversionPesos"
, SER."MontoMinimo" AS "MontoMinimoInversionTotal"
, SER."MontoMaximo" AS "MontoMaximoInversionPesos"
, SER."MontoMaximo" AS "MontoMaximoInversionTotal"
, FDO."IdCategoriaPerfilFondoMutuo" AS "IdCategoriaPerfilInversionista"
, COALESCE(PER."NombreCategoriaPerfilFondoMutuo",'') AS "NombreCategoriaPerfilInversionista"
, SER."CobraComision"
, SER."InstrumentoOWP"
, SER."EsAPV"
, SER."RestringeAporte"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER 
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS FDO 
ON (SER."IdFondo" = FDO."IdFondo" AND  "BloqueoAporte"='N')     
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS CEM ON  (CEM."IdEmisor" = FDO."IdEmisor")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS VCO ON (VCO."IdSerieFondo" = SER."IdSerieFondo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCategoriaPerfilFondoMutuo" AS PER ON (PER."IdCategoriaPerfilFondoMutuo" =FDO."IdCategoriaPerfilFondoMutuo")
WHERE 
(COALESCE(:IdSerieFondo,'') = '' OR SER."IdSerieFondo" = :IdSerieFondo)
AND (COALESCE(:IdFamiliaFondo,'') = '' OR SER."IdFamiliaFondo" = :IdFamiliaFondo)
AND (COALESCE(:IdEstado,'') = '' OR SER."IdEstado" = :IdEstado)
AND (COALESCE(:InstrumentoOWP,'') = '' OR SER."InstrumentoOWP" = :InstrumentoOWP)
AND (COALESCE(:EsAPV,'') = '' OR SER."EsAPV" = :EsAPV)
AND (COALESCE(:IdEmisor,'') = '' OR FDO."IdEmisor" = :IdEmisor)
AND (COALESCE(:IdCategoriaPerfilFondoMutuo,'') = '' OR FDO."IdCategoriaPerfilFondoMutuo" = :IdCategoriaPerfilFondoMutuo)
AND (COALESCE(:IdMoneda,'') = '' OR FDO."IdMoneda" = :IdMoneda)
AND (COALESCE(:FondoExtranjero,'') = '' OR FDO."FondoExtranjero" = :FondoExtranjero)
AND (:FiltroRegimen = 0 OR EXISTS (
				SELECT 1 FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" REG
				WHERE SER."IdSerieFondo" = REG."IdSerieFondo" 
				AND REG."IdRegimenFondoMutuo" IN (:IdRegimenFondoMutuo)
				)
)
 AND EXISTS 
(	  
		  ---BUSCAR REINVERSION
			SELECT 1 --DISTINCT DES."IdSerieFondo"
			FROM
				(
					SELECT REG_CONF."IdRegimenFondoMutuoDestino" AS "IdRegimenFondoMutuo" FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" REG
					LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionRegimenFondoMutuo" REG_CONF ON REG_CONF."IdRegimenFondoMutuoOrigen"=REG."IdRegimenFondoMutuo"
					WHERE REG."IdSerieFondo"= :IdSerieFondoOrigen
					UNION
					SELECT "IdRegimenFondoMutuo" FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" REG
					WHERE "IdSerieFondo"= :IdSerieFondoOrigen
				)AS REF
			INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" DES ON (DES."IdRegimenFondoMutuo"= CASE WHEN REF."IdRegimenFondoMutuo"='NINGUNO' THEN DES."IdRegimenFondoMutuo" ELSE  REF."IdRegimenFondoMutuo" END)
			
			WHERE  DES."IdSerieFondo" =SER."IdSerieFondo"
	  )
AND (COALESCE(:IdSerieFondoOrigen,'') = '' OR SER."IdSerieFondo" <> :IdSerieFondoOrigen)
AND SER."RestringeAporte"='N'
AND SER."IdEmpresa"=:IdEmpresa