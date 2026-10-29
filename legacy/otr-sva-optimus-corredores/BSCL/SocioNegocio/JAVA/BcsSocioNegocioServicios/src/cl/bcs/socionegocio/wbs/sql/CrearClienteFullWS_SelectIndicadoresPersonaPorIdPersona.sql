SELECT 
	IND."ValorIndicador"
	,IND."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON (PER."IdPersona" = IND."IdPersona")
WHERE  IND."IdPersona" IN (:IdPersona)
AND IND."IdUso"  = 'IND';