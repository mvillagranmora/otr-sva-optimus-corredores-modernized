SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica" 

AS SELECT
	 "EstadoSocioDeNegocio_$0"."IdEstado" ,
	 "Estado_$1"."Nombre" 
FROM "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS "EstadoSocioDeNegocio_$0" 
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" ON "Estado_$1"."IdEstado" = "EstadoSocioDeNegocio_$0"."IdEstado" AND "EstadoSocioDeNegocio_$0"."NombreEntidad" = 'DIRECCIONELECTRONICA';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 