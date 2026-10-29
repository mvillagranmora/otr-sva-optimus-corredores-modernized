SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" 
AS 


SELECT 
		 "MedioDePago"."IdMedioDePago"
		,"MedioDePago"."TipoMedioDePago"
		,"MedioDePago"."Nombre"  
		,"MedioDePago"."UsaCuentaBancaria"  
		,"MedioDePago"."UsaTarjetaBancaria"  
		,"MedioDePago"."IdEstado"
		,"Estado"."Nombre" AS "NombreEstado"
FROM  "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON  ("Estado"."IdEstado" = "MedioDePago"."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
