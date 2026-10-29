
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.SolicitudCircular"; 


CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.SolicitudCircular" AS

select 
	"SC"."IdSolicitudCircular",
	"SC"."IdTipoCircular",
	"SC"."FechaIngresoSolicitud",
	"SC"."FechaVencimientoSolicitud",
	"SC"."IdUsuario",
	COALESCE(ESTR."Nombre",'') AS "EstadoSolicitud",
	"SC"."Observaciones",
	"SC"."IdTipoDocumento",
	"SC"."IdDocumento",
	--"SC"."IdCliente" as "Cliente",
	
	(select "DOC"."DocumentoIdentidad" 
	from "BSCL.SocioNegocio.BD::CLI.Cliente" as "CLI" inner join
	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "PER" on ("CLI"."IdPersona"="PER"."IdPersona") inner join
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" on ("DOC"."IdDocumentoIdentidad" = "PER"."IdDocumentoIdentidad" )
	where  "CLI"."IdCliente"="SC"."IdCliente" 
	) as  "Rut",
	(select "PER"."NombreCompleto" 
	from "BSCL.SocioNegocio.BD::CLI.Cliente" as "CLI" inner join
	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as "PER" on ("CLI"."IdPersona"="PER"."IdPersona") inner join
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" on ("DOC"."IdDocumentoIdentidad" = "PER"."IdDocumentoIdentidad" )
	where  "CLI"."IdCliente"="SC"."IdCliente" 
	) as  "Cliente",

	"SC"."FechaInicio",
	"SC"."FechaFin",
	"SC"."ListaRut",
	"SC"."ListaRutAut",
	"SC"."ListaNemo",
	"SC"."Periodicidad",
	"SC"."RutaArchivo",
	"SC"."NombreArchivo",
	"SC"."NumeroRegistros",
    CASE 
        WHEN "SC"."IdEstado" = 'PEN' AND BP."idEstado" = 'ERR' THEN CAST(BP."IdPrograma" AS VARCHAR)
        ELSE CAST("SC"."IdProceso" AS VARCHAR)
    END AS "IdProceso", 
   	"DOC"."Esquema",
	"DOC"."Version",
	"USU"."CodigoUsuario",
	"SC"."EsPauta",
    CASE 
        WHEN "SC"."IdEstado" = 'PEN' AND BP."idEstado" = 'ERR' THEN BP."Resultado"
        ELSE "SC"."RutaLog"
    END AS "RutaLog",
    "SC"."NumeroRegistrosLog",
	"SC"."FechaIngreso",
	"SC"."FechaCreacion",
	"SC"."FechaModificacion",
    CASE 
        WHEN "SC"."IdEstado" = 'PEN' AND BP."idEstado" = 'ERR' THEN 'ERR'
        ELSE "SC"."IdEstado"
    END AS "IdEstado"
from "BSCL.Normativa.BD::NOR.SolicitudCircular" as "SC" 
LEFT JOIN "BSCL.Normativa.BD::DOC.Documento" AS "DOC" ON "SC"."IdDocumento" = "DOC"."IdDocumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" as "USU" 
on "SC"."IdUsuario" = "USU"."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosPrograma" BPP ON (
    BPP."NombreParametro" = 'IdSolicitudCircular' 
    AND BPP."Valor" = CAST("SC"."IdSolicitudCircular" AS VARCHAR)
)
LEFT JOIN "BSCL.Plataforma.BD::BAT.Programa" BP ON (BPP."IdPrograma" = BP."IdPrograma")
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarEstadoNormativa" ESTR
 ON (
    ESTR."NombreEntidad" = 'SOLICITUD-CIRCULAR' AND 
    ESTR."IdEstado" = CASE 
                        WHEN "SC"."IdEstado" = 'PEN' AND BP."idEstado" = 'ERR' THEN 'ERR' 
                        ELSE "SC"."IdEstado" 
                        END
)
WHERE "SC"."Borrado"='N'
order by "SC"."IdSolicitudCircular" desc;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.SolicitudCircular" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 




