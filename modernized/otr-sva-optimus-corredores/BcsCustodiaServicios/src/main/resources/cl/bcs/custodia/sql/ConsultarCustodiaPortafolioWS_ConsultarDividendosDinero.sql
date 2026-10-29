-- Copia propia del WS de ConsultarCustodiaPortafolio_ConsultarDividendosDinero.sql (compartido con
-- ConsultarCustodiaPortafolio.java, que no se toca). Cambios respecto del original:
--   1. Se agrega el filtro por :p_IdEmpresa, que el original no tenia, en las dos ramas del CTE y en
--      el WHERE externo.
--   2. Los filtros con la forma "col = CASE WHEN :p > 0 THEN :p ELSE col END" se reemplazan por los
--      macros CASE.ALIAS.p_X de PlataformaUtils. El CASE obliga a HANA a materializar el CTE
--      sobre CUS.Cartera completa y a expandir la vista CUS.ConsultarEventoCapitalCliente (11 vistas
--      encadenadas) antes de filtrar por cliente: 5594 ms medidos para devolver 1 fila. El macro
--      expande a un predicado de igualdad indexable cuando el filtro viene, y a nada cuando no viene
--      (equivalente a la tautologia "col = col" del original).
WITH
	Cartera AS
	(
		SELECT
			CAR."IdPortafolio"
			,CAR."Cantidad"
			,CAR."IdEstado"
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		WHERE CAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :p_IdEmpresa
		<<CASE.CAR.p_IdInstrumento>>
		<<CASE.CAR.p_Nemotecnico>>
		<<CASE.CAR.p_IdPortafolio>>
		<<CASE.CAR.p_IdCliente>>

		UNION ALL

		SELECT
			CAR."IdPortafolio"
			,GAR."INGCantidad"AS "Cantidad"
			,GAR."IdEstado"
			,CAR."FechaOperacion"
			,CAR."Nemotecnico"
			,CAR."INGFechaLiquidacion"
			,CAR."IdCartera"
			,CAR."FechaIngresoRegistro"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
		WHERE GAR."IdEstado" = 'VIG'
		AND CAR."IdEmpresa" = :p_IdEmpresa
		<<CASE.CAR.p_IdInstrumento>>
		<<CASE.CAR.p_Nemotecnico>>
		<<CASE.CAR.p_IdPortafolio>>
		<<CASE.CAR.p_IdCliente>>
)
SELECT
	 EVC."IdCliente"
	,EVC."IdPortafolio"
	,EVC."IdInstrumento"
	,SUM(
	CASE
		WHEN cast("FechaCierre" as DATE)>= CAR."INGFechaLiquidacion" AND (EVCCAR."IdCartera" IS NOT NULL )
			THEN CAR."Cantidad"
		ELSE 0 END * (EVC."Total"/EVC."CantidadDerecho") - ( CASE WHEN EVC."ISFUT"='S' THEN (EC."TasaEfectiva" * ((EC."Factor" * CAR."Cantidad")/100)) ELSE 0 END))	AS "Disponible"
	,EVC."IdMoneda"
	,TO_CHAR(MIN(CCM."FechaEfectivaMovimiento"),'YYYY-MM-DD') AS "FechaEfectivaMovimiento"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" EVC
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EC ON EC."IdEventoCapital"=EVC."IdEventoCapital" AND (EC."IdTipoEventoCapital"='DV'  OR EC."IdTipoEventoCapital" = 'RC')
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CCM ON CCM."IdTipoProducto"='EVCA' AND  CCM."IdOperacion"=EVC."IdEventoCapitalCliente"
INNER JOIN Cartera CAR
	ON EVC."IdPortafolio" = CAR."IdPortafolio"
	AND EVC."Nemo" = CAR."Nemotecnico"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteCartera" EVCCAR ON
EVC."IdEventoCapitalCliente" =EVCCAR."IdEventoCapitalCliente"  AND COALESCE(EVCCAR."IdCartera",-1) = CAR."IdCartera"
WHERE
EVC."IdEmpresa" = :p_IdEmpresa
AND EVC."IdClasificacionTipoInstrumento" = 'RV'
AND (EVC."IdTipoEventoCapital" = 'DV' OR EVC."IdTipoEventoCapital" = 'RC')
AND EVC."IdEstado" = 'CER'
AND EVC."CantidadDerecho" > 0
AND CAR."IdEstado" = 'VIG'
AND CAST (EVC."FechaCierre" as DATE) >= CAR."INGFechaLiquidacion"
AND CAST (EVC."FechaPago" as DATE) <=:p_FechaConsulta
-- p_Nemotecnico es el unico filtro que no se convierte a macro: creaCondicionesQuery deriva el nombre
-- de la columna del nombre del parametro (p_Nemotecnico -> "Nemotecnico") y en esta vista la columna
-- se llama "Nemo". Se preserva la forma original del archivo compartido.
AND EVC."Nemo" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE EVC."Nemo" END
<<CASE.EVC.p_IdInstrumento>>
<<CASE.EVC.p_IdPortafolio>>
<<CASE.EVC.p_IdCliente>>
GROUP BY EVC."IdCliente",EVC."IdPortafolio",EVC."IdInstrumento",EVC."IdMoneda";
