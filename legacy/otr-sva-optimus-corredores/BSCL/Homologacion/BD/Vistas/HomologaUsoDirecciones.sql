SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaUsoDirecciones";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaUsoDirecciones" as
SELECT
	CLI."IdPersona" AS per_id, -- int] NOT NULL, ***
	POR."IdPortafolio" AS CUENTA, -- int] NOT NULL, ***
	0 AS fil_id, -- int] NOT NULL,
	DF."IdDireccionFisica" AS dir_id, -- int] NOT NULL,****
	DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
	'S' AS udi_particular, -- char](1) NOT NULL,
	' ' AS udi_comercial, -- char](1) NOT NULL,****
	' ' AS udi_legal, -- char](1) NOT NULL,****
	' ' AS udi_corresp, -- char](1) NOT NULL,
	' ' AS udi_mail_corr, -- char](1) NOT NULL,****
	' ' AS udi_mail_dte -- char](1) NOT NULL,
from "BSCL.Portafolio.BD::POR.Portafolio" POR
JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'PART'
UNION ALL
SELECT
	CLI."IdPersona" AS per_id, -- int] NOT NULL, ***
	POR."IdPortafolio" AS CUENTA, -- int] NOT NULL, ***
	0 AS fil_id, -- int] NOT NULL,
	DF."IdDireccionFisica" AS dir_id, -- int] NOT NULL,****
	DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
	' ' AS udi_particular, -- char](1) NOT NULL,
	'S' AS udi_comercial, -- char](1) NOT NULL,****
	'S' AS udi_legal, -- char](1) NOT NULL,****
	' ' AS udi_corresp, -- char](1) NOT NULL,
	' ' AS udi_mail_corr, -- char](1) NOT NULL,****
	' ' AS udi_mail_dte -- char](1) NOT NULL,
from "BSCL.Portafolio.BD::POR.Portafolio" POR
JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'COME'
UNION ALL
SELECT
	CLI."IdPersona" AS per_id, -- int] NOT NULL, ***
	POR."IdPortafolio" AS CUENTA, -- int] NOT NULL, ***
	0 AS fil_id, -- int] NOT NULL,
	DF."IdDireccionElectronica" AS dir_id, -- int] NOT NULL,****
	DF."FechaCreacion" AS udi_fec_inicio, -- datetime] NOT NULL,
	' ' AS udi_particular, -- char](1) NOT NULL,
	' ' AS udi_comercial, -- char](1) NOT NULL,****
	' ' AS udi_legal, -- char](1) NOT NULL,****
	' ' AS udi_corresp, -- char](1) NOT NULL,
	'S' AS udi_mail_corr, -- char](1) NOT NULL,****
	' ' AS udi_mail_dte -- char](1) NOT NULL,
from "BSCL.Portafolio.BD::POR.Portafolio" POR
JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DF ON PER."IdPersona" = DF."IdPersona" AND DF."CodigoProposito" = 'CORR'
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaUsoDirecciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec