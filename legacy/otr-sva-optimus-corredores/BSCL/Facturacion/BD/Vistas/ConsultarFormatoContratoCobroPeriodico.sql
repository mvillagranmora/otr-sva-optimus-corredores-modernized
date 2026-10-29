SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFormatoContratoCobroPeriodico";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFormatoContratoCobroPeriodico" As 

Select 
       "IdFormatoContratoCobroPeriodico",
       "Codigo", 
       "Nombre",
       "IdEstado"
From "BSCL.Facturacion.BD::FAC.FormatoContratoCobroPeriodico";

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFormatoContratoCobroPeriodico" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
