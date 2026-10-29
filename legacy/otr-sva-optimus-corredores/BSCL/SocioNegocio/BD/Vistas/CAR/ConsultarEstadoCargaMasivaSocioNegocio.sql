SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarEstadoCargaMasivaSocioNegocio"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarEstadoCargaMasivaSocioNegocio" As  

SELECT "EstadoSocioNegocio_$0"."IdEstadoSocioDeNegocio", 
       "EstadoSocioNegocio_$0"."IdEstado", 
       "EstadoSocioNegocio_$0"."NombreEntidad", 
       "Estado_$1"."Nombre" AS "NombreEstado" 
FROM  ("BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS "EstadoSocioNegocio_$0" 
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$1" 
     ON ("Estado_$1"."IdEstado" = "EstadoSocioNegocio_$0"."IdEstado"));

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarEstadoCargaMasivaSocioNegocio" TO BCS_DESARROLLADOR WITH GRANT OPTION;