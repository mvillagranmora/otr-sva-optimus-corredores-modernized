SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoContratoCobroPeriodico";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarTipoContratoCobroPeriodico" As 

SELECT 
       "IdTipoContratoCobroPeriodico",
       "Codigo",
       TipConPer."Nombre",
       TipConPer."IdEstado",
       Est."Nombre" As "Estado"        
From "BSCL.Facturacion.BD::FAC.TipoContratoCobroPeriodico" As TipConPer
 Left Join "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstFac 
   On EstFac."IdEstado" = TipConPer."IdEstado" 
 Left Join "BSCL.Plataforma.BD::PLA.Estado" As Est 
   On Est."IdEstado" = EstFac."IdEstado"
Where "NombreEntidad" = 'FAC.TipoContratoCobroPeriodico';

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarTipoContratoCobroPeriodico" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
