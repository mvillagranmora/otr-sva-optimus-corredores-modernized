SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoClienteCmf"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoClienteCmf" AS
SELECT  
	  DI."IdDetalleIndicador"
	, DI."DetalleIndicador"
	, DI."IdTipoIndicador"
	, DI."Codigo"
	, TI."CodigoTipoIndicador"
	, TI."NombreTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DI
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI
		ON TI."IdTipoIndicador" = DI."IdTipoIndicador" 
		
WHERE TI."CodigoTipoIndicador" = 'TCLICMF' OR  TI."CodigoTipoIndicador" = 'TCLICMFJ';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoClienteCmf" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 