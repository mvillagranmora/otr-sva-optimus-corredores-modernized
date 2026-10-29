-- ORACLE DIALECT OVERRIDE (not legacy code): the ONLY change against the legacy file is the select list of the LEFT JOIN sub-select
-- ("SELECT *" -> the two columns the outer query uses). HANA tolerates the duplicate column names that "SELECT *" over the join produces
-- (IdMatrizAgrupadorAPV, IdRegimenTributario, IdEstado ...); H2 rejects them ("Duplicate column name"). Same rows, same semantics.
SELECT 
 "IdBonificacion"
,"AnoAhorro"
,"AnoProceso"
,"MontoPagadoNeto"
,"MontoSaldoNeto"
,"MontoPagadoReajustado"
,"MontoReajustadoPagadoAfiliado"
,"MontoCalculado"
,"MontoReajustadoPagadoAfiliado" - "MontoCalculado" AS "MontoDiferencia"
,Bonif."IdPersona"
,Bonif."IdCliente"
,Porta."IdPortafolio"
,Matriz."IdMatrizAgrupadorAPV"
,Matriz."IdPropositoPortafolio"
,Matriz."NombrePlan"
,Matriz."CodigoAgrupador"
,Porta."IdPerfilComercialBaseOrigen"
,Regimen."IdRegimenTributario"
,COALESCE(PortaBonif."IdPortafolio",0) AS "IdPortafolioBonificacion"
,"MontoBonificacionAfiliadoNeto"
,CASE WHEN "PA1" > 0 THEN 'PA1'
      WHEN "PA2" > 0 THEN 'PA2'
      WHEN "PA3" > 0 THEN 'PA3'
 END AS "TipoAhorro"
,"MontoAfiliadoNeto"
,Bonif."IdEstado"
FROM "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion" Bonif
LEFT JOIN "BSCL.APV.BD::PLA.ConfiguracionAPV" Tolerancia
	ON Tolerancia."CodigoParametro" = 'APV.ToleranciaReajuste'
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Porta
	ON Bonif."IdCliente" = Porta."IdCliente"
	AND Porta."TienePropositoAPV" = 'S'
	AND Porta."IdEstado" = 'ACT'
INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" Matriz
	ON Porta."IdMatrizAgrupadorAPV" = Matriz."IdMatrizAgrupadorAPV" 
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" Regimen
	ON Regimen."IdRegimenTributario" = Matriz."IdRegimenTributario"
	AND Regimen."Codigo" = 'APV-A'
LEFT JOIN (SELECT PortaBonif."IdPortafolio", PortaBonif."IdCliente" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PortaBonif
				INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MatrizBonif
					ON PortaBonif."IdMatrizAgrupadorAPV" = MatrizBonif."IdMatrizAgrupadorAPV" 
				INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" RegimenBonif
					ON RegimenBonif."IdRegimenTributario" = MatrizBonif."IdRegimenTributario"
					AND RegimenBonif."Codigo" = 'BF'
				WHERE PortaBonif."TienePropositoAPV" = 'S'
				AND PortaBonif."IdEstado" = 'ACT') AS PortaBonif
	ON Bonif."IdCliente" = PortaBonif."IdCliente"
	
WHERE "IdBonificacion" in (:IdsBonificacion)
ORDER BY "IdBonificacion" ASC;