SELECT CFE."CodigoFormulario"
	 , CFE."CodigoCampo"
	 , CFE."CodigoCampoExcepcion"
     , CE."TipoDato"
	 , CE."CodigoCampoBackend" AS "CodigoCampoBackendExcepcion"
	 , CFE."ValorCampoExcepcion"
	 , CFE."IdTipoCanal"
FROM "BSCL.Plataforma.BD::FRM.CampoFormularioExcepcion" CFE
	 LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" CE ON (CFE."CodigoCampoExcepcion" = CE."CodigoCampo")
WHERE CFE."IdTipoCanal" = :IdTipoCanal;