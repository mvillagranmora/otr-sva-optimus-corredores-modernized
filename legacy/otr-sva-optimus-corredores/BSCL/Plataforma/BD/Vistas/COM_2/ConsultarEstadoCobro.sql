SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarEstadoCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarEstadoCobro"  
AS

SELECT 
	estp."IdEstadoPlataformaComercial",
	estp."IdEstado",
	est."Nombre" as "NombreEstado"
	FROM "BSCL.Plataforma.BD::COM_2.EstadoPlataformaComercial" estp
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" est on est."IdEstado" = estp."IdEstado" and "NombreEntidad" = 'COBRO';


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarEstadoCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
