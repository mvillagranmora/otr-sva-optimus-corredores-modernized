SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarInformacionCliente"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarInformacionCliente" AS

SELECT 
        Per."IdPersona",
        Eje."IdPersona" AS "IdPersonaEjecutivo",
        Per."DocumentoIdentidad" AS "DocumentoIdentidadCliente", 
        Per."NombreCompleto" AS "NombreCliente", 
        Per."Tipo"         AS "IdTipoPersona",
        TPer."Descripcion" AS "NombreTipoPersona", 
        COALESCE(RelPer."FechaNacimiento",Per."FechaNacimiento") AS "FechaNacimiento", 
        COALESCE(Repre."Cargo",Per."Cargo") AS "Cargo", 
        Eje."NombreCompleto" AS "NombreEjecutivo", 
        COALESCE(RepreDir."Direccion",DirElec."Direccion") AS "DireccionElectronica",
        COALESCE(RelPer."DocumentoIdentidadDefecto",Per."DocumentoIdentidad") AS "DocumentoIdentidadRepLegal",
        COALESCE(RelPer."NombreCompleto",Per."NombreCompleto") AS "NombreRepLegal"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Per
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS TPer
     ON TPer."IdTipoPersona" = Per."Tipo"
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli 
     ON Cli."IdPersona" = PER."IdPersona"
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Eje
     ON Eje."IdPersona" = Cli."IdPersonaEjecutivo"
  INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DirElec
     ON DirElec."IdDireccionElectronica" = Per."IdDireccionElectronicaPrincipal"
   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS RelPer
     ON RelPer."IdPersonaOrigen" = Per."IdPersona" AND RelPer."IdTipoRelacion" IN ('RELEEN')
   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS Repre
   	 ON RelPer."IdPersonaDestino" = Repre."IdPersona"
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" RepreDir
   	 ON RepreDir."IdDireccionElectronica" = Repre."IdDireccionElectronicaPrincipal";
    
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarInformacionCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
