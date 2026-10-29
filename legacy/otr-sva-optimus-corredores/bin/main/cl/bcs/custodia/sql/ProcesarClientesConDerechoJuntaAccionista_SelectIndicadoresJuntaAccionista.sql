
-- Consulta el valor del indicador de cliente FACULTA ASISTENCIA JUNTA ACCIONISTA
SELECT "IdCliente", "CodigoTipoIndicador", "ValorIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" 
WHERE "IdCliente" in (:IdsCliente) 
AND   "CodigoTipoIndicador" = 'FACASITJA' -- FACULTA ASISTENCIA JUNTA ACCIONISTA
AND   "IdEmpresa" = :IdEmpresa;
