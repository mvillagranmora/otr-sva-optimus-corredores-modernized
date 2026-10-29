SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" ie
        WHERE ie."IdPersona" = v."IdPersona"
          AND (ie."IdEstado" IS NULL OR ie."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  ie."FechaMovimiento",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END

        UNION ALL

        SELECT 1
        FROM "BSCL.Tesoreria.BD::TES.Comprobante" comp
        WHERE comp."IdPersona" = v."IdPersona"
          AND (comp."IdEstado" IS NULL OR comp."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  comp."FechaMovimiento",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );