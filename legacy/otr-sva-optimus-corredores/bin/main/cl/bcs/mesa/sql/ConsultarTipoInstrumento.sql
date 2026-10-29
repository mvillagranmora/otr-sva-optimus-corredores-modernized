SELECT  "TipoInstrumento"."IdTipoInstrumento" , 
        "TipoInstrumento"."Codigo" AS "CodigoTipoInstrumento" , 
        "TipoInstrumento"."Nombre" AS "NombreTipoInstrumento" , 
        "TipoInstrumento"."IdTipoInstrumento" || ' - ' || "TipoInstrumento"."Nombre" AS "NombreLargoTipoInstrumento" , 
        "TipoInstrumento"."Descripcion" AS "DescripcionTipoInstrumento" , 
        "TipoInstrumento"."IdEstado" , 
        "ClasificacionTipoInstrumento"."Nombre" AS "ClasificacionTipoInstrumento" , 
        "TipoInstrumento"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" , 
        "Bolsa"."IdBolsa" AS "IdBolsa" ,        
        "TipoOperacionTipoInstrumento"."IdTipoOperacion" ,
        "TipoOperacionTipoInstrumento"."IdInstrumento",
        "TipoOperacionTipoInstrumento"."Nemotecnico", 
        "TipoOperacion"."Accion" , 
        "TipoOperacion"."EsOrden" , 
        "TipoOperacion"."EsVentaCorta" , 
        "TipoOperacion"."EsTransaccion" , 
        "TipoOperacion"."Comprobante",
        "BolsaTipoInstrumento"."ExigeGarantia",
        COALESCE("EntidadDestinoGarantia"."IdContraparte",0) AS "IdContraparteDestinoGarantia",     
        COALESCE("EntidadDestinoGarantia"."IdEntidadDestinoGarantia",0) AS "IdEntidadDestinoGarantia",
        "BolsaTipoInstrumento"."IdBolsaTipoInstrumento"

FROM  "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" AS "BolsaTipoInstrumento" ON  "BolsaTipoInstrumento"."IdTipoInstrumento" = "TipoInstrumento"."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS "EntidadDestinoGarantia" ON ("EntidadDestinoGarantia"."IdEntidadDestinoGarantia" = "BolsaTipoInstrumento"."IdEntidadDestinoGarantia")
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS "Bolsa" ON  "Bolsa"."IdBolsa" = "BolsaTipoInstrumento"."IdBolsa" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento" ON "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacionTipoInstrumento" ON "TipoOperacionTipoInstrumento"."IdTipoInstrumento" = "TipoInstrumento"."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON "TipoOperacion"."IdTipoOperacion" = "TipoOperacionTipoInstrumento"."IdTipoOperacion"
WHERE "TipoOperacionTipoInstrumento"."IdTipoOperacion" IN (:IdTipoOperacion);
--AND "TipoInstrumento"."IdTipoInstrumento" IN (:IdTipoInstrumento);