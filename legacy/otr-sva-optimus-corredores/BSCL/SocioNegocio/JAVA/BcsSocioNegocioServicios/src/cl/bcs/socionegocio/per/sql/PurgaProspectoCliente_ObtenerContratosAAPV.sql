
SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.APV.BD.Vistas::AFI.ConsultarContratoSAAPV" csa
        WHERE csa."IdCliente" = v."IdCliente"
          AND (csa."IdEstado" IS NULL OR csa."IdEstado" <> 'ANU')
          AND DAYS_BETWEEN(
                  csa."FechaSuscripcion",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) < :diasBorradoDefinitivo
  );