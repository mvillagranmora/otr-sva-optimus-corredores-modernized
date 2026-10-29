SET SCHEMA "BSCL"; 
  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoInstrumentoContratoCobroPeriodico"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoInstrumentoContratoCobroPeriodico" As

Select 
        "IdTipoInstrumento",
        "Codigo", 
        "Nombre", 
        "Descripcion", 
        "IdEstado",
        "IdClasificacionTipoInstrumento"
From "BSCL.Plataforma.BD::INS.TipoInstrumento";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoInstrumentoContratoCobroPeriodico" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
