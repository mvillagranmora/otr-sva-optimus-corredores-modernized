SELECT
	"CodigoFormulario"
	,"NombreFormulario"
	,"CodigoCampo"
	,"CodigoCampoFrontend"
	,"CodigoValorFrontend"
	,"CodigoCampoBackend"
	,"NombreCampo"
	,"TipoDato"
	,"ValorDefecto"
	,"CantidadMinima"
	,"Requerido"
	,"RegeneraContratos"
	,"IdTipoCanal"
	,"NombreCanal"
	,"TipoCampoFrontend"
	,"ConfiguracionFrontend"
	,"ValorDefectoCampoFormulario"
	,"Campo"
	,"AplicaRequerido"
FROM "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormulario"
WHERE 
("CodigoFormulario" = :CodigoFormulario OR :CodigoFormulario = '0' ) AND
("CodigoCampo" = :CodigoCampo OR :CodigoCampo = '0') AND
("IdTipoCanal" = :IdTipoCanal OR :IdTipoCanal = '0');