SELECT 
	IFNULL("CodigoCampo",'') 					AS "CodigoCampo", 
	IFNULL("NombreFormulario",'')				AS "NombreFormulario", 
	IFNULL("NombreCampo",'')					AS "NombreCampo", 
	IFNULL("NombreCanal",'')					AS "NombreCanal", 
	IFNULL("ValorDefectoCampoFormulario",'')	AS "ValorDefectoCampoFormulario", 
	IFNULL("Requerido",'')						AS "Requerido", 
	IFNULL("RegeneraContratos",'')				AS "RegeneraContratos"
FROM "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormulario" 
WHERE "CodigoFormulario" = :CodigoFormulario AND "CodigoCampo" = :CodigoCampo AND "IdTipoCanal" = :IdTipoCanal;