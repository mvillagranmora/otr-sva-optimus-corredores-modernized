SELECT
	'D' AS "TipoRegistro" --Constante
	,'S80080' AS "CodigoAI" --Constante
	,INSTRUMENTO."Nemotecnico" AS "CodigoIF"
	,COALESCE(INSTRUMENTO."DocumentoIdentidadEmisor",'') AS "IdentificadorUnicoPersona"
	,COALESCE(INSTRUMENTO."NombreCompletoEmisor",'') AS "RazonSocial"
	,COALESCE(HOMOLOGACION_MONEDA."ValorHomologacion",'') AS "Moneda"
	,COALESCE(HOMOLOGACION_TIPO_INSTRUMENTO."ValorHomologacion",'') AS "TipoInstrumento"
	,COALESCE(DIRECCION_FISICA."NombreLargo",'') AS "Direccion"
	,CASE WHEN PAIS_RESIDENCIA."CodigoPais" = 'PE' THEN DIRECCION_FISICA."CodigoDivisionPais" ELSE '' END AS "Ubigeo"
	,COALESCE(HOMOLOGACION_PAIS_RESIDENCIA."ValorHomologacion",'000') AS "Pais"
	,CASE WHEN INSTRUMENTO."FechaVencimiento" = '0001-01-01' THEN '' ELSE COALESCE(TO_CHAR(INSTRUMENTO."FechaVencimiento",'DDMMYYYY'),'') END AS "FechaVencimiento"
	,'0' AS "PrecioDerivado" --Verificar
	,INSTRUMENTO."ISIN" AS "IdentificadorUnico"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INSTRUMENTO
LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec on prec."IdBolsa" = 'BVL' and prec."IdInstrumento" = INSTRUMENTO."IdInstrumento" and prec."Fecha" <= TO_CHAR(:FechaDesde,'YYYY-MM-DD')
LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas" puntas on INSTRUMENTO."IdInstrumento" = puntas."IdInstrumento" and puntas."Fecha" = TO_CHAR(:FechaDesde,'YYYY-MM-DD') and puntas."IdBolsa" = 'BVL' and puntas."IdCondicionDeLiquidacion" = 'T2'
LEFT JOIN "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos" PreTas on PreTas."IdInstrumento" = INSTRUMENTO."IdInstrumento" and PreTas."IdTrabajo" = 100
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (INSTRUMENTO."IdEmisor" = EMISOR."IdEmisor")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."IdPersona" = EMISOR."IdPersona")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = PERSONA."IdDireccionFisicaPrincipal")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS_RESIDENCIA ON (PAIS_RESIDENCIA."IdPais" = DIRECCION_FISICA."IdPais")
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_PAIS_RESIDENCIA ON (HOMOLOGACION_PAIS_RESIDENCIA."IdCampo" = 'Pais' AND HOMOLOGACION_PAIS_RESIDENCIA."Valor" = PAIS_RESIDENCIA."CodigoPais")
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_MONEDA ON (HOMOLOGACION_MONEDA."IdCampo" = 'Moneda' AND HOMOLOGACION_MONEDA."Valor" = INSTRUMENTO."IdMoneda")
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_TIPO_INSTRUMENTO ON (HOMOLOGACION_TIPO_INSTRUMENTO."IdCampo" = 'TipoInstrumento' AND HOMOLOGACION_TIPO_INSTRUMENTO."Valor" = INSTRUMENTO."IdTipoInstrumento")
WHERE prec."Fecha" <= TO_CHAR(:FechaDesde,'YYYY-MM-DD') 
AND (:EsTodosInstrumentos = 'S' OR INSTRUMENTO."IdInstrumento" = :IdInstrumento);