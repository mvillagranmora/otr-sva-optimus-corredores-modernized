SELECT "IdContratoComision" 
FROM "BSCL.Plataforma.BD::COM.ContratoComision"
WHERE "IdCliente" = :IdCliente 
AND "IdPortafolio" = :IdPortafolio
AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
AND "IdEstado" = :IdEstado
AND "IdContratoComision" != (CASE
    WHEN :IdContratoComision IS NULL THEN 0
    ELSE :IdContratoComision
END)
AND "IdTipoContrato" = CASE WHEN :IdTipoContrato = 'CONV' THEN "IdTipoContrato" ELSE 'CONV' END 

