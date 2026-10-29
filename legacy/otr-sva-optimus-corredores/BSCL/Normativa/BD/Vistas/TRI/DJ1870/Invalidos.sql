SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Invalidos" (
    "CodigoValidacion"
    ,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
    ,"Numero"									
	,"DatosPersonaRut"							
	,"DatosPersonaDV"							
	,"DatosPersonaIndentificadorExtranjero"		
	,"DatosPersonaPaisOtorganteIdentificador"	
	,"DatosPersonaNombreRazonSocial"				
	,"TipoTransaccionDJ"							
	,"ValoresTransados"							
	,"FechaTransaccionDia"						
	,"FechaTransaccionMes"						
	,"FechaTransaccionAnio"						
	,"DocumentoTipoDocumento"					
	,"DocumentoNumero"							
	,"CodigoMonedaTransada"						
	,"MontoCompras"								
	,"MontoVentas"								
	,"TipoCambio"					
	,"TipoDocumento"
	,"NumeroDocumento"
	,"FechaDocumento"
	,"IdBaseDocumento"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoTransaccion"
	,"IdTipoOperacion"
	,"IdTipoProducto"
) AS
SELECT b."CodigoValidacion", a.*
    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Datos" a
    INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b
    ON a."IdDocumento"=b."IdDocumento"
    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c
    ON b."ID"=c."IdResumenValidacion"
    WHERE a."ID"=c."IdDetalleDocumento"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Invalidos" TO bcs_desarrollador WITH GRANT OPTION;