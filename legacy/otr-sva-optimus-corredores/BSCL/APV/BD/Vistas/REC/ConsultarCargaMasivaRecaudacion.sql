
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarCargaMasivaRecaudacion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarCargaMasivaRecaudacion" AS 
	
	SELECT 
	     "IdCargaMasiva"                  
		,"Fecha"                          
		,"TipoDeposito"                   
		,"FolioCartola"                   
		,"FolioLineaCartola"              
		,"Banco"                          
		,"Cuenta"                         
		,"Monto"                          
		,"PeriodoPago"                    
		,"TipoPago"                       
		,"RUTEmpleador"                   
		,"RUTCliente"                     
		,"TipoDeAfiliado"                 
		,"Regimen"                        
		,"MensajeValidacion"              
		,"EstadoRendicion"                
		,"EstadoProceso"                  	
	FROM "BSCL.APV.BD::APV.CargaMasivaDetalleRecaudacion";

GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarCargaMasivaRecaudacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;