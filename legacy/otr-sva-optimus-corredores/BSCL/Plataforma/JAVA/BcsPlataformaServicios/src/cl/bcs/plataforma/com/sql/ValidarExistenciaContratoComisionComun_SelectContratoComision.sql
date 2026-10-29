SELECT "IdContratoComision" 
FROM "BSCL.Plataforma.BD::COM.ContratoComision"
WHERE "IdCliente" = :IdCliente
AND ( (:IdFormatoCobro = 'AGRRUT')  OR (:IdFormatoCobro <> 'AGRRUT' AND "IdFormatoCobro" = 'AGRRUT') OR (:IdFormatoCobro <> 'AGRRUT' AND "IdFormatoCobro" <> 'AGRRUT' AND "IdPortafolio" = :IdPortafolio))
AND "IdMoneda" = :IdMoneda
AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
AND "IdEstado"			= :IdEstado
AND "IdTipoContrato" = 'CONT'
AND "IdContratoComision" != (CASE
    WHEN :IdContratoComision IS NULL THEN 0
    ELSE :IdContratoComision
END);
