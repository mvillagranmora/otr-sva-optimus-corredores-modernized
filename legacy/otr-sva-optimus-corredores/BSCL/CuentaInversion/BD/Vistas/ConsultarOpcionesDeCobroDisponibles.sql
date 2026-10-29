SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobroDisponibles"; 
 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobroDisponibles"  
AS
SELECT
	D."IdDetalleIndicador",
    D."IdTipoIndicador",
    D."DetalleIndicador", 
    D."Codigo",
    D."IdCarga",  
    TI."CodigoTipoIndicador", 
    TI."IdEmpresa"
FROM "BSCL.Plataforma.BD::IND.DetalleIndicador" D 
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI 
ON TI."IdTipoIndicador" = D."IdTipoIndicador"
WHERE TI."CodigoTipoIndicador" = 'COBROCUST';
     

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarOpcionesDeCobroDisponibles" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
