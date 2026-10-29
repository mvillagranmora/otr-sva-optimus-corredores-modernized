SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" AS 

SELECT
G."IdGrupoDireccion" 			as "IdGrupo",
G."IdDireccionElectronica"  	as "IdDireccionElectronica",
G."IdTipoDestinatario"	  	as "IdTipoDestinatario",
D."Direccion"					as "Direccion"
FROM 
"BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" G
JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" D ON D."IdDireccionElectronica" = G."IdDireccionElectronica"
UNION ALL
select 
"IdDireccionElectronica" as "IdGrupo",
"IdDireccionElectronica" as "IdDireccionElectronica",
'PARA'					 as "IdTipoDestinatario",
"Direccion"				 as "Direccion"
 from "BSCL.SocioNegocio.BD::PER.DireccionElectronica";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 