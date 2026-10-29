SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaLiquidacionAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaLiquidacionAnalitica" AS
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
		,"PorcentajeCobroComision"
		,"MontoComision"
		,"IdEstadoComision"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodia"
WHERE "TipoFechaConsulta" = 'LIQ';

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioCustodiaFechaLiquidacionAnalitica"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
