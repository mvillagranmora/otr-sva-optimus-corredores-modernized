SELECT 
cic."IdCliente"
, TO_BIGINT(COUNT(CASE "ValorIndicador" WHEN 'S' THEN 1 ELSE NULL END)) AS "ContratosFirmados"
, TO_BIGINT(COUNT(CASE "ValorIndicador" WHEN 'N' THEN 1 ELSE NULL END)) AS "ContratosNoFirmados"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" cic
JOIN "BSCL.SocioDeNegocio.BD::GBL.ConsultaAvanzadaClientes_Proceso_Paso" capp ON capp."IdCliente" = cic."IdCliente"
WHERE cic."IdUso" = 'REQ' AND cic."GrupoIndicador" = 'CONTRATOS'
GROUP BY cic."IdCliente";