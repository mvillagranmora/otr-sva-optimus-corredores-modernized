SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo"  
AS

    
SELECT 
	 "rmp"."IdRangosMontoPlazo"
	,"rmp"."IdLimite"
	,"rmp"."MontoDesde"
	,"rmp"."MontoHasta"
	,"rmp"."PlazoDesde"
    ,"rmp"."PlazoHasta"
	,"rmp"."Tasa"
    ,"rmp"."PuntosTasaMenos"
    ,"rmp"."PuntosTasaMas"
    ,"rmp"."PuntosTasaMenosAgente"
    ,"rmp"."PuntosTasaMasAgente"
    --,"rmp"."IdEstado"
	,"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',"rmp"."IdEstado","rmp"."IdEstadoAutorizacion") AS "IdEstado"
	,EST."Nombre" as "NombreEstadoRango"
    ,"l"."IdTipoLimite"
    ,"l"."IdTipoOperacion"
    ,"l"."IdTipoInstrumento"
    ,"l"."IdInstrumento"
    ,"l"."IdMoneda"
    ,("rmp"."Tasa" - "rmp"."PuntosTasaMenos") 			AS "TasaFinalMenos"
    ,("rmp"."Tasa" + "rmp"."PuntosTasaMas") 			AS "TasaFinalMas"
    ,("rmp"."Tasa" - "rmp"."PuntosTasaMenosAgente") 	AS "TasaFinalMenosAgente"
    ,("rmp"."Tasa" + "rmp"."PuntosTasaMasAgente") 		AS "TasaFinalMasAgente"
FROM "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" AS "rmp"
LEFT JOIN "BSCL.Plataforma.BD::LIM.Limite" AS "l" ON ("l"."IdLimite" = "rmp"."IdLimite")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
	ON (EST."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',"rmp"."IdEstado","rmp"."IdEstadoAutorizacion"))
WHERE "rmp"."Borrado"='N';
  	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" TO BCS_DESARROLLADOR WITH GRANT OPTION;
