SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sar
        WHERE sar."IdCliente" = v."IdCliente"
          AND (sar."Estado" IS NULL OR sar."Estado" <> 'ANU')
          AND DAYS_BETWEEN(
                  sar."FechaIngresoRegistro",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) < :diasBorradoDefinitivo
  );