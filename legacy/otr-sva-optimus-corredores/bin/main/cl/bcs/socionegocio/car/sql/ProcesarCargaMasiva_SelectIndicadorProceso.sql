SELECT
	COALESCE(IND."IdIndicador",0) AS "IdIndicador",
	SEL."IdTipoIndicador",
	SEL."IdEntidad" AS "p_IdEntidad",
	SEL."Entidad" AS "p_IdTipoEntidad",
	CASE WHEN SEL."ClaseDato"='COMBO' THEN CAST(SEL."DetalleIndicador" AS VARCHAR)
		ELSE SEL."ValorIndicador" END
	AS "ValorIndicador",
	COALESCE(IND."ModeloCliente",'') AS "ModeloCliente"
FROM (
	SELECT DID."DocumentoIdentidad", PER."IdPersona",CLI."IdCliente",CMD."IdPortafolio",CMD."Entidad",TIN."IdTipoIndicador",TIN."ClaseDato",CMD."CodigoIndicador",DIN."IdDetalleIndicador" AS "DetalleIndicador",CMD."IdCargaMasiva",CMD."IdCargaMasivaDetalle",
	CASE
		WHEN CMD."Entidad"='PORTAFOLIO' THEN CMD."IdPortafolio"
		WHEN CMD."Entidad"='CLIENTE' THEN CLI."IdCliente"
		WHEN CMD."Entidad"='PERSONA' THEN PER."IdPersona"
		ELSE CLI."IdPersona" END AS "IdEntidad",
	CMD."ValorIndicador"
	FROM "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" CMD
	LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (TIN."CodigoTipoIndicador"=CMD."CodigoIndicador" AND TIN."IdTipoEntidad"=CMD."Entidad")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DID ON (DID."DocumentoIdentidad"=CMD."DocumentoIdentidad")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (DID."IdDocumentoIdentidad"=PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdPersona"=PER."IdPersona")
	LEFT JOIN"BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON (TIN."IdTipoIndicador"=DIN."IdTipoIndicador" AND CMD."ValorIndicador"=DIN."Codigo")
	WHERE CMD."IdEstado"='ING' AND TIN."CodigoTipoIndicador"=CMD."CodigoIndicador" AND CMD."IdCargaMasiva"=:IdCargaMasiva) SEL
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
	ON (SEL."IdTipoIndicador"=IND."IdTipoIndicador" AND SEL."IdEntidad"=IND."IdEntidad");