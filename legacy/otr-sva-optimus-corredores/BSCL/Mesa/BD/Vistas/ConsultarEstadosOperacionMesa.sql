SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa"  
AS
	SELECT
		 "Estado"."IdEstado",
		 "Estado"."Nombre" AS "NombreEstado",
		 "EstadoSig"."IdEstado" as "IdEstadoSiguiente",
		 "EstadoSig"."Nombre" AS "NombreEstadoSiguiente",
		 "EstadoMesa"."IdTipoProducto",
		 "EstadoMesa"."NombreEntidad"
	FROM   "BSCL.Mesa.BD::MES.EstadoOperacion"  AS "EstadoMesa" 
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON "Estado"."IdEstado" = "EstadoMesa"."IdEstado" 
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EstadoSig" ON "EstadoSig"."IdEstado" = "EstadoMesa"."IdEstadoSiguiente"

	WITH READ ONLY;
    
GRANT SELECT 	ON "BSCL.Mesa.BD.Vistas::ConsultarEstadosOperacionMesa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
