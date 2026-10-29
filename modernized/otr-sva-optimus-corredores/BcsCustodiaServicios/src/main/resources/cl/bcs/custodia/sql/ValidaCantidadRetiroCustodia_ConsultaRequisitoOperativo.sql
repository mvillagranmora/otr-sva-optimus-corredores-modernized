SELECT "IdCondicionOperativa","IdTipoEtapa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
WHERE "IdTipoEtapa" = 'RETIROTITULOSRV' 
AND "IdEmpresa" = :IdEmpresa;