
	INSERT INTO "BSCL.Normativa.BD::TRI.ResumenGeneracion"
	(ID, "IdProceso", "IdDocumento", "IdUsuario", "Fecha", "Glosa", "TotalRegistrosGenerados", "NombreArchivo", "UltimoCorrelativo")
	SELECT  TOP 1 (SELECT max (ID)+ 1  FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion") AS ID
	, :IdProceso 	AS "IdProceso"
	, (SELECT TOP 1 "IdDocumento"  FROM "BSCL.Normativa.BD::DOC.Documento"
				 WHERE "Esquema"=:EsquemaDJ
		   	     AND  "Version"=:VersionDJ
				 AND  "IdEmpresa"=:idEmpresa ORDER BY "IdDocumento" DESC)		AS "IdDocumento"
	, :idUsuario	AS "IdUsuario"
	, CURRENT_UTCTIMESTAMP 	AS "Fecha"
	, 'Encolada' 	AS  "Glosa"
	, 0 			AS "TotalRegistrosGenerados"
	, '' 			AS "NombreArchivo"
	, 0 			AS "UltimoCorrelativo"
	FROM DUMMY;