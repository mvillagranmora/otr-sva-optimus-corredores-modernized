SELECT C."CodigoCampo"
	 , C."CodigoCampoBackend"
	 , C."NombreCampo"
	 , C."TipoDato"
	 , CF."Requerido"
	 , CF."CodigoFormulario"
	 , CF."CantidadMinima"
	 , F."IdUso"
	 , C."CodigoCampoFrontend"
	 , C."ValorDefecto"  AS "ValorVacioCampo"
	 , CF."ValorDefecto" AS "ValorDefectoCampoFormulario"
	 , F."Ubicacion"     AS "UbicacionFormulario"
FROM "BSCL.Plataforma.BD::FRM.Campo" C
     LEFT JOIN "BSCL.Plataforma.BD::FRM.CampoFormulario" CF ON CF."CodigoCampo" = C."CodigoCampo"
     LEFT JOIN "BSCL.Plataforma.BD::FRM.Formulario" AS F ON F."CodigoFormulario" = CF."CodigoFormulario"
WHERE CF."Requerido" = 'S'
  AND CF."CodigoFormulario" IN (
                                'DATOS_GENERALES_CLIENTE_FULL_JURIDICA',
                                'DATOS_GENERALES_CLIENTE_FULL_NATURAL',
                                'DATOS_GENERALES_PERSONA_FULL_JURIDICA',
                                'DATOS_GENERALES_PERSONA_FULL_NATURAL',
                                'FATCA_FULL_NATURAL',
                                'FATCA_FULL_JURIDICA',
                                'DATOS_GENERALES_PORTAFOLIO_CLIENTE_FULL_JURIDICA',
                                'DATOS_GENERALES_PORTAFOLIO_CLIENTE_FULL_NATURAL',
                                'GRILLA_FULL_JURIDICA',
                                'GRILLA_FULL_NATURAL',
                                'GRILLA_PORTAFOLIO_FULL_JURIDICA',
                                'GRILLA_PORTAFOLIO_FULL_NATURAL',
                                'INDICADORES_CLIENTE_FULL_JURIDICA',
                                'INDICADORES_CLIENTE_FULL_NATURAL',
                                'INDICADORES_PERSONA_FULL_JURIDICA',
                                'INDICADORES_PERSONA_FULL_NATURAL',
                                'PORTAFOLIO_CLIENTE_INDICADORES_FULL_JURIDICA',
                                'PORTAFOLIO_CLIENTE_INDICADORES_FULL_NATURAL',
                                'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_JURIDICA',
                                'PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_NATURAL',
                                'PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_JURIDICA',
                                'PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_NATURAL'
    )
  AND CF."IdTipoCanal" = :IdTipoCanalIngreso
  AND F."IdUso" <> 'CRE'
  AND C."TipoDato" <> 'CHECK';