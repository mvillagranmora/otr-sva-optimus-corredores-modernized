SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.Orden.BD::ORD.Orden" ord
        WHERE ord."IdCliente" = v."IdCliente"
          AND (ord."IdEstadoOrden" IS NULL OR ord."IdEstadoOrden" <> 'ANU')
          AND DAYS_BETWEEN(
                  ord."FechaOperacion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END

        UNION ALL

        SELECT 1
        FROM "BSCL.Orden.BD::ORD.Asignacion" asig
        INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord2
                ON ord2."IdOrden" = asig."IdOrden"
        WHERE ord2."IdCliente" = v."IdCliente"
          AND (asig."IdEstadoAsignacion" IS NULL OR asig."IdEstadoAsignacion" <> 'ANU')
          AND DAYS_BETWEEN(
                  asig."FechaAsignacion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );