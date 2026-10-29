SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEnvioMail"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEnvioMail"  
AS
select 
"IdEmail",
"FechaCreacion",
"Remitente",
"Asunto",
"MensajeCuerpo",
"EstadoEnviado",
"FechaEnviado"
 from "BSCL.Plataforma.BD::EVT.Mail";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEnvioMail" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 