WITH CUSTODIA AS (SELECT "DocumentoIdentidad", "CodigoPortafolio", "Nemotecnico", "FechaCustodia"
					FROM "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis" (
					PLACEHOLDER."$$p_idempresa$$" => '1',
					PLACEHOLDER."$$p_fechaconsulta$$" => :p_FechaCustodia,
					PLACEHOLDER."$$p_tipofechaconsulta$$" => 'ADQ'
					) WHERE "PrecioMercadoCustodia" = 0 and "Nemotecnico" != ''
				UNION ALL 
					SELECT "DocumentoIdentidad", "CodigoPortafolio", "Nemotecnico", "FechaCustodia"
					FROM "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis" (
					PLACEHOLDER."$$p_idempresa$$" => '1',
					PLACEHOLDER."$$p_fechaconsulta$$" => :p_FechaCustodia,
					PLACEHOLDER."$$p_tipofechaconsulta$$" => 'LIQ'
					) WHERE "PrecioMercadoCustodia" = 0 and "Nemotecnico" != ''
) SELECT DISTINCT "DocumentoIdentidad" AS "Valor1","CodigoPortafolio" AS "Valor2","Nemotecnico" AS "Valor3", "FechaCustodia" AS "Valor4"
FROM CUSTODIA;
