SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Replica.BD.Vistas::Optimus.Sucursal"; 
 
CREATE VIEW "BSCL.Replica.BD.Vistas::Optimus.Sucursal"
AS
SELECT SUC."IdSucursal",
	 SUC."IdEmpresa",
	 SUC."CodigoSucursal",
	 SUC."NombreSucursal",
	 SUC."IdDivisionPais",
	 COALESCE(CLA."IdClasificacionDivisionPais",0) AS "IdClasificacionDivisionPais",
	 SUC."Calle",
	 SUC."Numero",
	 SUC."Depto",
	 SUC."Telefono",
	 COALESCE(DIV."NombreDivisionPais",'') AS "NombreDivisionPais",
	 COALESCE(CLA."NombreClasificacionDivisionPais",'') AS "NombreClasificacionDivisionPais",
	 SUC."EstadoSucursal" 
FROM "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIV ON SUC."IdDivisionPais"=DIV."IdDivisionPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" CLA ON CLA."IdClasificacionDivisionPais"=DIV."IdClasificacion1";

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Optimus.Sucursal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 






