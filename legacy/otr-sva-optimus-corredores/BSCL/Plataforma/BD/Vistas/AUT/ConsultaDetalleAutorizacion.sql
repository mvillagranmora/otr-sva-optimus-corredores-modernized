SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultaDetalleAutorizacion"; 
GO
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultaDetalleAutorizacion"  as

select 
	"IdEventoDetalle", 
	"IdEvento",
	"NombreCampo",
	"TipoDato", 
	"Valor",
	"Clasificacion"
	FROM  "BSCL.Plataforma.BD::AUT.EventoDetalle"  AS EventoDetalle;
		

GO
GRANT SELECT ON  "BSCL.Plataforma.BD.Vistas::AUT.ConsultaDetalleAutorizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO