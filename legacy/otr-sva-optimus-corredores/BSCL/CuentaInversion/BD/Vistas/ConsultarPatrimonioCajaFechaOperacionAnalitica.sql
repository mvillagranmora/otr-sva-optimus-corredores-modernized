SET SCHEMA "BSCL";

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaOperacionAnalitica";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaOperacionAnalitica" AS 
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
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCaja" 
WHERE "TipoFechaConsulta" = 'ADQ';

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaOperacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
