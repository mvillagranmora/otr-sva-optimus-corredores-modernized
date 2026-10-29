SELECT 
DISTINCT CASE WHEN COALESCE("IdDireccionElectronica",-1)>0 THEN "IdDireccionElectronica" ELSE CEMP."IdDireccionElectronicaPrincipal" END AS "IdDireccionElectronica",
"IdPersona" AS "IdPersona", 
cee."IdEtapaEvento"  AS "IdEtapa", 
CE."IdTipoEvento" AS "IdTipoEvento",
'TO' AS "SocNeg",
cee."Nombre" AS "NombreEtapa"
FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa" ce
INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa" ea
ON ce."IdEtapa" = ea."IdEtapa" 
INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento" CEE ON CEE."IdEtapa" = CE."IdEtapa" AND  cee."IdEtapaEvento"  IN (:p_IdEtapa)
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" u ON ea."IdUsuario" = u."IdUsuario" 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP
ON cemp."IdEmpleado" = u."IdEntidad" AND (COALESCE("IdDireccionElectronica",-1)>0 OR COALESCE("IdDireccionElectronicaPrincipal",-1)>0 ) 
WHERE ce."IdTipoEvento" IN (:p_IdTipoEvento) 
AND ce."IdEstado" ='VIG' 
AND ea."IdEstado" ='VIG' 
AND u."IdEstado"  = 'ACT'