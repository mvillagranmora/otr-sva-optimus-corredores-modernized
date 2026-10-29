SELECT "Numero"
       ,"IdEstado"
       ,"IdTipoTelefono"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono"
WHERE "IdPersona" = :p_IdPersona AND "IdTipoTelefono" IN ('FIJO', 'MOVIL') AND "IdEstado" = 'ACT'