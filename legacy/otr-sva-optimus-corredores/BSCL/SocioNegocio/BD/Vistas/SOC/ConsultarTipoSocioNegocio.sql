SET SCHEMA "BSCL";
---BSCL.SocioNegocio.BD::SOC.Servicios.ConsultarTipoSocioNegocio

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" ( "IdTipoSocioDeNegocio",
	 "CodigoTipoSocioDeNegocio",
	 "NombreTipoSocioDeNegocio" ) AS SELECT
	 "TipoSocioDeNegocio_$0"."IdTipoSocioDeNegocio" ,
	 "TipoSocioDeNegocio_$0"."IdTipoSocioDeNegocio" AS "CodigoTipoSocioDeNegocio" ,
	-- "TipoSocioDeNegocio_$0"."CodigoTipoSocioDeNegocio" ,
	 "TipoSocioDeNegocio_$0"."NombreTipoSocioDeNegocio" 
FROM "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" AS "TipoSocioDeNegocio_$0";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
