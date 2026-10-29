SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica" AS 
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
		,"Nemotecnico"
		,"MontoPatrimonio"
		,"IdMoneda"
		,"IdMonedaSistema"
		,"Vigencia"
		,"MontoPatrimonioAfectoComision"
		,"PorcentajeCobroComision"
		,"MontoComision"
		,"IdEstadoComision"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja" 
WHERE "TipoFechaConsulta" = 'LIQ';

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
