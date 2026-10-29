SELECT (CASE WHEN "CodigoValidacion" = 'OK' THEN 'S' ELSE 'N' END) AS "CodigoValidacion" 
FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" WHERE "IdDocumento" IN (
	SELECT "IdDocumento" FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"= 'DJ1932' AND "Version" = :AgnoComercial
);