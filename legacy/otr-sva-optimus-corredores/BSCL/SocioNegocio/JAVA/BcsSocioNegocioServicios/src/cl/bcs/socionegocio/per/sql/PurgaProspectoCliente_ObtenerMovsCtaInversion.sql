SELECT DISTINCT v."DocumentoIdentidad" AS "RutTitular"
FROM BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" v
WHERE v."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND EXISTS (
        SELECT 1
        FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cta
        INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cam
                ON cam."IdCuentaInversion" = cta."IdCuentaInversion"
               AND cam."IdEstado" IN ('CRT','PEN')
               AND DAYS_BETWEEN(
                       cam."FechaEfectivaMovimiento",
                       TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3')
                   ) <= CASE WHEN :esBorradoDefinitivo = 1 THEN :diasBorradoDefinitivo
                            ELSE (SELECT DAYS_BETWEEN(per."FechaIngreso", TO_TIMESTAMP(:FechaSistema, 'YYYYMMDDHH24MISSFF3'))
                                  FROM "BSCL.SocioNegocio.BD::PER.Persona" per
                                  WHERE per."IdPersona" = v."IdPersona")
                       END
        WHERE cta."IdPortafolio" = v."IdPortafolio"
  );