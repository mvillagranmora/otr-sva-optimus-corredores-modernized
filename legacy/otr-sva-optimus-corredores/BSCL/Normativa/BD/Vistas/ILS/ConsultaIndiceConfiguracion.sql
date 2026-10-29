SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndiceConfiguracion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndiceConfiguracion" 
AS

SELECT    IndiceConfiguracion."IdIndice"
         ,IndiceConfiguracion."IdConcepto"  
         ,Concepto."NombreConcepto"     
         ,IndiceConfiguracion."IdEmpresa"   
         ,Posicion."NombrePosicionFraccion"     
         ,IndiceConfiguracion."Factor"  
         ,IndiceConfiguracion."Operacion"                   
         ,IndiceConfiguracion."Estado"      
FROM "BSCL.Normativa.BD::ILS.IndiceConfiguracion" IndiceConfiguracion
LEFT JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto" Concepto
    ON IndiceConfiguracion."IdConcepto" = Concepto."IdConcepto"
LEFT JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaPosicionFraccion" Posicion
    ON IndiceConfiguracion."Posicion"  = Posicion."IdPosicionFraccion"  ;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndiceConfiguracion" TO BCS_DESARROLLADOR WITH GRANT OPTION;commit
