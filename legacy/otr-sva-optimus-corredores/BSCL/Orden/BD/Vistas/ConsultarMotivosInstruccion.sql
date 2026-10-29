SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarMotivosInstruccion" ;
GO

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarMotivosInstruccion" AS

	SELECT "IdMotivo", "Motivo", "IdEstado", "Tipo", "IdTipoCanalIngreso", "Activo"
	FROM "BSCL.Orden.BD::ORD.InstruccionMotivo"
    ORDER BY  "Motivo";
    
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarMotivosInstruccion"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO