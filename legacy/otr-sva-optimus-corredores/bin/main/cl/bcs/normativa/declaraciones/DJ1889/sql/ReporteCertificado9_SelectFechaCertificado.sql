	SELECT DOCDJ."DefineFechaCertificado" 				as "DefineFechaCertificado"
	, SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) as "FechaCertificado"
	FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1889' AND "Version" = :Version AND "IdTipoDocumento" = 'DECLACORR';