SELECT
"IdSubdetalleContratoComision" as "IdInterno",
"IdSubdetalleContratoComision" 
FROM "BSCL.Plataforma.BD::COM.SubdetalleContratoComision"
WHERE "IdDetalleContratoComision" IN (:IdDetalleContratoComision);