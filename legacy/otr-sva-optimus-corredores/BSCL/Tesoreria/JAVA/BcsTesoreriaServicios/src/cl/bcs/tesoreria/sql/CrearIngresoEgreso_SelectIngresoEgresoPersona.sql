SELECT PER."IdPersona", PER."IdDireccionElectronicaPrincipal"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
WHERE "IdPersona" IN (:idsPersona);