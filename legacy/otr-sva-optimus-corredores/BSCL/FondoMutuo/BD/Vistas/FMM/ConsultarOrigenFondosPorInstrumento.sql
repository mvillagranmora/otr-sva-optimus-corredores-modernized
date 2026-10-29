SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOrigenFondosPorInstrumento";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOrigenFondosPorInstrumento"
AS
SELECT SerieFondo."IdSerieFondo"							
     , SerieFondo."IdInstrumento"
     , SerieFondo."IdFondo"
     , SerieFondo."CodigoSerie"
     , SerieFondo."IdEmisor"
     , DctoIdentidad."DocumentoIdentidad" AS "DocumentoIdentidad"
     , CASE WHEN SUBSTR_BEFORE(RegimenSerieFondo."IdRegimenFondoMutuo",'-') = 'APV' THEN 
     		SUBSTR_AFTER(RegimenSerieFondo."IdRegimenFondoMutuo",'-')
       ELSE ''
       END 																AS "RegimenTributario"	
     , COALESCE(RegimenSerieFondo."IdRegimenFondoMutuo",'RTN') 			AS "TipoAhorro"
     , COALESCE(RegimenSerieFondo."Descripcion",'REGIMEN NORMAL') 		AS "DescripcionTipoAhorro"
     , COALESCE(RegimenSerieFondo."IdRegimenFondoMutuo",'RTN') 			AS "TipoCuenta"
     , COALESCE(RegimenSerieFondo."Descripcion",'REGIMEN NORMAL') 		AS "DescripcionTipoCuenta"
     , COALESCE(RegimenSerieFondo."TipoCarteraNormativa",'RTN') 		AS "Regimen"
     , COALESCE(RegimenSerieFondo."AceptaMandatoEnReinversion",'') 		AS "ClasificacionMLT"
     , RegimenSerieFondo."IdRegimenFondoMutuo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondo" AS SerieFondo
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo" AS RegimenSerieFondo	
		ON RegimenSerieFondo."IdSerieFondo" = SerieFondo."IdSerieFondo"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS Emisor 
		ON Emisor."IdEmisor" = SerieFondo."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Persona 
		ON Persona."IdPersona" = Emisor."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DctoIdentidad
		ON DctoIdentidad."IdDocumentoIdentidad" = Persona."IdDocumentoIdentidad" AND DctoIdentidad."Predeterminado" = 'S';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOrigenFondosPorInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
