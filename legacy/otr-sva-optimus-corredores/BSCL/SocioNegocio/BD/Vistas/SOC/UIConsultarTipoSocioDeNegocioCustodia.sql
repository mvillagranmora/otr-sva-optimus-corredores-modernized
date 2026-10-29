SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarTipoSocioDeNegocioCustodia";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarTipoSocioDeNegocioCustodia" 
AS 
	SELECT 1 AS "IdTipoSocioDeNegocioCustodia", 'Corredor' AS "TipoSocioNegocio", 'VIG' AS "IdEstado" FROM DUMMY UNION ALL
	SELECT 2 AS "IdTipoSocioDeNegocioCustodia", 'Institucional' AS "TipoSocioNegocio", 'VIG' AS "IdEstado" FROM DUMMY UNION ALL
	SELECT 3 AS "IdTipoSocioDeNegocioCustodia", 'Custodia Externa' AS "TipoSocioNegocio", 'VIG' AS "IdEstado" FROM DUMMY UNION ALL
	SELECT 4 AS "IdTipoSocioDeNegocioCustodia", 'Con Custodia' AS "TipoSocioNegocio", 'VIG' AS "IdEstado" FROM DUMMY;

GRANT SELECT ON  "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarTipoSocioDeNegocioCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
