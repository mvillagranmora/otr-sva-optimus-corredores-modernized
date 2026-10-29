SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.Custodia.BD::CUS.Cartera" car
        WHERE car."IdPortafolio" = v."IdPortafolio"
          AND (car."IdEstado" IS NULL OR car."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  car."FechaOperacion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END

        UNION ALL

        SELECT 1
        FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" carn
        WHERE carn."IdPortafolio" = v."IdPortafolio"
          AND (carn."IdEstado" IS NULL OR carn."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  carn."FechaOperacion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );