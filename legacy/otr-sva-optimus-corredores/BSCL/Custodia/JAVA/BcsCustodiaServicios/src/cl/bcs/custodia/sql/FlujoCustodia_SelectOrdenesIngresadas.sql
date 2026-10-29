/********************************************************** 
Proposito	: Recuperar información de órdenes ingresadas
			  para el flujo de custodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

SELECT
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
--	:p_out_KeyFlujoCustodia AS "KeyFlujoCustodia",
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
--	I."ItemCalculo",
--	I."ItemOrigen",
--	:p_FechaSaldo AS "FechaEjecucion",
	OI."IdClasificacionTipoInstrumento",
	OI."IdTipoInstrumento",
	OI."IdInstrumento",
	OI."IdEmisor",
	OI."Nemotecnico",
	'' AS "NemoSVS",
	OI."IdMonedaOrden" AS "IdMonedaInstrumento",
	OI."IdTipoReajuste",
	OI."FechaEmision" AS "FechaEmisionInstrumento",
	OI."FechaVencimiento" AS "FechaVencimientoInstrumento",
	'' AS "CodigoIFRS",
	OI."EsFungible",
	'' AS "TipoCarteraCorredor",
	OI."IdBoveda" AS "IdBoveda",
	OI."IdBolsa" as "IdBolsa",
	OI."IdTipoOperacion",
	TO_BIGINT(0) AS "IdMesa",
	OI."Accion",
	--OI."FechaLiquidacion",
	CASE WHEN OI."Accion" = 'COMPRA' THEN OI."FechaLiquidacion"  ELSE :FechaMinima END AS "FechaLiquidacion",
	OI."CantidadDisponible" AS"Cantidad",
	OI."ValorRescateDisponible" AS "ValorRescate",
	OI."PrecioOrden" AS "PrecioCompra",
	0.0 AS "TasaCompra",
	OI."MontoDisponible" AS "MontoATasaCompra",
	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",
	
	OI."IdCliente",
	OI."IdPortafolio",
--	(CASE WHEN OI."FechaLiquidacion" >= :v_FechaMinima THEN OI."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
	OI."IdOrden" as "IdRegistro",
	TO_BIGINT(0) as "FolioRegistro",
	OI."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", --CASE WHEN OI."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
	OI."Accion",
	'' AS "IdRegimenFondoMutuo",
	'' AS "GrupoRegimen",
	TO_DATE('0001-01-01') AS "FechaOrigenCompra"
FROM "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia" as OI
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU
		ON ((MZU."IdCliente" IS NULL OR OI."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR OI."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR OI."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR OI."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR OI."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR OI."IdEmisor" = MZU."IdEmisor")
			AND (MZU."IdTipoReajuste" IS NULL OR OI."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR OI."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR OI."IdBoveda" = MZU."IdBoveda")
		   )
WHERE OI."CantidadDisponible" > 0
	--AND OI."FechaLiquidacion" <= :FechaMaxima
	AND OI."FechaLiquidacion" <= CASE WHEN OI."Accion" = 'COMPRA' THEN :FechaMaxima ELSE OI."FechaLiquidacion" END
	AND OI."IdEmpresa" = :IdEmpresa
	AND MZU."IdTrabajo" = :IdTrabajo
;
