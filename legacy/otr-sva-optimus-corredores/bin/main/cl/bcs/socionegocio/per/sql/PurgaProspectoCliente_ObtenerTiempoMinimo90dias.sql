SELECT DISTINCT DI."DocumentoIdentidad" AS "RutTitular"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
    ON PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad"
WHERE DI."DocumentoIdentidad" IN (:DocumentosIdentidad)
  AND DAYS_BETWEEN(PER."FechaIngreso", :fechaSistema) < :diasBorradoProspectoConsentimiento;