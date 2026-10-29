SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" 
AS 


SELECT  Mo."IdModalidadDePago",
		Mo."Nombre",
		MM."IdMedioDePago",
		Mo."IdEstado"
  FROM "BSCL.Plataforma.BD::PAG.ModalidadDePago" as Mo
 INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" as MM ON MM."IdModalidadDePago" = Mo."IdModalidadDePago";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
