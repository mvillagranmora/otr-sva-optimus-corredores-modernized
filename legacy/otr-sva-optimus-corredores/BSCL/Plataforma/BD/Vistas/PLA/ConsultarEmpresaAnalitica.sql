set schema "BSCL";

drop view "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresaAnalitica"; 

create view "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresaAnalitica" as
SELECT EMP."IdEmpresa",
	"Codigo",
 	"Nombre",
	EMP."IdEstado",
	"IdPais",
	EMP."IdSocioDeNegocio",
	"DocumentoIdentidad",
	CONF."Valor" AS "FechaSistema"
FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
left outer join "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC on EMP."IdSocioDeNegocio" =SOC."IdSocioDeNegocio"
left outer join "BSCL.SocioNegocio.BD::PER.Persona" as PER on PER."IdPersona"=SOC."IdPersona"
left JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON CONF."CodigoParametro" = 'PLA.FechaSistema'
;

grant select on "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresaAnalitica" to BCS_DESARROLLADOR with grant option;