SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos" (
    "ID"
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
	--,"TipoDocumento"
	--,"NumeroDocumento"
	--,"FechaDocumento"
	--,"IdBaseDocumento"
	,"IdCliente"
	--,"IdPortafolio"
	--,"TipoTransaccion"
	--,"IdTipoOperacion"
	--,"IdTipoProducto"
	,"FechaOperacion"
	,"CodPaisRenta"
	,"TipoOperacion"
) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
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
	--,a."TipoDocumento"
	----,a."NumeroDocumento"
	--,a."FechaDocumento"
	--,a."IdBaseDocumento"
	,a."IdCliente"
	--,a."IdPortafolio"
	--,a."TipoTransaccion"
	--,a."IdTipoOperacion"
	--,a."IdTipoProducto"
	,a."FechaOperacion"
	,a."CodPaisRenta"
	,a."TipoOperacion"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1932" a
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1932" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;


GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos" TO bcs_desarrollador WITH GRANT OPTION;
