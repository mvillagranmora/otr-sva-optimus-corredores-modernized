UPDATE "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
SET FA."IdEstado"        = 'ACT'
  , FA."IdPersonaAgente" = :IdPersonaAgente
  , FA."FechaFirma"      = :FechaFirma
  , FA."EsAutorizado"    = :EsAutorizado
  , FA."IdTipoCanalIngreso"    = :IdTipoCanalIngreso
WHERE "IdFirmaAnexo" = :IdFirmaAnexo;