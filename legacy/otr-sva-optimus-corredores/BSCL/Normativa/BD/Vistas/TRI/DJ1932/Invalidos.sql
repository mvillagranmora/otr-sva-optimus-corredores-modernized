SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Invalidos" (
    "CodigoValidacion"
    ,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
    ,"Numero"								
	,"Mes"									
	,"RutDepositanteInversionistaPrestamista"
	,"TaxID"									
	,"Pais"									
	,"DepositosInversionesPrestamos"			
	,"RescatesDevoluciones"					
	,"InteresesPercibidas"					
	,"DividendosPercibidas"					
	,"ContratosPorDiferenciaPercibidas"		
	,"OtrasRentasPercibidas"					
	,"InteresesDevengadas"					
	,"OtrasRentasDevengadas"
	,"FechaOperacion"
	,"IdCliente"
	,"TipoOperacion"
) AS
SELECT
   	b."CodigoValidacion"
    ,a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,a."Observaciones"
    ,a."FuenteDatos"
    ,a."Numero"								
	,a."Mes"									
	,a."RutDepositanteInversionistaPrestamista"
	,a."TaxID"									
	,a."Pais"									
	,a."DepositosInversionesPrestamos"			
	,a."RescatesDevoluciones"					
	,a."InteresesPercibidas"					
	,a."DividendosPercibidas"					
	,a."ContratosPorDiferenciaPercibidas"		
	,a."OtrasRentasPercibidas"					
	,a."InteresesDevengadas"					
	,a."OtrasRentasDevengadas"
	,a."FechaOperacion"
	,a."IdCliente"
	,a."TipoOperacion"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Invalidos" to bcs_desarrollador with grant option;
