SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad" AS
SELECT DivisionPais."IdPais"
     , DivisionPais."NombrePais"
     , "IdClasificacionDivisionPais1"
     , "NombreClasificacionDivisionPais1"
     , Ciudad."IdCiudad"
     , Ciudad."NombreCiudad"
     , DivisionPais."IdDivisionPais"
     , "NombreDivisionPais"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" AS DivisionPais
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad" AS Ciudad 
        ON Ciudad."IdPais" = DivisionPais."IdPais"
       AND Ciudad."IdCiudad" = DivisionPais."IdCiudad"
       AND Ciudad."IdDivisionPais" = DivisionPais."IdDivisionPais"
ORDER BY "IdClasificacionDivisionPais1";
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO