SET SCHEMA "BSCL";
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizaciones";

CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizaciones" ( "IdTipoCentralizacion",
	 "NombreTipoCentralizacion",
	 "IdTipoContabilidad",
	 "Estado" ) AS SELECT
	 CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" AS "IdTipoCentralizacion",
	 CTB_itzCentralizacionTipo."Nombre" AS "NombreTipoCentralizacion",
	 CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" AS "IdTipoContabilidad",
	 CTB_itzContabilidadTipoCentralizacionTipo."Estado" AS "Estado" 
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo 
INNER JOIN "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" AS CTB_itzCentralizacionTipo ON CTB_itzCentralizacionTipo."IdTipoCentralizacion" = CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarTipoCentralizaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  