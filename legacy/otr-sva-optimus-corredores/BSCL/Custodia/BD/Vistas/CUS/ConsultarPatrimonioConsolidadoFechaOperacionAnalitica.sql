SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica" AS
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
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidado"
WHERE "TipoFechaConsulta" = 'ADQ';

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaOperacionAnalitica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
