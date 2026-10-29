SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente" AS
SELECT
	 ROP."IdTipoEtapa"
	,ROP."IdTipoCanalIngreso"
	,ROP."IdCondicionOperativa"
	,ROP."IdTipoComportamiento"
	,ROP."IdEmpresa"
	,ROP."IdEstado"
	,IND."CodigoTipoIndicador"
	,IND."GrupoIndicador"
	,IND."NombreTipoIndicador"
	,IND."ValorIndicador"
	,CLI."IdCliente"
FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo" ROP
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
	ON (IND."CodigoTipoIndicador"	= 'CCF' AND 
		IND."IdTipoEntidad"			= 'CLIENTE' AND 
		IND."IdUso"					= 'REQ')
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	ON (IND."IdCliente" = CLI."IdCliente" AND ROP."IdEmpresa" = CLI."IdEmpresa")
WHERE ROP."IdTipoEtapa" IN ('INGRESOFORWARD')
  AND ROP."IdEstado" = 'ACT'
;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarRequisitosOperativosFWDCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
