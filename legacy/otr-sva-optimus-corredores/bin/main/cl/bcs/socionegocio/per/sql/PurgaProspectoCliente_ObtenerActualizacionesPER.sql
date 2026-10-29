SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.SocioNegocio.BD::PER.Persona" per
        WHERE per."IdPersona" = v."IdPersona"
          AND DAYS_BETWEEN(
                  NULLIF(per."FechaModificacion",
                         TO_TIMESTAMP('00010101000000000', 'YYYYMMDDHH24MISSFF3')),
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                  END

        UNION ALL

        SELECT 1
        FROM "BSCL.Plataforma.BD.Vistas::DOC.ConsultarDocumentoCliente" doc
        WHERE doc."IdCliente" = v."IdCliente"
          AND DAYS_BETWEEN(
                  COALESCE(
                      NULLIF(doc."FechaModificacion",
                             TO_TIMESTAMP('00010101000000000', 'YYYYMMDDHH24MISSFF3')),
                      doc."FechaIngreso"
                  ),
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );