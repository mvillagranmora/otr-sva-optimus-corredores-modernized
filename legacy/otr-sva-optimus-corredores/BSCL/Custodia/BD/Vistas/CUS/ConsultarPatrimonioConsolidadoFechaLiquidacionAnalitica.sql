SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica" AS
SELECT
		"IdCliente"
		,"IdPortafolio"
		,TO_CHAR("FechaPatrimonio",'YYYY-MM-DD') AS "FechaPatrimonio"
		,"DocumentoIdentidad"
		,"NombreCompleto"
		,"TipoAhorro"
		,"IdMatrizAgrupadorAPV"
		,"CodigoPortafolio"
		,"Proposito"
		,"PlanAPV"
		,"CategoriaAPV"
		,"IdRegimenTributario"
		,"Fondo"
		,"Serie"
		,"IdInstrumento"
		,"Nemotecnico"
		,"IdClasificacionTipoInstrumento"
		,"CantidadPatrimonio"
		,"PrecioMercadoCustodia"
		,"ValorCuota"
		,"MontoPatrimonio"
		,"IdMoneda"
		,"IdMonedaSistema"
		,"Vigencia"
		,"FechaFuente"
		,"MontoComision"
		,"IdEstadoComision"
		,"PorcentajeCobroComision"
		,"MontoPatrimonioAfectoComision"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidado"
WHERE "TipoFechaConsulta" = 'LIQ';

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
