SELECT DI."IdPersona" AS "IdPersonaEntidad"  
	,TRA."IdContratoSAAPV"
FROM "BSCL.APV.BD::TRA.Traspaso" AS TRA
LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON DI."DocumentoIdentidad"= TRA."DocumentoEntidad" AND DI."Predeterminado" ='S'
WHERE TRA."IdContratoSAAPV" IN (:IdContratoSAAPV);
