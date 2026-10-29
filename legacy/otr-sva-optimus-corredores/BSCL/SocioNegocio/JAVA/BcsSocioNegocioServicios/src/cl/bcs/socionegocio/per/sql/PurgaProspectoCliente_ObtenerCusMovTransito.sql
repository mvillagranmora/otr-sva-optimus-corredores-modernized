SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" mct
        WHERE mct."IdCliente" = v."IdCliente"
          AND (mct."IdEstado" IS NULL OR mct."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  mct."FechaOperacion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );