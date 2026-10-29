SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones"
AS
SELECT
    TCO."IdModuloCuadratura"
	,TCO."IdCuadraturaDiariaOperaciones"
	,TCO."IdEmpresa"
	,TCO."OperacionCuadraturaDiariaOperaciones"
	,CDO."FechaEjecucion"
	,CDO."TotalOperaciones"
	,CDO."OperacionesConProblemas"
	,CDO."TipoOperacion"
	,CDO."Resultado"
FROM "BSCL.Plataforma.BD::PLA.TipoCuadraturaOperaciones" TCO
LEFT JOIN "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones" CDO ON TCO."IdCuadraturaDiariaOperaciones" = CDO."IdCuadraturaDiariaOperaciones" AND TCO."IdEmpresa" = CDO."IdEmpresa";
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO