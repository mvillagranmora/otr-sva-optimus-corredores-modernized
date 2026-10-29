SET SCHEMA "BSCL";
 GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona";
 GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
AS

---SALIDA UI
SELECT	DISTINCT 					 REL."IdPersona" AS "IdPersonaOrigen",
									 REL."IdRelacionPersona", 
									 REL."Administracion",
									 REL."IdTipoRelacion",
									 IFNULL(TREL."NombreTipoRelacion",'') AS "NombreTipoRelacion",
									 REL."FechaInicioRelacion",
									 REL."FechaFinRelacion",
									 --(CASE  TREL."EsIndefinida" WHEN 'S' THEN REL."IdEstado" ELSE 
								     --   (CASE  WHEN ("FechaFinRelacion" < CURRENT_UTCTIMESTAMP) THEN 'VCD' ELSE REL."IdEstado" END)   
								     --END) AS "IdEstado",
								     --Cflores: El vencimiento de Relaciones no es solo a Nvl de Tipo, sino que se escala desde la relacion persona
								     --modificar funcion (Obtener) y luego escalar la vigencia hasta esta vista  
								     REL."IdEstado",									 
									 ESTPER."Nombre" as "NombreEstado",
									 REL."IdPersonaDestino", ---DESTINO
									 PER."Tipo"	as "CodigoTipoPersona",
									 PER."DocumentoIdentidad" AS "DocumentoIdentidadDefecto",
									 PER."NombreCompleto",
									 PER."RazonSocial",
									 PER."Alias",    
									 PER."Nombres",
									 PER."ApellidoPaterno",
									 PER."ApellidoMaterno",    
									 PER."FechaNacimiento",
									 PER."IdPaisResidencia",
									 PER."TipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDefecto",
									 SOC."IdEmpresa",
									 SOC."IdCliente",
									 SOC."IdEmpleado",
									 SOC."IdOperador",
									 SOC."IdProveedor",
									 SOC."IdContraparte",
									 REL."PorcentajeParticipacion",
									 REL."FinalidadRelacion",--perú
									 REL."CondicionesRelacion",--perú
									 REL."IdGradoVinculacionParental",--perú
									 REL."FechaModificacion",
									 COALESCE(TREL."UsaFirmaDocumento", 'N') AS "UsaFirmaDocumento",
									 COALESCE(TREL."AccesoOptimusCliente", 'N') AS "AccesoOptimusCliente"
            FROM 
               	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
				ON (SOC."IdPersona"=PER."IdPersona")
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ObtenerRelacionPersona" REL 					
				ON (PER."IdPersona"= REL."IdPersonaDestino")
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"  TREL 
				ON (REL."IdTipoRelacion" = TREL."IdTipoRelacion" AND SOC."IdEmpresa" = TREL."IdEmpresa")
				LEFT JOIN 
					"BSCL.Plataforma.BD::PLA.Estado" ESTPER 
						ON (ESTPER."IdEstado" = REL."IdEstado");
				
GO	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
