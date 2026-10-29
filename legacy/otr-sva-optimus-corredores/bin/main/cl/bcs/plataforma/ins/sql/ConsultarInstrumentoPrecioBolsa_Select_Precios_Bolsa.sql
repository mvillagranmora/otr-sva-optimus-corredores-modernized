WITH precios as(        
            SELECT row_number() over (partition by inn."IdInstrumento",bolins."IdBolsa",insfe."Fecha" order by inn."IdInstrumento",prec."Fecha" desc) AS row_num, insfe."IdInterno", inn."IdInstrumento",bolins."IdBolsa",insfe."Fecha" AS "FechaConsulta",
            CAST(prec."Fecha" AS VARCHAR(10)) AS "Fecha",prec."UnidadesTransadas" AS "VolumenTransado","MontoTransado",inst."Precio" AS "PrecioBase",prec."PrecioCierre", prec."Variacion",inst."IdMoneda", puntas."PuntaCompra", puntas."PuntaVenta",
            prec."PrecioApertura",prec."PrecioMayor",prec."PrecioMenor",prec."PrecioMedio",null as "PrecioMercado",null as "TasaMercado", puntas."CantidadPuntaCompra", puntas."CantidadPuntaVenta"
            FROM "BSCL.Plataforma.BD::INS.ConsultarInstrumentoPrecioBolsa_Proceso" insfe  
            INNER JOIN "BSCL.Custodia.BD::GBL.ConsultarInstrumentoPrecioBolsa_Insert_Tabla_Paso" Precio ON (Precio."IdInstrumento" = insfe."IdInstrumento" )
            INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" inn ON inn."IdInstrumento" = insfe."IdInstrumento" 
            INNER JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumento" bolins ON bolins."IdInstrumento" = inn."IdInstrumento" AND bolins."IdBolsa" = insfe."IdBolsa"
            LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on inn."IdInstrumento" = inst."IdInstrumento"
            LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec on prec."IdInstrumento" = inn."IdInstrumento" and prec."Fecha" <= insfe."Fecha"
            LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPuntas" puntas on inst."IdInstrumento" = puntas."IdInstrumento" and puntas."Fecha" = insfe."Fecha" and puntas."IdBolsa" = bolins."IdBolsa")
SELECT "IdInterno","IdInstrumento","IdBolsa","FechaConsulta","Fecha","VolumenTransado","MontoTransado","PrecioBase","PrecioCierre","PuntaCompra","PuntaVenta","Variacion",
               "IdMoneda","PrecioApertura","PrecioMayor","PrecioMenor","PrecioMedio","PrecioMercado","TasaMercado","CantidadPuntaCompra","CantidadPuntaVenta"
FROM precios
WHERE row_num =1
ORDER BY "IdInstrumento","IdBolsa","FechaConsulta","Fecha" desc;
