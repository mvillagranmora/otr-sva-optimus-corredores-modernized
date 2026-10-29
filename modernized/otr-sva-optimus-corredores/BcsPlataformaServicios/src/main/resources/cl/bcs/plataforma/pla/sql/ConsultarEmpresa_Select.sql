SELECT
    EMP."IdEmpresa",
    COD."CodigoCorredora" as "Codigo",
    EMP."Nombre",
    EMP."IdEstado",
    EMP."IdPais",
    EMP."IdSocioDeNegocio",
    SOC."IdContraparte",
    SOC."IdCliente",
    PER."IdPersona",
    PER."FechaIngreso",
    PER."FechaCreacion",
    CORR."IdCorredora",
    PER."FechaModificacion",
    PER."IdDocumentoIdentidad",
    IDENT."DocumentoIdentidad",
    CASE PER."Tipo"
        WHEN 'J' THEN PER."RazonSocial"
        ELSE PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno"  
    END                                  as "NombreCompleto",
    DOC1."DocumentoIdentidad"         as "DocumentoIdentidadRepresentante",
    ELE."Direccion"                     as "DireccionElectronicaPrincipal",
    FIS."Calle"                         as "DireccionFisicaPrincipalCalle",
    FIS."Numero"                         as "DireccionFisicaPrincipalNumero",
    FIS."Departamento"                 as "DireccionFisicaPrincipalDepartamento",
    FIS."Sector"                         as "DireccionFisicaPrincipalSector",
    CIU."NombreCiudad"                 as "DireccionFisicaPrincipalNombreCiudad",
    DIV."NombreDivisionPais"             as "DireccionFisicaPrincipalNombreDivisionPais",
    TEL."Numero"                         as "TelefonoPrincipal",
    PAIS."CodigoPais",
    PAIS."NombrePais",
    CNT."CodigoInscripcion"
FROM "BSCL.Plataforma.BD::PLA.Empresa"     as EMP
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC on EMP."IdSocioDeNegocio" =SOC."IdSocioDeNegocio"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER on PER."IdPersona"=SOC."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER1 on PER."IdPersonaRepresentante" = PER1."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC1 on PER1."IdDocumentoIdentidad" = DOC1."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" ELE on PER."IdDireccionElectronicaPrincipal" = ELE."IdDireccionElectronica"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" FIS on PER."IdDireccionFisicaPrincipal" = FIS."IdDireccionFisica"
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU on FIS."IdCiudad" = CIU."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIV on FIS."IdDivisionPais" = DIV."IdDivisionPais"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL on PER."IdTelefonoPrincipal" = TEL."IdTelefono"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" CORR on CORR."IdPersona" = SOC."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON EMP."IdPais" = PAIS."IdPais"
LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" as CNT on CNT."IdContraparte" = SOC."IdContraparte"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS COD ON CORR."IdCorredora" = COD."IdCorredora"
WHERE COD."IdBolsa" = 'BCS'
AND EMP."IdEmpresa" = :IdEmpresa;