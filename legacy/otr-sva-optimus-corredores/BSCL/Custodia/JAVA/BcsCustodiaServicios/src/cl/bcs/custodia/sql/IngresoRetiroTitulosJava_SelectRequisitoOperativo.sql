SELECT "IdCondicionOperativa"
	,"IdTipoEtapa"
	,"IdTipoCanalIngreso"
	,"IdTipoComportamiento"
	,"IdEstado"
	,"IdTipoCanalIngreso" || "IdTipoEtapa" AS "KeyRequisitos"
FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
WHERE "IdTipoEtapa" IN ('INGRESOTITULOSRV','TRASPASOTITULOSRV')
AND "IdEmpresa" = :IdEmpresa
AND "IdEstado" = 'ACT';