SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mc
        WHERE mc."IdCliente" = v."IdCliente"
          AND (mc."IdEstadoFacturacion" IS NULL OR mc."IdEstadoFacturacion" <> 'ANU')
          AND DAYS_BETWEEN(
                  mc."FechaMovimientoComprobante",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END

        UNION ALL

        SELECT 1
        FROM "BSCL.Facturacion.BD::FAC.Comprobante" comp
        WHERE comp."IdCliente" = v."IdCliente"
          AND (comp."IdEstadoComprobante" IS NULL OR comp."IdEstadoComprobante" <> 'ANU')
          AND DAYS_BETWEEN(
                  comp."FechaComprobante",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                       ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                             FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                             WHERE per."IdPersona" = v."IdPersona")
                  END
  );