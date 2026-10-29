SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.SocioNegocio.BD::PER.Persona" per
        WHERE per."IdPersona" = v."IdPersona"
          AND DAYS_BETWEEN(
                  per."FechaIngreso",
                  TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
              ) < :diasBorradoDefinitivo
  );