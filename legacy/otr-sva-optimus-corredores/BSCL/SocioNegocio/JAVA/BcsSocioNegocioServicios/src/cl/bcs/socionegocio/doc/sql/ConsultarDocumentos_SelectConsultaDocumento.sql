WITH CTE AS (
SELECT
	TPO."IdTipoDocumento"																					AS "IdTipoDocumento",
	TPO."NombreDocumento"																					AS "NombreDocumento",
	TPO."Glosa"																								AS "Glosa",
    TO_BIGINT(COALESCE(ANX."NumeroFirmasMinimas", TPO."NumeroFirmasMinimas")) 								AS "NumeroFirmasMinimas",
    TO_BIGINT(COALESCE(ANX."NumeroFirmasMaximas", TPO."NumeroFirmasMaximas")) 								AS "NumeroFirmasMaximas",
	TPO."SolicitarFechaFirma"																				AS "SolicitarFechaFirma",
    TPO."FechaFirma"																						AS "FechaFirma",
	TPO."ValidaFechaVigencia"																				AS "ValidaFechaVigencia",
	ANX."IdAnexo"																							AS "IdAnexo",
	DOC."IdCliente"																							AS "IdCliente",
	DOC."IdDocumento"																						AS "IdDocumento",
	IFNULL(ANX."IdEstado",'PEN') 																			AS "IdEstado",
	CASE WHEN ANX."ArchivoAdjunto" IS NULL THEN 'N' ELSE 'S' END 											AS "TieneAdjunto", 
	EST."Nombre" 																							AS "NombreEstado",
	ANX."FechaVigencia"																						AS "FechaVigencia",
	COALESCE(CSN."Valor", 'S') 																				AS "PermiteFirmarSinDocumento",
	COALESCE(TPO."IdClase",1) 																				AS "IdClaseDocumento",
	TPO."TipoDocumentoNormativa"																			AS "TipoDocumentoNormativa",
    ANX."IdTipoCanalIngreso"																				AS "IdTipoCanalIngreso",
    ANX."FechaIngreso"																						AS "FechaIngreso",
    COALESCE(ANX."NumeroFirmasMinimas", TPO."NumeroFirmasMinimas") 											AS "CantidadMinimaFirmasRequeridas",
    CASE "PER"."Tipo"
        WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"
        ELSE "PER"."RazonSocial"
        END  																								AS "AgenteNombreCompleto",
    ANX."FechaActivacion" 																					AS "FechaActivacion",
    TPO."CodigoArchivo" 																					AS "CodigoArchivo",
    AC."Descripcion" 																						AS "NombrePlantillaContrato",
    TPO."ValidaClienteFull" 																				AS "ValidaClienteFull",
    (SELECT  STRING_AGG(cnl."IdTipoCanalIngreso", ',')   
     FROM "BSCL.Plataforma.BD::DOC.CanalesDocumento" cnl 
     WHERE cnl."IdTipoDocumento" = TPO."IdTipoDocumento"  GROUP BY cnl."IdTipoDocumento" ) 					AS "CanalesIngreso",
    CFUMC."FechaUltimaModificacion" 																		AS "FechaModificacion",
    USU."CodigoUsuario" 																					AS "UsuarioFirmante",
    CASE WHEN DOC."DocumentoSubido" IS NULL THEN 'N' ELSE DOC."DocumentoSubido" END							AS "DocumentoSubido",
    CUC."NombreCompleto"																					AS "FirmadoPor",
    USUDESC."CodigoUsuario"																					AS "UsuarioSolicitante",
    CUCDESC."NombreCompleto"																				AS "SolicitadoPor"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento" AND DOC."IdCliente" = :p_IdCliente
LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = IFNULL(ANX."IdEstado",'PEN')
LEFT JOIN "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" CSN ON TPO."IdEmpresa" = CSN."IdEmpresa" AND CSN."CodigoParametro" = 'DOC.FirmarSinArchivo'
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = DOC."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersonaEjecutivo"
LEFT JOIN "BSCL.Plataforma.BD::CTT.ArchivosContrato" AC ON (AC."CodigoArchivo" = TPO."CodigoArchivo" AND AC."IdEmpresa" = TPO."IdEmpresa")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionCliente" CFUMC ON CLI."IdCliente" = CFUMC."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario"    USU ON  (ANE."IdUsuario" = USU."IdUsuario")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" CUC ON USU."IdUsuario" = CUC."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USUDESC ON ANX."IdUsuarioDescargaDocumento" = USUDESC."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" CUCDESC ON USUDESC."IdUsuario" = CUCDESC."IdUsuario"
WHERE
	TPO."IdEmpresa" = :p_IdEmpresa
AND (TPO."IdTipoPersona" = :p_IdTipoPersona OR TPO."IdTipoPersona" = 'A')
AND (:p_IdEstadoDocumento = '' OR IFNULL(ANX."IdEstado",'PEN') = :p_IdEstadoDocumento)
ORDER BY TPO."IdTipoDocumento")

SELECT  
	"IdTipoDocumento","NombreDocumento","Glosa","NumeroFirmasMinimas","NumeroFirmasMaximas","SolicitarFechaFirma","FechaFirma","ValidaFechaVigencia","IdAnexo","IdCliente","IdDocumento","IdEstado","TieneAdjunto", 
	"NombreEstado","FechaVigencia","PermiteFirmarSinDocumento","IdClaseDocumento","TipoDocumentoNormativa","IdTipoCanalIngreso","FechaIngreso","CantidadMinimaFirmasRequeridas","AgenteNombreCompleto",
	"FechaActivacion","CodigoArchivo","NombrePlantillaContrato","ValidaClienteFull","CanalesIngreso","FechaModificacion","UsuarioFirmante","DocumentoSubido", "FirmadoPor", "UsuarioSolicitante", "SolicitadoPor"
FROM CTE 
GROUP BY 
	"IdTipoDocumento","NombreDocumento","Glosa","NumeroFirmasMinimas","NumeroFirmasMaximas","SolicitarFechaFirma","FechaFirma","ValidaFechaVigencia","IdAnexo","IdCliente","IdDocumento","IdEstado","TieneAdjunto", 
	"NombreEstado","FechaVigencia","PermiteFirmarSinDocumento","IdClaseDocumento","TipoDocumentoNormativa","IdTipoCanalIngreso","FechaIngreso","CantidadMinimaFirmasRequeridas","AgenteNombreCompleto",
	"FechaActivacion","CodigoArchivo","NombrePlantillaContrato","ValidaClienteFull","CanalesIngreso","FechaModificacion","UsuarioFirmante","DocumentoSubido", "FirmadoPor", "UsuarioSolicitante", "SolicitadoPor"
ORDER BY "IdTipoDocumento";
