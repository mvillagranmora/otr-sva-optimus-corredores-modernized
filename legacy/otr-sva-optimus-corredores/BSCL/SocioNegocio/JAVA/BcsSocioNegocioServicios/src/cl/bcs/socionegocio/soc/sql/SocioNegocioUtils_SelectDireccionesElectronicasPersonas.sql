SELECT VDE."IdPersona" AS "IdPersona"
     , VDE."IdDireccionElectronica" AS "IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" VDE
WHERE VDE."IdEstado" = 'ACT'
  AND VDE."IdPersona" IN (:PersonasIds);