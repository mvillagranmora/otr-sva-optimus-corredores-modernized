SELECT "IdContratoComision"
FROM "BSCL.Plataforma.BD::COM.ContratoComision"
WHERE "IdCliente" = :IdCliente 
AND "IdPortafolio" = (CASE WHEN "IdFormatoCobro" = 'AGRRUT' THEN "IdPortafolio" ELSE :IdPortafolio END)
AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
AND "IdEstado" = :IdEstado
AND "IdContratoComision" != (CASE
    WHEN :IdContratoComision IS NULL THEN 0
    ELSE :IdContratoComision
END)
AND "IdMoneda" = :IdMoneda 
