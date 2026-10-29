SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" 
AS 
SELECT
	"TipoBloqueoSocioDeNegocio_$0"."IdTipoBloqueo" ,
	"TipoBloqueoSocioDeNegocio_$0"."IdTipoBloqueo"	 AS "CodigoTipoBloqueo",
	"TipoBloqueoSocioDeNegocio_$0"."NombreTipoBloqueo",
	"TipoBloqueoSocioDeNegocio_$0"."IdEmpresa",
	"TipoBloqueoSocioDeNegocio_$0"."IdAccion",
	"TipoBloqueoSocioDeNegocio_$0"."IdTipoEntidad",
	"TipoBloqueoSocioDeNegocio_$0"."IdEstado",
	"TipoBloqueoSocioDeNegocio_$0"."ComentarioPorDefecto"
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "TipoBloqueoSocioDeNegocio_$0" WITH READ ONLY;
--LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" AS "TipoSocioDeNegocio_$1" ON ( "TipoSocioDeNegocio_$1"."CodigoTipoSocioDeNegocio" = "TipoBloqueoSocioDeNegocio_$0"."CodigoTipoSocioDeNegocio" ) ) WITH READ ONLY;
	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
	

	