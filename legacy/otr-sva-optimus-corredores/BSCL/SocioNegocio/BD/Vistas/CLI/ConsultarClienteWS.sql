SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteWS"; 
GO 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteWS"  AS 
SELECT 
    CLIENTE.*,
    ESTCLIENTE."Nombre" as "NombreEstado"
FROM (
    SELECT 
            -- Datos de Personas
            PER."IdPersona",
            PER."IdDocumentoIdentidad",
            PER."Tipo",
            PER."NombreCompleto",
            PER."ApellidoPaterno",
            PER."ApellidoMaterno",
            PER."RazonSocial",
            PER."IdGiroPrincipal",
            GIR."Nombre" AS "NombreGiro",
            PER."IdPaisNacimiento",
            PAI."NombrePais" AS "NombrePaisNacimiento",
            PER."FechaNacimiento",
            PER."EsExtranjero",
            PER."EstadoCivil",
            PER."CodigoTipoCargoEmpleado" AS "Cargo",
            CARG."NombreTipoCargoEmpleado" AS "NombreCargo",
            PER."IdPersonaEmpleador" as "Empleador",
            PER."IdProfesion",
            PRO."Nombre" AS "NombreProfesion",
            PER."DocumentoIdentidad",
            PER."TipoDocumentoIdentidad",
            PER."IdPersonaRepresentante",
            CASE PER."Sexo" 
                WHEN 'M' THEN 'MASCULINO'
                WHEN 'F' THEN 'FEMENINO'
                ELSE PER."Sexo" 
            END AS "Sexo",
            COALESCE("SEG"."NombreSegmentoCliente",'') as "NombreSegmentoCliente",
            CASE CLI."IdEstado" 
                WHEN 'ACT' THEN CASE "CLI"."EsEnrolado"
                WHEN 'P' THEN 'PEN'
                WHEN 'N' THEN 'PEN' 
                ELSE 'ACT' END ELSE 'BLO' 
            END AS "IdEstado",
            CLI."IdCliente",
            CLI."IdCategoriaInversionista",
            CASE EMPL."Tipo" 
                WHEN 'N' THEN "EMPL"."Nombres" ||' '|| EMPL."ApellidoPaterno" 
                ELSE EMPL."RazonSocial" 
            END  AS "NombreEmpleador",
            DIR."NombreLargo" AS "DireccionFisicaEmpleador",
            TEL."Numero" AS "TelefonoPrincipalEmpleador",
            COALESCE(PER."RegimenConyugal",'') as "IdTipoRegimenConyugal",
            COALESCE(TRC."Nombre",'') as "RegimenConyugal",     
            PER."FechaInicioActividad",
            CLI."IdPersonaEjecutivo",
            CASE EJE."Nombres" WHEN '' 
                THEN EJE."RazonSocial" 
                ELSE EJE."Nombres"||' '|| EJE."ApellidoPaterno"||' '|| EJE."ApellidoMaterno" 
            END AS "NombreCompletoEjecutivo",
            EMP."CodigoEmpleado",
            EMPEJE."CodigoEmpleado" AS "CodigoEmpleadoEjecutivo",
            PER."GlobalLegalEntity",
            PER."IdTipoCanalIngresoCorredor",
            CASE
            	WHEN PER."Tipo" = 'N' THEN PER."CodigoRangoRenta"
            	ELSE ''
            END AS "CodigoRangoRenta",
            CASE
            	WHEN PER."Tipo" = 'N' THEN PER."CodigoPropositoTransaccion"
            	ELSE ''
            END AS "CodigoPropositoTransaccion",
            CASE 
            	WHEN PER."Tipo" = 'J' THEN PER."CodigoRangoNivelVenta"
            	ELSE ''
            END AS "CodigoRangoNivelVenta"
    FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER    
    INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON ( PER."IdPersona" = CLI."IdPersona" )
    LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON ( PER."IdPersona" = EMP."IdPersona" )
    LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" AS PRO ON ( PER."IdProfesion" = PRO."IdProfesion" )
    LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS CARG ON (PER."CodigoTipoCargoEmpleado" = CARG."CodigoTipoCargoEmpleado")
    LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON (PER."IdPaisNacimiento" = PAI."IdPais")
    LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" AS GIR ON (PER."IdGiroPrincipal" = GIR."IdGiro")
    LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS EMPL ON (PER."IdPersonaEmpleador" = EMPL."IdPersona")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS TEL ON (TEL."IdTelefono" = EMPL."IdTelefonoPrincipal")
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON (DIR."IdDireccionFisica" = EMPL."IdDireccionFisicaPrincipal")
    LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS  SEG ON (SEG."IdSegmentoCliente" = CLI."IdSegmentoCliente")    
    LEFT JOIN "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" AS TRC ON (TRC."IdTipoRegimenConyugal" = PER."RegimenConyugal")    
    LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS EJE ON(CLI."IdPersonaEjecutivo" = EJE."IdPersona") 
    LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMPEJE ON ( EJE."IdPersona" = EMPEJE."IdPersona" )
     WHERE CLI."Borrado" = 'N') CLIENTE
LEFT JOIN 
    "BSCL.Plataforma.BD::PLA.Estado" ESTCLIENTE 
        ON (ESTCLIENTE."IdEstado" = CLIENTE."IdEstado");
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteWS" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
