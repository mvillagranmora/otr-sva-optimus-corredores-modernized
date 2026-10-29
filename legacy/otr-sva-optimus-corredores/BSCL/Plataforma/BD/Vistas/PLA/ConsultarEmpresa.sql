set schema "BSCL";

drop view "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"; 

create view "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" as

select
    "EMP"."IdEmpresa",
    "EMP"."Codigo",
    "EMP"."Nombre",
    "EMP"."IdEstado",
    "EMP"."IdPais",
    "EMP"."IdSocioDeNegocio",
    "CORR"."NombreCorto",

    "SOC"."IdContraparte",

    "PER"."IdPersona",
    "PER"."FechaIngreso",
    "PER"."FechaCreacion",
    "PER"."FechaModificacion",
    "PER"."IdDocumentoIdentidad",
    "PER"."DocumentoIdentidad",
    "PER"."TipoDocumentoIdentidad",
    "PER"."DocumentoIdentidadAdicional",
    "PER"."TipoDocumentoIdentidadAdicional",
    "PER"."RazonSocial",

    "CNT"."CodigoInscripcion" as "CodigoContraparte",
    "CNT"."Corredora" as "EsCorredora",

    "DOC1"."DocumentoIdentidad" as "DocumentoIdentidadRepresentante",
    "ELE"."Direccion" as "DireccionElectronicaPrincipal",
    "FIS"."Calle" as "DireccionFisicaPrincipalCalle",
    "FIS"."Numero" as "DireccionFisicaPrincipalNumero",
    "FIS"."Departamento" as "DireccionFisicaPrincipalDepartamento",
    "FIS"."Sector" as "DireccionFisicaPrincipalSector",
    "CIU"."NombreCiudad" as "DireccionFisicaPrincipalNombreCiudad",
    "DIV"."NombreDivisionPais" as "DireccionFisicaPrincipalNombreDivisionPais",
    "PAI"."NombrePais" AS "DireccionFisicaPrincipalNombrePais",
    "TEL"."Numero" as "TelefonoPrincipal"

from "BSCL.Plataforma.BD::PLA.Empresa" as "EMP"
left outer join "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as "SOC" on "EMP"."IdSocioDeNegocio" ="SOC"."IdSocioDeNegocio"
left outer join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "PER" on "PER"."IdPersona"="SOC"."IdPersona"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Corredora" AS "CORR" ON "CORR"."IdPersona"="PER"."IdPersona"
left outer join "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" as "CNT" on "CNT"."IdContraparte" = "SOC"."IdContraparte"
left outer join "BSCL.SocioNegocio.BD::PER.Persona" "PER1" on "PER"."IdPersonaRepresentante" = "PER1"."IdPersona"
left outer join "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "DOC1" on "PER1"."IdDocumentoIdentidad" = "DOC1"."IdDocumentoIdentidad"
left outer join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" "ELE" on "PER"."IdDireccionElectronicaPrincipal" = "ELE"."IdDireccionElectronica"
left outer join "BSCL.SocioNegocio.BD::PER.DireccionFisica" "FIS" on "PER"."IdDireccionFisicaPrincipal" = "FIS"."IdDireccionFisica"
left outer join "BSCL.Plataforma.BD::REG.Ciudad" "CIU" on "FIS"."IdCiudad" = "CIU"."IdCiudad"
left outer join "BSCL.Plataforma.BD::REG.DivisionPais" "DIV" on "FIS"."IdDivisionPais" = "DIV"."IdDivisionPais"
left outer join "BSCL.Plataforma.BD::REG.Pais" "PAI" on "CIU"."IdPais" = "PAI"."IdPais"
left outer join "BSCL.SocioNegocio.BD::PER.Telefono" "TEL" on "PER"."IdTelefonoPrincipal" = "TEL"."IdTelefono"
;

grant select on "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" to BCS_DESARROLLADOR with grant option;
